package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class AlertsService {

    suspend fun list(
        agencyId: UUID,
        status: String?,
        targetType: String?,
        channel: String?,
        from: String?,
        to: String?
    ): List<Alert> = dbQuery {
        var query = AlertsTable.selectAll().where { AlertsTable.agencyId eq agencyId }
        if (status != null) query = query.andWhere { AlertsTable.status eq status }
        if (targetType != null) query = query.andWhere { AlertsTable.targetType eq targetType }
        query.orderBy(AlertsTable.sentAt, SortOrder.DESC).map { it.toAlert() }
    }

    suspend fun create(agencyId: UUID, actorId: UUID, req: CreateAlertRequest): Alert = dbQuery {
        val now: Instant = Clock.System.now()

        val recipients = mutableListOf<Pair<UUID, String>>()
        val tenantIds = req.tenantIds
        if (!tenantIds.isNullOrEmpty()) {
            tenantIds.forEach { tenantIdStr ->
                val tenantRow = TenantsTable.selectAll()
                    .where { TenantsTable.id eq UUID.fromString(tenantIdStr) }
                    .firstOrNull()
                if (tenantRow != null) {
                    recipients.add(UUID.fromString(tenantIdStr) to tenantRow[TenantsTable.fullName])
                }
            }
        }

        val targetLabel = when {
            req.buildingId != null -> {
                val bRow = BuildingsTable.selectAll().where { BuildingsTable.id eq UUID.fromString(req.buildingId) }.firstOrNull()
                bRow?.get(BuildingsTable.name) ?: bRow?.get(BuildingsTable.address) ?: req.buildingId
            }
            req.targetType == "all" -> "All Tenants"
            else -> req.targetType
        }

        val alertId = AlertsTable.insertAndGetId {
            it[AlertsTable.agencyId] = agencyId
            it[sentBy] = actorId
            it[AlertsTable.targetType] = req.targetType
            it[AlertsTable.targetLabel] = targetLabel ?: req.targetType
            it[buildingId] = req.buildingId?.let { b -> UUID.fromString(b) }
            it[rentDueFilter] = req.rentDueFilter
            it[channels] = req.channels.joinToString(",")
            it[subject] = req.subject
            it[message] = req.message
            it[recipientCount] = recipients.size
            it[status] = "SENT"
            it[sentAt] = now
        }

        req.tenantIds?.forEach { tenantIdStr ->
            AlertTenantIdsTable.insert {
                it[AlertTenantIdsTable.alertId] = alertId
                it[tenantId] = UUID.fromString(tenantIdStr)
            }
        }

        recipients.forEach { (tenantId, tenantName) ->
            val tenantRow = TenantsTable.selectAll().where { TenantsTable.id eq tenantId }.firstOrNull()
            AlertRecipientsTable.insert {
                it[AlertRecipientsTable.alertId] = alertId
                it[AlertRecipientsTable.tenantId] = tenantId
                it[AlertRecipientsTable.tenantName] = tenantName
                it[email] = tenantRow?.get(TenantsTable.email)
                it[phone] = tenantRow?.get(TenantsTable.phone)
                it[inAppStatus] = "SENT"
                it[emailStatus] = if (req.channels.contains("email")) "SENT" else "SKIPPED"
                it[smsStatus] = if (req.channels.contains("sms")) "SENT" else "SKIPPED"
            }
        }

        AlertsTable.selectAll().where { AlertsTable.id eq alertId }.single().toAlert()
    }

    suspend fun getById(agencyId: UUID, alertId: UUID): Alert? = dbQuery {
        val row = AlertsTable.selectAll()
            .where { (AlertsTable.id eq alertId) and (AlertsTable.agencyId eq agencyId) }
            .firstOrNull() ?: return@dbQuery null

        val recipients = AlertRecipientsTable.selectAll()
            .where { AlertRecipientsTable.alertId eq alertId }
            .map {
                AlertRecipient(
                    id = it[AlertRecipientsTable.id].value.toString(),
                    alertId = alertId.toString(),
                    tenantId = it[AlertRecipientsTable.tenantId].toString(),
                    tenantName = it[AlertRecipientsTable.tenantName],
                    email = it[AlertRecipientsTable.email],
                    phone = it[AlertRecipientsTable.phone],
                    inAppStatus = it[AlertRecipientsTable.inAppStatus],
                    emailStatus = it[AlertRecipientsTable.emailStatus],
                    smsStatus = it[AlertRecipientsTable.smsStatus]
                )
            }

        row.toAlert().copy(recipients = recipients)
    }

    suspend fun delete(agencyId: UUID, alertId: UUID): Boolean = dbQuery {
        val existing = AlertsTable.selectAll()
            .where { (AlertsTable.id eq alertId) and (AlertsTable.agencyId eq agencyId) }
            .firstOrNull() ?: return@dbQuery false
        AlertRecipientsTable.deleteWhere { AlertRecipientsTable.alertId eq alertId }
        AlertTenantIdsTable.deleteWhere { AlertTenantIdsTable.alertId eq alertId }
        AlertsTable.deleteWhere { AlertsTable.id eq alertId }
        true
    }

    private fun ResultRow.toAlert() = Alert(
        id = this[AlertsTable.id].value.toString(),
        agencyId = this[AlertsTable.agencyId].value.toString(),
        sentBy = this[AlertsTable.sentBy].value.toString(),
        targetType = this[AlertsTable.targetType],
        targetLabel = this[AlertsTable.targetLabel],
        buildingId = this[AlertsTable.buildingId]?.toString(),
        rentDueFilter = this[AlertsTable.rentDueFilter],
        channels = this[AlertsTable.channels].split(",").filter { it.isNotBlank() },
        subject = this[AlertsTable.subject],
        message = this[AlertsTable.message],
        recipientCount = this[AlertsTable.recipientCount],
        status = this[AlertsTable.status],
        failureReason = this[AlertsTable.failureReason],
        sentAt = this[AlertsTable.sentAt].toString()
    )
}
