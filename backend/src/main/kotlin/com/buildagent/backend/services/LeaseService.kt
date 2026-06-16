package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.extensions.toLease
import com.buildagent.backend.db.tables.*
import com.buildagent.shared.domain.LeaseUtils
import com.buildagent.shared.models.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.util.UUID

class LeaseService {

    suspend fun listLeases(agencyId: String, page: Int = 1, limit: Int = 20): Pair<List<Lease>, Int> = dbQuery {
        val offset = ((page - 1) * limit).toLong()
        val query = LeasesTable
            .innerJoin(UnitsTable)
            .innerJoin(BuildingsTable)
            .selectAll()
            .where { BuildingsTable.agencyId eq UUID.fromString(agencyId) }

        val total = query.count().toInt()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val leases = query
            .orderBy(LeasesTable.createdAt, SortOrder.DESC)
            .limit(limit, offset)
            .map {
                val lease = it.toLease()
                val daysUntilEnd = lease.endDate?.let { d ->
                    (LocalDate.parse(d).toEpochDays() - today.toEpochDays()).toInt()
                }
                lease.copy(computedStatus = LeaseUtils.computeStatus(lease.status, lease.endDate, daysUntilEnd))
            }
        leases to total
    }

    suspend fun getLease(agencyId: String, leaseId: String): Lease? = dbQuery {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val row = LeasesTable
            .innerJoin(UnitsTable)
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (LeasesTable.id eq UUID.fromString(leaseId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .firstOrNull() ?: return@dbQuery null

        val lease = row.toLease()
        val daysUntilEnd = lease.endDate?.let { d ->
            (LocalDate.parse(d).toEpochDays() - today.toEpochDays()).toInt()
        }
        lease.copy(computedStatus = LeaseUtils.computeStatus(lease.status, lease.endDate, daysUntilEnd))
    }

    suspend fun createLease(agencyId: String, request: CreateLeaseRequest): Lease = dbQuery {
        UnitsTable
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (UnitsTable.id eq UUID.fromString(request.unitId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .firstOrNull() ?: error("Unit not found")

        val now: Instant = Clock.System.now()
        val id = LeasesTable.insertAndGetId {
            it[unitId] = UUID.fromString(request.unitId)
            it[tenantId] = UUID.fromString(request.tenantId)
            it[startDate] = LocalDate.parse(request.startDate)
            it[endDate] = request.endDate?.let { d -> LocalDate.parse(d) }
            it[rentAmount] = request.rentAmount.toBigDecimal()
            it[rentFrequency] = request.rentFrequency
            it[bondAmount] = request.bondAmount.toBigDecimal()
            it[paymentDay] = request.paymentDay
            it[specialConditions] = request.specialConditions
            it[moveInDate] = request.moveInDate?.let { d -> LocalDate.parse(d) }
            it[status] = LeaseStatus.ACTIVE
            it[createdAt] = now
            it[updatedAt] = now
        }

        UnitsTable.update({ UnitsTable.id eq UUID.fromString(request.unitId) }) {
            it[status] = UnitStatus.OCCUPIED
            it[updatedAt] = now
        }

        LeasesTable.selectAll().where { LeasesTable.id eq id }.first().toLease()
    }

    suspend fun terminateLease(agencyId: String, leaseId: String, moveOutDate: String): Lease = dbQuery {
        val lease = LeasesTable
            .innerJoin(UnitsTable)
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (LeasesTable.id eq UUID.fromString(leaseId)) and
                (BuildingsTable.agencyId eq UUID.fromString(agencyId))
            }
            .firstOrNull()?.toLease() ?: error("Lease not found")

        val now: Instant = Clock.System.now()
        LeasesTable.update({ LeasesTable.id eq UUID.fromString(leaseId) }) {
            it[status] = LeaseStatus.VACATED
            it[LeasesTable.moveOutDate] = LocalDate.parse(moveOutDate)
            it[updatedAt] = now
        }
        UnitsTable.update({ UnitsTable.id eq UUID.fromString(lease.unitId) }) {
            it[status] = UnitStatus.VACANT
            it[updatedAt] = now
        }

        LeasesTable.selectAll().where { LeasesTable.id eq UUID.fromString(leaseId) }.first().toLease()
    }

    suspend fun getExpiringLeases(agencyId: String, withinDays: Int = 60): List<Lease> = dbQuery {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val cutoff = today.plus(withinDays, DateTimeUnit.DAY)
        LeasesTable
            .innerJoin(UnitsTable)
            .innerJoin(BuildingsTable)
            .selectAll()
            .where {
                (BuildingsTable.agencyId eq UUID.fromString(agencyId)) and
                (LeasesTable.status inList listOf(LeaseStatus.ACTIVE, LeaseStatus.PERIODIC)) and
                (LeasesTable.endDate lessEq cutoff) and
                (LeasesTable.endDate greaterEq today)
            }
            .map { it.toLease() }
    }
}
