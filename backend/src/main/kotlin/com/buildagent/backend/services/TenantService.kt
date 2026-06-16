package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.extensions.toTenant
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.UUID

class TenantService {

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
        val id = TenantsTable.insertAndGetId {
            it[TenantsTable.agencyId] = UUID.fromString(agencyId)
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
}
