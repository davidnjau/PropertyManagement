package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.extensions.toLease
import com.buildagent.backend.db.extensions.toUnit
import com.buildagent.backend.db.tables.BuildingsTable
import com.buildagent.backend.db.tables.LeasesTable
import com.buildagent.backend.db.tables.TenantsTable
import com.buildagent.backend.db.tables.UnitsTable
import com.buildagent.shared.models.BuildingUnit
import com.buildagent.shared.models.CreateUnitRequest
import com.buildagent.shared.models.LeaseStatus
import com.buildagent.shared.models.TenantSummary
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.UUID

class UnitService {

    suspend fun listUnits(agencyId: String, buildingId: String): List<BuildingUnit> = dbQuery {
        val units = UnitsTable
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (UnitsTable.buildingId eq UUID.fromString(buildingId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .orderBy(UnitsTable.unitNumber, SortOrder.ASC)
            .map { it.toUnit() }

        if (units.isEmpty()) return@dbQuery units

        val unitIds = units.map { UUID.fromString(it.id) }
        val activeStatuses = listOf(LeaseStatus.ACTIVE, LeaseStatus.PERIODIC, LeaseStatus.EXPIRING_SOON)
        val leasesByUnit = LeasesTable
            .join(TenantsTable, JoinType.LEFT, LeasesTable.tenantId, TenantsTable.id)
            .selectAll()
            .where {
                (LeasesTable.unitId inList unitIds) and
                (LeasesTable.status inList activeStatuses)
            }
            .map { row ->
                row.toLease().copy(
                    tenant = TenantSummary(
                        id = row[TenantsTable.id].value.toString(),
                        fullName = row[TenantsTable.fullName],
                        email = row[TenantsTable.email],
                        phone = row[TenantsTable.phone]
                    )
                )
            }
            .groupBy { it.unitId }

        units.map { unit -> unit.copy(leases = leasesByUnit[unit.id] ?: emptyList()) }
    }

    suspend fun getUnit(agencyId: String, unitId: String): BuildingUnit? = dbQuery {
        val unit = UnitsTable
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (UnitsTable.id eq UUID.fromString(unitId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .firstOrNull()
            ?.toUnit() ?: return@dbQuery null

        val leases = LeasesTable
            .join(TenantsTable, JoinType.LEFT, LeasesTable.tenantId, TenantsTable.id)
            .selectAll()
            .where { LeasesTable.unitId eq UUID.fromString(unitId) }
            .map { row ->
                row.toLease().copy(
                    tenant = TenantSummary(
                        id = row[TenantsTable.id].value.toString(),
                        fullName = row[TenantsTable.fullName],
                        email = row[TenantsTable.email],
                        phone = row[TenantsTable.phone]
                    )
                )
            }

        unit.copy(leases = leases)
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
