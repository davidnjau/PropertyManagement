package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.extensions.toUnit
import com.buildagent.backend.db.tables.BuildingsTable
import com.buildagent.backend.db.tables.UnitsTable
import com.buildagent.shared.models.BuildingUnit
import com.buildagent.shared.models.CreateUnitRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class UnitService {

    suspend fun listUnits(agencyId: String, buildingId: String): List<BuildingUnit> = dbQuery {
        UnitsTable
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (UnitsTable.buildingId eq UUID.fromString(buildingId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .orderBy(UnitsTable.unitNumber, SortOrder.ASC)
            .map { it.toUnit() }
    }

    suspend fun getUnit(agencyId: String, unitId: String): BuildingUnit? = dbQuery {
        UnitsTable
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (UnitsTable.id eq UUID.fromString(unitId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .firstOrNull()
            ?.toUnit()
    }

    suspend fun createUnit(agencyId: String, buildingId: String, request: CreateUnitRequest): BuildingUnit = dbQuery {
        BuildingsTable.selectAll().where {
            (BuildingsTable.id eq UUID.fromString(buildingId)) and
            (BuildingsTable.agencyId eq UUID.fromString(agencyId))
        }.firstOrNull() ?: error("Building not found")

        val now: Instant = Clock.System.now()
        val id = UnitsTable.insertAndGetId {
            it[UnitsTable.buildingId] = UUID.fromString(buildingId)
            it[unitNumber] = request.unitNumber
            it[floor] = request.floor
            it[bedrooms] = request.bedrooms
            it[bathrooms] = request.bathrooms
            it[parkingSpaces] = request.parkingSpaces
            it[areaSqm] = request.areaSqm?.toBigDecimal()
            it[rentAmount] = request.rentAmount?.toBigDecimal()
            it[rentFrequency] = request.rentFrequency
            it[notes] = request.notes
            it[createdAt] = now
            it[updatedAt] = now
        }

        UnitsTable.selectAll().where { UnitsTable.id eq id }.first().toUnit()
    }

    suspend fun updateUnit(agencyId: String, unitId: String, request: CreateUnitRequest): BuildingUnit = dbQuery {
        UnitsTable
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (UnitsTable.id eq UUID.fromString(unitId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .firstOrNull() ?: error("Unit not found")

        val nowUpdate: Instant = Clock.System.now()
        UnitsTable.update({ UnitsTable.id eq UUID.fromString(unitId) }) {
            it[unitNumber] = request.unitNumber
            request.floor?.let { v -> it[floor] = v }
            it[bedrooms] = request.bedrooms
            it[bathrooms] = request.bathrooms
            it[parkingSpaces] = request.parkingSpaces
            it[areaSqm] = request.areaSqm?.toBigDecimal()
            it[rentAmount] = request.rentAmount?.toBigDecimal()
            it[rentFrequency] = request.rentFrequency
            request.notes?.let { v -> it[notes] = v }
            it[updatedAt] = nowUpdate
        }

        UnitsTable.selectAll().where { UnitsTable.id eq UUID.fromString(unitId) }.first().toUnit()
    }
}
