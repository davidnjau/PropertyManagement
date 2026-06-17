package com.buildagent.backend.services

import com.buildagent.backend.TestDatabase
import com.buildagent.backend.TestFixtures
import com.buildagent.backend.db.tables.UnitsTable
import com.buildagent.shared.models.CreateLeaseRequest
import com.buildagent.shared.models.LeaseStatus
import com.buildagent.shared.models.RentFrequency
import com.buildagent.shared.models.UnitStatus
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaseServiceTest {

    private lateinit var service: LeaseService

    @BeforeAll
    fun setUpDatabase() {
        TestDatabase.init()
    }

    @BeforeEach
    fun setUp() {
        TestDatabase.resetAll()
        service = LeaseService()
    }

    private fun makeLeaseRequest(unitId: String, tenantId: String) = CreateLeaseRequest(
        unitId = unitId,
        tenantId = tenantId,
        startDate = "2025-01-01",
        endDate = "2026-01-01",
        rentAmount = 1500.0,
        rentFrequency = RentFrequency.MONTHLY,
        bondAmount = 3000.0,
        paymentDay = 1
    )

    @Test
    fun `createLease creates lease with ACTIVE status`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)
        val tenantId = TestFixtures.createTenant(agencyId)

        val lease = service.createLease(agencyId, makeLeaseRequest(unitId, tenantId))
        assertEquals(LeaseStatus.ACTIVE, lease.status)
        assertEquals(unitId, lease.unitId)
        assertEquals(tenantId, lease.tenantId)
        assertNotNull(lease.id)
    }

    @Test
    fun `createLease marks unit as OCCUPIED`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)
        val tenantId = TestFixtures.createTenant(agencyId)

        service.createLease(agencyId, makeLeaseRequest(unitId, tenantId))

        val unitStatus = transaction {
            UnitsTable.selectAll()
                .where { UnitsTable.id eq UUID.fromString(unitId) }
                .first()[UnitsTable.status]
        }
        assertEquals(UnitStatus.OCCUPIED, unitStatus)
    }

    @Test
    fun `createLease throws when unit does not belong to agency`() {
        runBlocking {
            val agencyId1 = TestFixtures.createAgency(name = "A1", email = "a1@test.com")
            val agencyId2 = TestFixtures.createAgency(name = "A2", email = "a2@test.com")
            val buildingId = TestFixtures.createBuilding(agencyId2)
            val unitId = TestFixtures.createUnit(buildingId)
            val tenantId = TestFixtures.createTenant(agencyId1)

            try {
                service.createLease(agencyId1, makeLeaseRequest(unitId, tenantId))
                assert(false) { "Expected exception not thrown" }
            } catch (e: IllegalStateException) {
                assertEquals("Unit not found", e.message)
            }
        }
    }

    @Test
    fun `listLeases returns leases for the agency`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId1 = TestFixtures.createUnit(buildingId, unitNumber = "1A")
        val unitId2 = TestFixtures.createUnit(buildingId, unitNumber = "1B")
        val tenantId1 = TestFixtures.createTenant(agencyId, email = "t1@test.com")
        val tenantId2 = TestFixtures.createTenant(agencyId, email = "t2@test.com")

        service.createLease(agencyId, makeLeaseRequest(unitId1, tenantId1))
        service.createLease(agencyId, makeLeaseRequest(unitId2, tenantId2))

        val (leases, total) = service.listLeases(agencyId)
        assertEquals(2, leases.size)
        assertEquals(2, total)
    }

    @Test
    fun `terminateLease sets status to VACATED and unit back to VACANT`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)
        val tenantId = TestFixtures.createTenant(agencyId)

        val lease = service.createLease(agencyId, makeLeaseRequest(unitId, tenantId))
        val terminated = service.terminateLease(agencyId, lease.id, "2025-06-30")

        assertEquals(LeaseStatus.VACATED, terminated.status)

        val unitStatus = transaction {
            UnitsTable.selectAll()
                .where { UnitsTable.id eq UUID.fromString(unitId) }
                .first()[UnitsTable.status]
        }
        assertEquals(UnitStatus.VACANT, unitStatus)
    }

    @Test
    fun `getLease returns null for unknown id`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val result = service.getLease(agencyId, "00000000-0000-0000-0000-000000000000")
        assertNull(result)
    }
}
