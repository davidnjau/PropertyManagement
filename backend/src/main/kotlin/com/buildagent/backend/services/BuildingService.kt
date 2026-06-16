package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.UUID

class BuildingService {

    suspend fun list(agencyId: UUID, page: Int, limit: Int): Pair<List<Building>, Int> = dbQuery {
        val offset = ((page - 1) * limit).toLong()
        val query = BuildingsTable
            .join(ClientsTable, JoinType.LEFT, BuildingsTable.clientId, ClientsTable.id)
            .selectAll()
            .where { BuildingsTable.agencyId eq agencyId and (BuildingsTable.isActive eq true) }

        val total = query.count().toInt()
        val buildings = query.limit(limit, offset).map { row ->
            row.toBuilding(withClient = true)
        }
        buildings to total
    }

    suspend fun get(agencyId: UUID, buildingId: UUID): Building? = dbQuery {
        BuildingsTable
            .join(ClientsTable, JoinType.LEFT, BuildingsTable.clientId, ClientsTable.id)
            .selectAll()
            .where { BuildingsTable.id eq buildingId and (BuildingsTable.agencyId eq agencyId) and (BuildingsTable.isActive eq true) }
            .singleOrNull()
            ?.toBuilding(withClient = true)
    }

    suspend fun create(agencyId: UUID, req: CreateBuildingRequest): Building = dbQuery {
        val now: Instant = Clock.System.now()
        val id = BuildingsTable.insertAndGetId {
            it[BuildingsTable.agencyId] = agencyId
            it[clientId] = req.clientId?.let { c -> UUID.fromString(c) }
            it[name] = req.name
            it[address] = req.address
            it[suburb] = req.suburb
            it[state] = req.state
            it[postcode] = req.postcode
            it[country] = req.country
            it[buildingType] = req.buildingType
            it[yearBuilt] = req.yearBuilt
            it[notes] = req.notes
            it[createdAt] = now
            it[updatedAt] = now
        }
        BuildingsTable.selectAll().where { BuildingsTable.id eq id }.single().toBuilding()
    }

    suspend fun update(agencyId: UUID, buildingId: UUID, req: UpdateBuildingRequest): Building? = dbQuery {
        val existing = BuildingsTable.selectAll()
            .where { BuildingsTable.id eq buildingId and (BuildingsTable.agencyId eq agencyId) and (BuildingsTable.isActive eq true) }
            .singleOrNull() ?: return@dbQuery null

        val now: Instant = Clock.System.now()
        val reqName = req.name
        val reqAddress = req.address
        BuildingsTable.update({ BuildingsTable.id eq buildingId }) {
            if (reqName != null) it[name] = reqName
            if (reqAddress != null) it[address] = reqAddress
            it[updatedAt] = now
        }
        BuildingsTable
            .join(ClientsTable, JoinType.LEFT, BuildingsTable.clientId, ClientsTable.id)
            .selectAll()
            .where { BuildingsTable.id eq buildingId }
            .single()
            .toBuilding(withClient = true)
    }

    suspend fun delete(agencyId: UUID, buildingId: UUID): Boolean = dbQuery {
        val unitIds = UnitsTable.selectAll()
            .where { UnitsTable.buildingId eq buildingId }
            .map { it[UnitsTable.id].value }

        if (unitIds.isNotEmpty()) {
            val hasActiveLeases = LeasesTable.selectAll().where {
                (LeasesTable.unitId inList unitIds) and
                (LeasesTable.status inList listOf(LeaseStatus.ACTIVE, LeaseStatus.PERIODIC))
            }.count() > 0
            if (hasActiveLeases) return@dbQuery false
        }

        val now: Instant = Clock.System.now()
        BuildingsTable.update({ BuildingsTable.id eq buildingId and (BuildingsTable.agencyId eq agencyId) }) {
            it[isActive] = false
            it[updatedAt] = now
        }
        true
    }

    suspend fun summary(agencyId: UUID, buildingId: UUID): BuildingSummaryData = dbQuery {
        val units = UnitsTable.selectAll().where { UnitsTable.buildingId eq buildingId }.toList()
        val total = units.size
        val occupied = units.count { it[UnitsTable.status] == UnitStatus.OCCUPIED }
        val vacant = units.count { it[UnitsTable.status] == UnitStatus.VACANT }
        val monthlyIncome = units
            .filter { it[UnitsTable.status] == UnitStatus.OCCUPIED }
            .sumOf { it[UnitsTable.rentAmount]?.toDouble() ?: 0.0 }
        BuildingSummaryData(total, occupied, vacant, if (total > 0) (occupied.toDouble() / total) * 100 else 0.0, monthlyIncome)
    }

    private fun ResultRow.toBuilding(withClient: Boolean = false): Building {
        val clientSummary = if (withClient && this.getOrNull(ClientsTable.id) != null) {
            ClientSummary(
                id = this[ClientsTable.id].value.toString(),
                fullName = this[ClientsTable.fullName],
                email = this[ClientsTable.email]
            )
        } else null

        return Building(
            id = this[BuildingsTable.id].value.toString(),
            agencyId = this[BuildingsTable.agencyId].value.toString(),
            clientId = this[BuildingsTable.clientId]?.value?.toString(),
            name = this[BuildingsTable.name],
            address = this[BuildingsTable.address],
            suburb = this[BuildingsTable.suburb],
            state = this[BuildingsTable.state],
            postcode = this[BuildingsTable.postcode],
            country = this[BuildingsTable.country],
            buildingType = this[BuildingsTable.buildingType],
            yearBuilt = this[BuildingsTable.yearBuilt],
            notes = this[BuildingsTable.notes],
            isActive = this[BuildingsTable.isActive],
            createdAt = this[BuildingsTable.createdAt].toString(),
            updatedAt = this[BuildingsTable.updatedAt].toString(),
            client = clientSummary
        )
    }
}
