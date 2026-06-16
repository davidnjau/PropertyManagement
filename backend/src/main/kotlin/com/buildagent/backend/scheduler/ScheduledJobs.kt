package com.buildagent.backend.scheduler

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.*
import com.buildagent.backend.kafka.KafkaFactory
import com.buildagent.shared.events.*
import com.buildagent.shared.models.LeaseStatus
import com.buildagent.shared.models.MaintenanceStatus
import kotlinx.coroutines.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.slf4j.LoggerFactory
import java.util.UUID

class ScheduledJobs {
    private val log = LoggerFactory.getLogger("ScheduledJobs")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        scope.launch {
            while (isActive) {
                try { runSlaCheck() } catch (e: Exception) { log.error("SLA check failed: ${e.message}") }
                try { runLeaseExpiryCheck() } catch (e: Exception) { log.error("Lease expiry check failed: ${e.message}") }
                delay(24 * 60 * 60 * 1000L)
            }
        }
        log.info("Scheduled jobs started")
    }

    private suspend fun runSlaCheck() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val breached = dbQuery {
            MaintenanceRequestsTable.selectAll().where {
                (MaintenanceRequestsTable.status notInList listOf(
                    MaintenanceStatus.COMPLETED, MaintenanceStatus.CLOSED, MaintenanceStatus.CANCELLED
                )) and
                (MaintenanceRequestsTable.slaTargetDate less today)
            }.toList()
        }

        breached.forEach { row ->
            val slaDate = row[MaintenanceRequestsTable.slaTargetDate] ?: return@forEach
            val daysOverdue = (today.toEpochDays() - slaDate.toEpochDays()).toInt()
            KafkaFactory.sendMaintenanceSlaBreached(
                MaintenanceSlaBreachedEvent(
                    eventId = UUID.randomUUID().toString(),
                    agencyId = row[MaintenanceRequestsTable.agencyId].value.toString(),
                    requestId = row[MaintenanceRequestsTable.id].value.toString(),
                    unitId = row[MaintenanceRequestsTable.unitId].value.toString(),
                    title = row[MaintenanceRequestsTable.title],
                    priority = row[MaintenanceRequestsTable.priority].name,
                    slaTargetDate = slaDate.toString(),
                    daysOverdue = daysOverdue,
                    occurredAt = Clock.System.now().toString()
                )
            )
        }
        if (breached.isNotEmpty()) log.info("SLA breach events emitted: ${breached.size}")
    }

    private suspend fun runLeaseExpiryCheck() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val in60Days = today.plus(60, DateTimeUnit.DAY)
        val expiring = dbQuery {
            LeasesTable
                .join(TenantsTable, JoinType.INNER, LeasesTable.tenantId, TenantsTable.id)
                .selectAll()
                .where {
                    (LeasesTable.status inList listOf(LeaseStatus.ACTIVE, LeaseStatus.PERIODIC)) and
                    (LeasesTable.endDate greaterEq today) and
                    (LeasesTable.endDate lessEq in60Days)
                }.toList()
        }

        expiring.forEach { row ->
            val endDate = row[LeasesTable.endDate] ?: return@forEach
            val daysUntil = (endDate.toEpochDays() - today.toEpochDays()).toInt()
            KafkaFactory.sendLeaseExpiring(
                LeaseExpiringEvent(
                    eventId = UUID.randomUUID().toString(),
                    agencyId = row[TenantsTable.agencyId].value.toString(),
                    leaseId = row[LeasesTable.id].value.toString(),
                    unitId = row[LeasesTable.unitId].value.toString(),
                    tenantId = row[LeasesTable.tenantId].value.toString(),
                    tenantEmail = row[TenantsTable.email],
                    tenantName = row[TenantsTable.fullName],
                    endDate = endDate.toString(),
                    daysUntilExpiry = daysUntil,
                    occurredAt = Clock.System.now().toString()
                )
            )
        }
        if (expiring.isNotEmpty()) log.info("Lease expiry events emitted: ${expiring.size}")
    }
}
