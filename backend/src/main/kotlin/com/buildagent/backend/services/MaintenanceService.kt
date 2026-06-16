package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.extensions.toMaintenance
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.domain.SlaUtils
import com.buildagent.shared.models.*
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import java.util.UUID

class MaintenanceService {

    suspend fun listMaintenance(
        agencyId: String,
        status: MaintenanceStatus? = null,
        priority: MaintenancePriority? = null,
        page: Int = 1,
        limit: Int = 20
    ): Pair<List<MaintenanceRequest>, Int> = dbQuery {
        val offset = ((page - 1) * limit).toLong()
        val conditions = mutableListOf<Op<Boolean>>(
            MaintenanceRequestsTable.agencyId eq UUID.fromString(agencyId)
        )
        status?.let { conditions.add(MaintenanceRequestsTable.status eq it) }
        priority?.let { conditions.add(MaintenanceRequestsTable.priority eq it) }

        val query = MaintenanceRequestsTable.selectAll().where { conditions.reduce { a, b -> a and b } }
        val total = query.count().toInt()
        val requests = query
            .orderBy(MaintenanceRequestsTable.priority, SortOrder.ASC)
            .orderBy(MaintenanceRequestsTable.createdAt, SortOrder.DESC)
            .limit(limit, offset)
            .map { it.toMaintenance() }
        requests to total
    }

    suspend fun getRequest(agencyId: String, requestId: String): MaintenanceRequest? = dbQuery {
        MaintenanceRequestsTable.selectAll().where {
            (MaintenanceRequestsTable.id eq UUID.fromString(requestId)) and
            (MaintenanceRequestsTable.agencyId eq UUID.fromString(agencyId))
        }.firstOrNull()?.toMaintenance()
    }

    suspend fun createRequest(agencyId: String, actorId: String, request: CreateMaintenanceRequest): MaintenanceRequest = dbQuery {
        UnitsTable
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (UnitsTable.id eq UUID.fromString(request.unitId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .firstOrNull() ?: error("Unit not found")

        val now: Instant = Clock.System.now()
        val slaDays = SlaUtils.slaDaysFor(request.priority)
        val slaTarget = Clock.System.todayIn(TimeZone.currentSystemDefault()).plus(slaDays, DateTimeUnit.DAY)

        val id = MaintenanceRequestsTable.insertAndGetId {
            it[MaintenanceRequestsTable.agencyId] = UUID.fromString(agencyId)
            it[unitId] = UUID.fromString(request.unitId)
            it[reportedByType] = ReporterType.AGENT
            it[reportedById] = UUID.fromString(actorId)
            it[category] = request.category
            it[priority] = request.priority
            it[title] = request.title
            it[description] = request.description
            it[slaTargetDate] = slaTarget
            it[createdAt] = now
            it[updatedAt] = now
        }

        MaintenanceRequestsTable.selectAll().where { MaintenanceRequestsTable.id eq id }.first().toMaintenance()
    }

    suspend fun updateStatus(agencyId: String, requestId: String, request: UpdateMaintenanceRequest): MaintenanceRequest = dbQuery {
        MaintenanceRequestsTable.selectAll().where {
            (MaintenanceRequestsTable.id eq UUID.fromString(requestId)) and
            (MaintenanceRequestsTable.agencyId eq UUID.fromString(agencyId))
        }.firstOrNull() ?: error("Request not found")

        val nowUpdate: Instant = Clock.System.now()
        MaintenanceRequestsTable.update({ MaintenanceRequestsTable.id eq UUID.fromString(requestId) }) {
            request.status?.let { s -> it[status] = s }
            request.contractorName?.let { v -> it[contractorName] = v }
            request.contractorPhone?.let { v -> it[contractorPhone] = v }
            request.assignedDate?.let { d -> it[assignedDate] = LocalDate.parse(d) }
            request.attendedDate?.let { d -> it[attendedDate] = LocalDate.parse(d) }
            request.notes?.let { v -> it[notes] = v }
            it[updatedAt] = nowUpdate
        }

        MaintenanceRequestsTable.selectAll().where { MaintenanceRequestsTable.id eq UUID.fromString(requestId) }.first().toMaintenance()
    }

    suspend fun closeRequest(agencyId: String, actorId: String, requestId: String, request: CloseMaintenanceRequest): MaintenanceRequest = dbQuery {
        MaintenanceRequestsTable.selectAll().where {
            (MaintenanceRequestsTable.id eq UUID.fromString(requestId)) and
            (MaintenanceRequestsTable.agencyId eq UUID.fromString(agencyId))
        }.firstOrNull() ?: error("Request not found")

        val now: Instant = Clock.System.now()
        MaintenanceRequestsTable.update({ MaintenanceRequestsTable.id eq UUID.fromString(requestId) }) {
            it[status] = MaintenanceStatus.CLOSED
            it[closedDate] = Clock.System.todayIn(TimeZone.currentSystemDefault())
            it[completedDate] = LocalDate.parse(request.completedDate)
            it[invoiceReference] = request.invoiceReference
            it[invoiceAmount] = request.invoiceAmount?.toBigDecimal()
            it[agentSignOff] = UUID.fromString(actorId)
            it[notes] = request.notes
            it[updatedAt] = now
        }

        MaintenanceRequestsTable.selectAll().where { MaintenanceRequestsTable.id eq UUID.fromString(requestId) }.first().toMaintenance()
    }

    suspend fun getSlaBreached(agencyId: String): List<MaintenanceRequest> = dbQuery {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        MaintenanceRequestsTable.selectAll().where {
            (MaintenanceRequestsTable.agencyId eq UUID.fromString(agencyId)) and
            (MaintenanceRequestsTable.status notInList listOf(
                MaintenanceStatus.COMPLETED, MaintenanceStatus.CLOSED, MaintenanceStatus.CANCELLED
            )) and
            (MaintenanceRequestsTable.slaTargetDate less today)
        }.map { it.toMaintenance() }
    }
}
