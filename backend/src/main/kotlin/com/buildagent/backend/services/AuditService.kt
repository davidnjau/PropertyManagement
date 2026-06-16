package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.AuditEventsTable
import com.buildagent.backend.kafka.KafkaFactory
import com.buildagent.shared.events.AuditStreamEvent
import com.buildagent.shared.models.AuditAction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import java.time.Instant
import java.util.UUID

class AuditService {
    private val json = Json { encodeDefaults = true }

    suspend fun record(
        agencyId: UUID,
        actorId: UUID,
        action: AuditAction,
        entityType: String,
        entityId: UUID,
        before: Map<String, String?>? = null,
        after: Map<String, String?>? = null,
        ipAddress: String? = null,
        userAgent: String? = null
    ) {
        val diffJson = if (before != null || after != null)
            json.encodeToString(mapOf("before" to before, "after" to after))
        else null

        dbQuery {
            AuditEventsTable.insert {
                it[AuditEventsTable.agencyId] = agencyId
                it[AuditEventsTable.actorId] = actorId
                it[AuditEventsTable.action] = action
                it[AuditEventsTable.entityType] = entityType
                it[AuditEventsTable.entityId] = entityId
                it[AuditEventsTable.diffJson] = diffJson
                it[AuditEventsTable.ipAddress] = ipAddress
                it[AuditEventsTable.userAgent] = userAgent
            }
        }

        KafkaFactory.sendAuditEvent(
            AuditStreamEvent(
                eventId = UUID.randomUUID().toString(),
                agencyId = agencyId.toString(),
                actorId = actorId.toString(),
                action = action.name,
                entityType = entityType,
                entityId = entityId.toString(),
                diffJson = diffJson,
                occurredAt = Instant.now().toString()
            )
        )
    }
}
