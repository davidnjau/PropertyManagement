package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.extensions.toDocument
import com.buildagent.backend.db.extensions.toExtensionRequest
import com.buildagent.backend.db.extensions.toLease
import com.buildagent.backend.db.extensions.toMaintenance
import com.buildagent.backend.db.extensions.toPayment
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.UUID

class TenantPortalService {

    // userId is the Users.id UUID from JWT; look up the Tenant by matching email
    private fun findTenantId(userId: String): UUID? {
        val userRow = UsersTable.selectAll()
            .where { UsersTable.id eq UUID.fromString(userId) }
            .firstOrNull() ?: return null
        val email = userRow[UsersTable.email]
        return TenantsTable.selectAll()
            .where { TenantsTable.email eq email }
            .firstOrNull()?.get(TenantsTable.id)?.value
    }

    private fun findActiveLease(tenantId: UUID): org.jetbrains.exposed.sql.ResultRow? {
        return LeasesTable.selectAll()
            .where {
                (LeasesTable.tenantId eq tenantId) and
                (LeasesTable.status inList listOf(LeaseStatus.ACTIVE, LeaseStatus.PERIODIC))
            }
            .firstOrNull()
    }

    suspend fun getOverview(userId: String): TenantOverview = dbQuery {
        val tenantId = findTenantId(userId) ?: error("Tenant profile not found for user $userId")
        val leaseRow = findActiveLease(tenantId)
        val lease = leaseRow?.toLease()

        val recentPayments = if (lease != null) {
            PaymentsTable.selectAll()
                .where { PaymentsTable.leaseId eq UUID.fromString(lease.id) }
                .orderBy(PaymentsTable.createdAt, SortOrder.DESC)
                .limit(3)
                .map { it.toPayment() }
        } else emptyList()

        TenantOverview(
            rentDue = lease?.rentAmount,
            rentDueDate = lease?.paymentDay?.let { day ->
                val today = Clock.System.now().toString().substring(0, 10)
                val parts = today.split("-")
                "${parts[0]}-${parts[1]}-${day.toString().padStart(2, '0')}"
            },
            leaseEndDate = lease?.endDate,
            leaseStatus = lease?.status?.name,
            recentPayments = recentPayments
        )
    }

    suspend fun getActiveLease(userId: String): Lease? = dbQuery {
        val tenantId = findTenantId(userId) ?: return@dbQuery null
        findActiveLease(tenantId)?.toLease()
    }

    suspend fun getPayments(userId: String, page: Int, limit: Int): Pair<List<Payment>, Int> = dbQuery {
        val tenantId = findTenantId(userId) ?: error("Tenant not found")
        val leaseIds = LeasesTable.selectAll()
            .where { LeasesTable.tenantId eq tenantId }
            .map { it[LeasesTable.id].value }

        if (leaseIds.isEmpty()) return@dbQuery emptyList<Payment>() to 0

        val offset = ((page - 1) * limit).toLong()
        val total = PaymentsTable.selectAll()
            .where { PaymentsTable.leaseId inList leaseIds }
            .count().toInt()
        val payments = PaymentsTable.selectAll()
            .where { PaymentsTable.leaseId inList leaseIds }
            .orderBy(PaymentsTable.createdAt, SortOrder.DESC)
            .limit(limit, offset)
            .map { it.toPayment() }

        payments to total
    }

    suspend fun recordPayment(agencyId: UUID, userId: String, req: RecordPaymentRequest): Payment = dbQuery {
        val tenantId = findTenantId(userId) ?: error("Tenant not found")
        val leaseId = UUID.fromString(req.leaseId)
        val lease = LeasesTable.selectAll()
            .where { (LeasesTable.id eq leaseId) and (LeasesTable.tenantId eq tenantId) }
            .firstOrNull() ?: error("Lease not found or does not belong to tenant")

        val now: Instant = Clock.System.now()
        val id = PaymentsTable.insertAndGetId {
            it[PaymentsTable.leaseId] = leaseId
            it[PaymentsTable.agencyId] = agencyId
            it[amount] = req.amount.toBigDecimal()
            it[paymentType] = req.paymentType
            it[status] = req.status
            it[periodFrom] = LocalDate.parse(req.periodFrom)
            it[periodTo] = LocalDate.parse(req.periodTo)
            it[referenceNo] = req.referenceNo
            it[notes] = req.notes
            it[paymentDate] = req.paymentDate?.let { d -> LocalDate.parse(d) }
            it[createdAt] = now
            it[updatedAt] = now
        }
        PaymentsTable.selectAll().where { PaymentsTable.id eq id }.single().toPayment()
    }

    suspend fun getMaintenance(agencyId: UUID, userId: String, page: Int, limit: Int): Pair<List<MaintenanceRequest>, Int> = dbQuery {
        val tenantId = findTenantId(userId) ?: error("Tenant not found")
        val offset = ((page - 1) * limit).toLong()
        val query = MaintenanceRequestsTable.selectAll().where {
            (MaintenanceRequestsTable.agencyId eq agencyId) and
            (MaintenanceRequestsTable.reportedById eq tenantId)
        }
        val total = query.count().toInt()
        val requests = query.orderBy(MaintenanceRequestsTable.createdAt, SortOrder.DESC)
            .limit(limit, offset)
            .map { it.toMaintenance() }
        requests to total
    }

    suspend fun getDocuments(agencyId: UUID, userId: String): List<Document> = dbQuery {
        val tenantId = findTenantId(userId) ?: return@dbQuery emptyList<Document>()
        val leaseIds = LeasesTable.selectAll()
            .where { LeasesTable.tenantId eq tenantId }
            .map { it[LeasesTable.id].value }
        if (leaseIds.isEmpty()) return@dbQuery emptyList<Document>()
        DocumentsTable.selectAll()
            .where {
                (DocumentsTable.agencyId eq agencyId) and
                (DocumentsTable.targetId inList leaseIds) and
                DocumentsTable.deletedAt.isNull()
            }
            .orderBy(DocumentsTable.uploadedAt, SortOrder.DESC)
            .map { it.toDocument() }
    }

    suspend fun getPaymentMethods(agencyId: UUID): PaymentMethodsConfig = dbQuery {
        val methods = PaymentMethodsConfigTable.selectAll()
            .where { PaymentMethodsConfigTable.agencyId eq agencyId }
            .map { PaymentMethod(it[PaymentMethodsConfigTable.methodId], it[PaymentMethodsConfigTable.enabled]) }
        val banks = BankConfigTable.selectAll()
            .where { BankConfigTable.agencyId eq agencyId }
            .map { BankConfig(it[BankConfigTable.bankId], it[BankConfigTable.bankName], it[BankConfigTable.enabled]) }
        val mpesa = MpesaConfigTable.selectAll()
            .where { MpesaConfigTable.agencyId eq agencyId }
            .firstOrNull()?.let {
                MpesaConfig(it[MpesaConfigTable.businessNo], it[MpesaConfigTable.accountNo], it[MpesaConfigTable.instructions])
            }
        val paypal = PaypalConfigTable.selectAll()
            .where { PaypalConfigTable.agencyId eq agencyId }
            .firstOrNull()?.let {
                PaypalConfig(it[PaypalConfigTable.email], it[PaypalConfigTable.instructions])
            }
        PaymentMethodsConfig(
            methods = methods,
            banks = banks,
            mpesaConfig = mpesa,
            paypalConfig = paypal
        )
    }

    suspend fun submitLeaseExtension(agencyId: UUID, userId: String, req: CreateLeaseExtensionRequest): LeaseExtensionRequest = dbQuery {
        val tenantId = findTenantId(userId) ?: error("Tenant not found")
        val leaseRow = LeasesTable.selectAll()
            .where { (LeasesTable.id eq UUID.fromString(req.leaseId)) and (LeasesTable.tenantId eq tenantId) }
            .firstOrNull() ?: error("Lease not found")

        val currentEnd = leaseRow[LeasesTable.endDate]
            ?: error("Lease has no end date; cannot submit extension request")
        val proposedEnd = when {
            req.customEndDate != null -> LocalDate.parse(req.customEndDate!!)
            req.durationMonths != null -> currentEnd.plus(req.durationMonths!!, DateTimeUnit.MONTH)
            else -> error("Provide either durationMonths or customEndDate")
        }

        val now: Instant = Clock.System.now()
        val id = LeaseExtensionRequestsTable.insertAndGetId {
            it[LeaseExtensionRequestsTable.agencyId] = agencyId
            it[LeaseExtensionRequestsTable.leaseId] = UUID.fromString(req.leaseId)
            it[LeaseExtensionRequestsTable.tenantId] = tenantId
            it[LeaseExtensionRequestsTable.currentEndDate] = currentEnd
            it[LeaseExtensionRequestsTable.proposedEndDate] = proposedEnd
            it[LeaseExtensionRequestsTable.durationMonths] = req.durationMonths
            it[LeaseExtensionRequestsTable.customEndDate] = req.customEndDate?.let { d -> LocalDate.parse(d) }
            it[LeaseExtensionRequestsTable.notes] = req.notes
            it[LeaseExtensionRequestsTable.status] = "Pending"
            it[LeaseExtensionRequestsTable.submittedAt] = now
        }
        LeaseExtensionRequestsTable.selectAll().where { LeaseExtensionRequestsTable.id eq id }.single().toExtensionRequest()
    }

    suspend fun submitMaintenance(agencyId: UUID, userId: String, req: CreateMaintenanceRequest): MaintenanceRequest = dbQuery {
        val tenantId = findTenantId(userId) ?: error("Tenant not found")
        val lease = findActiveLease(tenantId) ?: error("No active lease found")
        val unitId = lease[LeasesTable.unitId].value

        val now: Instant = Clock.System.now()
        val id = MaintenanceRequestsTable.insertAndGetId {
            it[MaintenanceRequestsTable.agencyId] = agencyId
            it[MaintenanceRequestsTable.unitId] = unitId
            it[reportedByType] = ReporterType.TENANT
            it[reportedById] = tenantId
            it[category] = req.category
            it[priority] = req.priority
            it[title] = req.title
            it[description] = req.description
            it[status] = MaintenanceStatus.REPORTED
            it[createdAt] = now
            it[updatedAt] = now
        }
        MaintenanceRequestsTable.selectAll().where { MaintenanceRequestsTable.id eq id }.single().toMaintenance()
    }
}
