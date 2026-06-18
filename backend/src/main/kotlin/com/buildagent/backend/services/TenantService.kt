package com.buildagent.backend.services

import com.buildagent.backend.auth.PasswordHasher
import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.extensions.toLease
import com.buildagent.backend.db.extensions.toTenant
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.slf4j.LoggerFactory
import java.util.Base64
import java.util.UUID

class TenantService(private val notificationService: NotificationService = NotificationService()) {

    private val log = LoggerFactory.getLogger("TenantService")

    suspend fun listTenants(agencyId: String, page: Int = 1, limit: Int = 20): Pair<List<Tenant>, Int> = dbQuery {
        val offset = ((page - 1) * limit).toLong()
        val query = TenantsTable.selectAll().where {
            (TenantsTable.agencyId eq UUID.fromString(agencyId)) and
            (TenantsTable.isActive eq true)
        }
        val total = query.count().toInt()
        val tenants = query
            .orderBy(TenantsTable.fullName, SortOrder.ASC)
            .limit(limit, offset)
            .map { it.toTenant() }
        tenants to total
    }

    suspend fun getTenant(agencyId: String, tenantId: String): Tenant? = dbQuery {
        TenantsTable.selectAll().where {
            (TenantsTable.id eq UUID.fromString(tenantId)) and
            (TenantsTable.agencyId eq UUID.fromString(agencyId))
        }.firstOrNull()?.toTenant()
    }

    suspend fun createTenant(agencyId: String, request: CreateTenantRequest): Tenant = dbQuery {
        val now: Instant = Clock.System.now()
        val agencyUUID = UUID.fromString(agencyId)

        val id = TenantsTable.insertAndGetId {
            it[TenantsTable.agencyId] = agencyUUID
            it[fullName] = request.fullName
            it[email] = request.email
            it[phone] = request.phone
            it[dateOfBirth] = request.dateOfBirth?.let { d -> LocalDate.parse(d) }
            it[idType] = request.idType
            it[idReference] = request.idReference
            it[emergencyContactName] = request.emergencyContactName
            it[emergencyContactPhone] = request.emergencyContactPhone
            it[notes] = request.notes
            it[createdAt] = now
            it[updatedAt] = now
        }

        // Create login credentials so the tenant can log in via the tenant portal
        if (!request.password.isNullOrBlank()) {
            val existingUser = UsersTable.selectAll()
                .where { UsersTable.email eq request.email }
                .firstOrNull()
            if (existingUser == null) {
                val salt = PasswordHasher.generateSalt()
                val hash = PasswordHasher.hash(request.password, salt)
                val userId = UsersTable.insertAndGetId {
                    it[UsersTable.agencyId] = agencyUUID
                    it[UsersTable.auth0Sub] = "local|${UUID.randomUUID()}"
                    it[UsersTable.email] = request.email
                    it[UsersTable.fullName] = request.fullName
                    it[UsersTable.role] = UserRole.TENANT
                    it[UsersTable.phone] = request.phone
                    it[UsersTable.createdAt] = now
                    it[UsersTable.updatedAt] = now
                }
                UserCredentialsTable.insertAndGetId {
                    it[UserCredentialsTable.userId] = userId
                    it[UserCredentialsTable.passwordHash] = hash
                    it[UserCredentialsTable.salt] = Base64.getEncoder().encodeToString(salt)
                }
            }
        }

        TenantsTable.selectAll().where { TenantsTable.id eq id }.first().toTenant()
    }

    suspend fun getTenantEnriched(agencyId: String, tenantId: String): TenantDetail? = dbQuery {
        val tenantUUID = UUID.fromString(tenantId)
        val agencyUUID = UUID.fromString(agencyId)
        val row = TenantsTable.selectAll().where {
            (TenantsTable.id eq tenantUUID) and (TenantsTable.agencyId eq agencyUUID)
        }.firstOrNull() ?: return@dbQuery null

        val tenant = row.toTenant()

        val totalPaid = PaymentsTable
            .join(LeasesTable, JoinType.INNER, PaymentsTable.leaseId, LeasesTable.id)
            .select(PaymentsTable.amount.sum())
            .where {
                (LeasesTable.tenantId eq tenantUUID) and
                (PaymentsTable.status eq PaymentStatus.RECEIVED)
            }
            .firstOrNull()?.get(PaymentsTable.amount.sum())?.toDouble() ?: 0.0

        val lastPaymentDate = PaymentsTable
            .join(LeasesTable, JoinType.INNER, PaymentsTable.leaseId, LeasesTable.id)
            .select(PaymentsTable.paymentDate)
            .where {
                (LeasesTable.tenantId eq tenantUUID) and
                (PaymentsTable.status eq PaymentStatus.RECEIVED)
            }
            .orderBy(PaymentsTable.paymentDate, SortOrder.DESC)
            .firstOrNull()?.get(PaymentsTable.paymentDate)?.toString()

        val overdueCount = PaymentsTable
            .join(LeasesTable, JoinType.INNER, PaymentsTable.leaseId, LeasesTable.id)
            .selectAll()
            .where {
                (LeasesTable.tenantId eq tenantUUID) and
                (PaymentsTable.status inList listOf(PaymentStatus.OVERDUE, PaymentStatus.PENDING))
            }
            .count().toInt()

        val openMaintenanceCount = MaintenanceRequestsTable.selectAll().where {
            (MaintenanceRequestsTable.reportedById eq tenantUUID) and
            (MaintenanceRequestsTable.status inList listOf(
                MaintenanceStatus.REPORTED, MaintenanceStatus.ASSESSED,
                MaintenanceStatus.ASSIGNED, MaintenanceStatus.IN_PROGRESS
            ))
        }.count().toInt()

        TenantDetail(
            tenant = tenant,
            paymentSummary = TenantPaymentSummary(
                totalPaid = totalPaid,
                lastPaymentDate = lastPaymentDate,
                overdueCount = overdueCount
            ),
            openMaintenanceCount = openMaintenanceCount
        )
    }

    suspend fun deleteTenant(agencyId: String, tenantId: String): Boolean = dbQuery {
        val tenantUUID = UUID.fromString(tenantId)
        val hasActiveLease = LeasesTable.selectAll().where {
            (LeasesTable.tenantId eq tenantUUID) and
            (LeasesTable.status inList listOf(LeaseStatus.ACTIVE, LeaseStatus.PERIODIC))
        }.count() > 0
        if (hasActiveLease) return@dbQuery false

        val now: Instant = Clock.System.now()
        TenantsTable.update({
            (TenantsTable.id eq tenantUUID) and (TenantsTable.agencyId eq UUID.fromString(agencyId))
        }) {
            it[isActive] = false
            it[updatedAt] = now
        }
        true
    }

    suspend fun updateTenant(agencyId: String, tenantId: String, request: CreateTenantRequest): Tenant = dbQuery {
        TenantsTable.selectAll().where {
            (TenantsTable.id eq UUID.fromString(tenantId)) and
            (TenantsTable.agencyId eq UUID.fromString(agencyId))
        }.firstOrNull() ?: error("Tenant not found")

        val nowUpdate: Instant = Clock.System.now()
        TenantsTable.update({ TenantsTable.id eq UUID.fromString(tenantId) }) {
            it[fullName] = request.fullName
            it[email] = request.email
            it[phone] = request.phone
            it[notes] = request.notes
            it[updatedAt] = nowUpdate
        }
        TenantsTable.selectAll().where { TenantsTable.id eq UUID.fromString(tenantId) }.first().toTenant()
    }

    /**
     * Creates a tenant and their lease in one transaction, then asynchronously
     * provisions portal access and sends an appropriate notification:
     *  - New user      → 4-digit OTP, create TENANT credentials, send OTP
     *  - Existing TENANT → send new-lease notification (credentials unchanged)
     *  - Existing other role → send notification (role unchanged, portal still accessible)
     */
    suspend fun createTenantWithLease(agencyId: String, request: CreateTenantWithLeaseRequest): TenantWithLeaseResponse {
        val agencyUUID = UUID.fromString(agencyId)
        val now: Instant = Clock.System.now()

        val (tenant, lease) = dbQuery {
            // 1. Create tenant
            val tenantId = TenantsTable.insertAndGetId {
                it[TenantsTable.agencyId] = agencyUUID
                it[fullName] = request.fullName
                it[email] = request.email
                it[phone] = request.phone
                it[dateOfBirth] = request.dateOfBirth?.let { d -> LocalDate.parse(d) }
                it[idType] = request.idType
                it[idReference] = request.idReference
                it[emergencyContactName] = request.emergencyContactName
                it[emergencyContactPhone] = request.emergencyContactPhone
                it[notes] = request.notes
                it[createdAt] = now
                it[updatedAt] = now
            }

            // 2. Validate unit belongs to this agency
            UnitsTable.innerJoin(BuildingsTable)
                .selectAll()
                .where {
                    (UnitsTable.id eq UUID.fromString(request.unitId)) and
                    (BuildingsTable.agencyId eq agencyUUID)
                }
                .firstOrNull() ?: error("Unit not found or does not belong to this agency")

            // 3. Create lease
            val leaseId = LeasesTable.insertAndGetId {
                it[unitId] = UUID.fromString(request.unitId)
                it[LeasesTable.tenantId] = tenantId
                it[startDate] = LocalDate.parse(request.startDate)
                it[endDate] = request.endDate?.let { d -> LocalDate.parse(d) }
                it[rentAmount] = request.rentAmount.toBigDecimal()
                it[rentFrequency] = request.rentFrequency
                it[bondAmount] = request.bondAmount.toBigDecimal()
                it[paymentDay] = request.paymentDay
                it[specialConditions] = request.specialConditions
                it[moveInDate] = request.moveInDate?.let { d -> LocalDate.parse(d) }
                it[status] = LeaseStatus.ACTIVE
                it[createdAt] = now
                it[updatedAt] = now
            }

            // 4. Mark unit occupied
            UnitsTable.update({ UnitsTable.id eq UUID.fromString(request.unitId) }) {
                it[status] = UnitStatus.OCCUPIED
                it[updatedAt] = now
            }

            val tenant = TenantsTable.selectAll().where { TenantsTable.id eq tenantId }.first().toTenant()
            val lease = LeasesTable.selectAll().where { LeasesTable.id eq leaseId }.first().toLease()
            tenant to lease
        }

        // 5. Background: provision user access + notify
        CoroutineScope(Dispatchers.IO).launch {
            try {
                provisionAndNotify(agencyUUID, tenant, lease.id, now)
            } catch (e: Exception) {
                log.error("[TenantService] provisionAndNotify failed for tenant=${tenant.id}: ${e.message}")
            }
        }

        return TenantWithLeaseResponse(tenant, lease)
    }

    private suspend fun provisionAndNotify(agencyUUID: UUID, tenant: Tenant, leaseId: String, now: Instant) {
        dbQuery {
            val existing = UsersTable.selectAll()
                .where { UsersTable.email eq tenant.email }
                .firstOrNull()

            when {
                existing == null -> {
                    // Brand new user — generate 4-digit OTP, create TENANT user + credentials
                    val otp = (1000..9999).random().toString()
                    val salt = PasswordHasher.generateSalt()
                    val hash = PasswordHasher.hash(otp, salt)
                    val userId = UsersTable.insertAndGetId {
                        it[UsersTable.agencyId] = agencyUUID
                        it[UsersTable.auth0Sub] = "local|${UUID.randomUUID()}"
                        it[UsersTable.email] = tenant.email
                        it[UsersTable.fullName] = tenant.fullName
                        it[UsersTable.role] = UserRole.TENANT
                        it[UsersTable.phone] = tenant.phone
                        it[UsersTable.createdAt] = now
                        it[UsersTable.updatedAt] = now
                    }
                    UserCredentialsTable.insertAndGetId {
                        it[UserCredentialsTable.userId] = userId
                        it[UserCredentialsTable.passwordHash] = hash
                        it[UserCredentialsTable.salt] = Base64.getEncoder().encodeToString(salt)
                    }
                    notificationService.sendOtp(tenant.phone, tenant.email, otp, tenant.fullName)
                }

                existing[UsersTable.role] == UserRole.TENANT -> {
                    // Already a tenant on another lease — notify of this new lease
                    notificationService.sendLeaseCreatedNotification(
                        tenant.phone, tenant.email, tenant.fullName, leaseId
                    )
                }

                else -> {
                    // Existing AGENT or ADMIN — they already have system access;
                    // notify them that a tenant lease has been linked to their account
                    notificationService.sendLeaseAddedToExistingUserNotification(
                        tenant.phone, tenant.email, tenant.fullName, leaseId
                    )
                }
            }
        }
    }
}
