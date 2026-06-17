package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class LeaseExtensionService {

    suspend fun list(agencyId: UUID, status: String?): List<LeaseExtensionRequest> = dbQuery {
        var query = LeaseExtensionRequestsTable.selectAll()
            .where { LeaseExtensionRequestsTable.agencyId eq agencyId }
        if (status != null) query = query.andWhere { LeaseExtensionRequestsTable.status eq status }
        query.orderBy(LeaseExtensionRequestsTable.submittedAt, SortOrder.DESC).map { it.toExtensionRequest() }
    }

    suspend fun resolve(agencyId: UUID, requestId: UUID, actorId: UUID, req: ResolveLeaseExtensionRequest): LeaseExtensionRequest? = dbQuery {
        val row = LeaseExtensionRequestsTable.selectAll()
            .where { (LeaseExtensionRequestsTable.id eq requestId) and (LeaseExtensionRequestsTable.agencyId eq agencyId) }
            .firstOrNull() ?: return@dbQuery null

        val now: Instant = Clock.System.now()
        LeaseExtensionRequestsTable.update({ LeaseExtensionRequestsTable.id eq requestId }) {
            it[status] = req.status
            it[agentNotes] = req.agentNotes
            it[resolvedAt] = now
            it[resolvedBy] = actorId
        }

        if (req.status == "Approved") {
            val leaseId = row[LeaseExtensionRequestsTable.leaseId].value
            val proposedEnd = row[LeaseExtensionRequestsTable.proposedEndDate]
            LeasesTable.update({ LeasesTable.id eq leaseId }) {
                it[endDate] = proposedEnd
                it[updatedAt] = now
            }
        }

        LeaseExtensionRequestsTable.selectAll().where { LeaseExtensionRequestsTable.id eq requestId }.single().toExtensionRequest()
    }

    suspend fun submitRequest(agencyId: UUID, tenantId: UUID, req: CreateLeaseExtensionRequest): LeaseExtensionRequest = dbQuery {
        val lease = LeasesTable.selectAll()
            .where { LeasesTable.id eq UUID.fromString(req.leaseId) }
            .firstOrNull() ?: error("Lease not found")

        val currentEnd = lease[LeasesTable.endDate] ?: error("Lease has no end date")

        val reqCustomEndDate = req.customEndDate
        val reqDurationMonths = req.durationMonths
        val proposedEnd = when {
            reqCustomEndDate != null -> LocalDate.parse(reqCustomEndDate)
            reqDurationMonths != null -> currentEnd.plus(reqDurationMonths, DateTimeUnit.MONTH)
            else -> error("Either durationMonths or customEndDate must be provided")
        }

        val now: Instant = Clock.System.now()
        val id = LeaseExtensionRequestsTable.insertAndGetId {
            it[LeaseExtensionRequestsTable.agencyId] = agencyId
            it[leaseId] = UUID.fromString(req.leaseId)
            it[LeaseExtensionRequestsTable.tenantId] = tenantId
            it[currentEndDate] = currentEnd
            it[proposedEndDate] = proposedEnd
            it[durationMonths] = req.durationMonths
            it[customEndDate] = req.customEndDate?.let { d -> LocalDate.parse(d) }
            it[notes] = req.notes
            it[status] = "Pending"
            it[submittedAt] = now
        }

        LeaseExtensionRequestsTable.selectAll().where { LeaseExtensionRequestsTable.id eq id }.single().toExtensionRequest()
    }

    private fun ResultRow.toExtensionRequest() = LeaseExtensionRequest(
        id = this[LeaseExtensionRequestsTable.id].value.toString(),
        agencyId = this[LeaseExtensionRequestsTable.agencyId].value.toString(),
        leaseId = this[LeaseExtensionRequestsTable.leaseId].value.toString(),
        tenantId = this[LeaseExtensionRequestsTable.tenantId].value.toString(),
        currentEndDate = this[LeaseExtensionRequestsTable.currentEndDate].toString(),
        proposedEndDate = this[LeaseExtensionRequestsTable.proposedEndDate].toString(),
        durationMonths = this[LeaseExtensionRequestsTable.durationMonths],
        customEndDate = this[LeaseExtensionRequestsTable.customEndDate]?.toString(),
        notes = this[LeaseExtensionRequestsTable.notes],
        status = this[LeaseExtensionRequestsTable.status],
        submittedAt = this[LeaseExtensionRequestsTable.submittedAt].toString(),
        resolvedAt = this[LeaseExtensionRequestsTable.resolvedAt]?.toString(),
        resolvedBy = this[LeaseExtensionRequestsTable.resolvedBy]?.toString(),
        agentNotes = this[LeaseExtensionRequestsTable.agentNotes]
    )
}
