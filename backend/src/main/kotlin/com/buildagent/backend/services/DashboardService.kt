package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.*
import com.buildagent.backend.redis.RedisFactory
import com.buildagent.shared.models.*
import kotlinx.datetime.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import java.util.UUID

class DashboardService {
    private val json = Json { encodeDefaults = true }

    suspend fun agentDashboard(agencyId: UUID): DashboardData {
        val cacheKey = "dashboard:agent:$agencyId"
        val cached = RedisFactory.get(cacheKey)
        if (cached != null) return json.decodeFromString(cached)

        val data = dbQuery {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val graceCutoff = today.minus(3, DateTimeUnit.DAY)
            val expiryWarning = today.plus(60, DateTimeUnit.DAY)

            val buildings = BuildingsTable.selectAll()
                .where { BuildingsTable.agencyId eq agencyId and (BuildingsTable.isActive eq true) }
                .count().toInt()

            val unitRows = UnitsTable
                .join(BuildingsTable, JoinType.INNER, UnitsTable.buildingId, BuildingsTable.id)
                .select(UnitsTable.status, UnitsTable.status.count())
                .where { BuildingsTable.agencyId eq agencyId }
                .groupBy(UnitsTable.status)
                .associate { it[UnitsTable.status] to it[UnitsTable.status.count()].toInt() }

            val occupied = unitRows[UnitStatus.OCCUPIED] ?: 0
            val vacant = unitRows[UnitStatus.VACANT] ?: 0
            val totalUnits = unitRows.values.sum()

            val overdue = PaymentsTable.selectAll().where {
                (PaymentsTable.agencyId eq agencyId) and
                (PaymentsTable.status inList listOf(PaymentStatus.PENDING, PaymentStatus.PARTIAL)) and
                (PaymentsTable.periodTo less graceCutoff)
            }.count().toInt()

            val expiring = LeasesTable
                .join(UnitsTable, JoinType.INNER, LeasesTable.unitId, UnitsTable.id)
                .join(BuildingsTable, JoinType.INNER, UnitsTable.buildingId, BuildingsTable.id)
                .selectAll()
                .where {
                    (BuildingsTable.agencyId eq agencyId) and
                    (LeasesTable.status inList listOf(LeaseStatus.ACTIVE, LeaseStatus.PERIODIC)) and
                    (LeasesTable.endDate greaterEq today) and
                    (LeasesTable.endDate lessEq expiryWarning)
                }.count().toInt()

            val openMaintenance = MaintenanceRequestsTable.selectAll().where {
                (MaintenanceRequestsTable.agencyId eq agencyId) and
                (MaintenanceRequestsTable.status notInList listOf(
                    MaintenanceStatus.COMPLETED, MaintenanceStatus.CLOSED, MaintenanceStatus.CANCELLED
                ))
            }.count().toInt()

            val slaBreached = MaintenanceRequestsTable.selectAll().where {
                (MaintenanceRequestsTable.agencyId eq agencyId) and
                (MaintenanceRequestsTable.status notInList listOf(
                    MaintenanceStatus.COMPLETED, MaintenanceStatus.CLOSED, MaintenanceStatus.CANCELLED
                )) and
                (MaintenanceRequestsTable.slaTargetDate less today)
            }.count().toInt()

            DashboardData(
                buildings = buildings,
                units = UnitStats(total = totalUnits, occupied = occupied, vacant = vacant),
                occupancyRate = if (totalUnits > 0) ((occupied.toDouble() / totalUnits) * 100).toInt() else 0,
                overduePayments = overdue,
                expiringLeases = expiring,
                openMaintenance = openMaintenance,
                slaBreached = slaBreached
            )
        }

        RedisFactory.set(cacheKey, json.encodeToString(data), ttlSeconds = 30)
        return data
    }
}
