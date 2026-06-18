package com.buildagent.backend.services

import com.buildagent.backend.TestDatabase
import com.buildagent.backend.TestFixtures
import com.buildagent.backend.db.tables.UsersTable
import com.buildagent.backend.db.tables.LeasesTable
import com.buildagent.backend.db.tables.UserCredentialsTable
import com.buildagent.shared.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenantServiceTest {

    private lateinit var service: TenantService

    @BeforeAll
    fun setUpDatabase() {
        TestDatabase.init()
    }

    @BeforeEach
    fun setUp() {
        TestDatabase.resetAll()
        service = TenantService()
    }

    @Test
    fun `createTenant creates tenant record`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val req = CreateTenantRequest(
            fullName = "Alice Tenant",
            email = "alice@tenant.com",
            phone = "0400000001"
        )
        val tenant = service.createTenant(agencyId, req)
        assertEquals("Alice Tenant", tenant.fullName)
        assertEquals("alice@tenant.com", tenant.email)
        assertNotNull(tenant.id)
    }

    @Test
    fun `createTenant with password creates user credentials`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val req = CreateTenantRequest(
            fullName = "Bob Tenant",
            email = "bob@tenant.com",
            password = "BobPass123!"
        )
        service.createTenant(agencyId, req)

        val userExists = transaction {
            UsersTable.selectAll()
                .where { UsersTable.email eq "bob@tenant.com" }
                .firstOrNull()
        }
        assertNotNull(userExists)
        assertEquals("bob@tenant.com", userExists[UsersTable.email])
    }

    @Test
    fun `createTenant without password does not create user record`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val req = CreateTenantRequest(
            fullName = "Carol Tenant",
            email = "carol@tenant.com"
        )
        service.createTenant(agencyId, req)

        val userExists = transaction {
            UsersTable.selectAll()
                .where { UsersTable.email eq "carol@tenant.com" }
                .firstOrNull()
        }
        assertNull(userExists)
    }

    @Test
    fun `listTenants returns only active tenants for the agency`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        TestFixtures.createTenant(agencyId, email = "t1@test.com")
        TestFixtures.createTenant(agencyId, email = "t2@test.com")

        val (tenants, total) = service.listTenants(agencyId)
        assertEquals(2, tenants.size)
        assertEquals(2, total)
    }

    @Test
    fun `listTenants does not return tenants from another agency`() = runBlocking {
        val agencyId1 = TestFixtures.createAgency(name = "Agency 1", email = "a1@test.com")
        val agencyId2 = TestFixtures.createAgency(name = "Agency 2", email = "a2@test.com")
        TestFixtures.createTenant(agencyId1, email = "tenant1@a1.com")
        TestFixtures.createTenant(agencyId2, email = "tenant1@a2.com")

        val (tenants, _) = service.listTenants(agencyId1)
        assertEquals(1, tenants.size)
        assertEquals("tenant1@a1.com", tenants.first().email)
    }

    @Test
    fun `getTenant returns null for unknown id`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val result = service.getTenant(agencyId, "00000000-0000-0000-0000-000000000000")
        assertNull(result)
    }

    @Test
    fun `deleteTenant soft-deletes when no active lease`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val tenantId = TestFixtures.createTenant(agencyId)
        val deleted = service.deleteTenant(agencyId, tenantId)
        assertTrue(deleted)

        val (tenants, _) = service.listTenants(agencyId)
        assertTrue(tenants.isEmpty())
    }

    // ── createTenantWithLease ─────────────────────────────────────────────────

    private fun makeWithLeaseRequest(unitId: String) = CreateTenantWithLeaseRequest(
        fullName = "Dana Tenant",
        email = "dana@tenant.com",
        phone = "0400000099",
        unitId = unitId,
        startDate = "2025-01-01",
        endDate = "2026-01-01",
        rentAmount = 1800.0,
        rentFrequency = RentFrequency.MONTHLY,
        bondAmount = 3600.0,
        paymentDay = 5
    )

    @Test
    fun `createTenantWithLease creates both tenant and lease records`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)

        val result = service.createTenantWithLease(agencyId, makeWithLeaseRequest(unitId))

        assertEquals("Dana Tenant", result.tenant.fullName)
        assertEquals("dana@tenant.com", result.tenant.email)
        assertNotNull(result.lease.id)
        assertEquals(result.tenant.id, result.lease.tenantId)
        assertEquals(unitId, result.lease.unitId)
        assertEquals(LeaseStatus.ACTIVE, result.lease.status)
    }

    @Test
    fun `createTenantWithLease marks unit as OCCUPIED`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)

        service.createTenantWithLease(agencyId, makeWithLeaseRequest(unitId))

        val unitStatus = transaction {
            com.buildagent.backend.db.tables.UnitsTable.selectAll()
                .where { com.buildagent.backend.db.tables.UnitsTable.id eq java.util.UUID.fromString(unitId) }
                .first()[com.buildagent.backend.db.tables.UnitsTable.status]
        }
        assertEquals(UnitStatus.OCCUPIED, unitStatus)
    }

    @Test
    fun `createTenantWithLease provisions new TENANT user in background`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)

        service.createTenantWithLease(agencyId, makeWithLeaseRequest(unitId))
        delay(200) // allow background coroutine to complete

        val user = transaction {
            UsersTable.selectAll()
                .where { UsersTable.email eq "dana@tenant.com" }
                .firstOrNull()
        }
        assertNotNull(user)
        assertEquals(UserRole.TENANT, user[UsersTable.role])

        val creds = transaction {
            UserCredentialsTable.selectAll()
                .where { UserCredentialsTable.userId eq user[UsersTable.id] }
                .firstOrNull()
        }
        assertNotNull(creds, "Credentials row should be created with OTP hash")
    }

    @Test
    fun `createTenantWithLease does not create duplicate user for existing TENANT`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId1 = TestFixtures.createUnit(buildingId, unitNumber = "1A")
        val unitId2 = TestFixtures.createUnit(buildingId, unitNumber = "1B")

        service.createTenantWithLease(agencyId, makeWithLeaseRequest(unitId1))
        delay(200)

        // Second call with same email, different unit
        service.createTenantWithLease(
            agencyId,
            makeWithLeaseRequest(unitId2).copy(email = "dana@tenant.com", fullName = "Dana Again")
        )
        delay(200)

        val userCount = transaction {
            UsersTable.selectAll()
                .where { UsersTable.email eq "dana@tenant.com" }
                .count()
        }
        assertEquals(1L, userCount, "Should not create a second user record for the same email")
    }

    @Test
    fun `createTenantWithLease throws when unit does not belong to agency`() = runBlocking {
        val agencyId1 = TestFixtures.createAgency(name = "A1", email = "a1@test.com")
        val agencyId2 = TestFixtures.createAgency(name = "A2", email = "a2@test.com")
        val buildingId = TestFixtures.createBuilding(agencyId2)
        val unitId = TestFixtures.createUnit(buildingId)

        try {
            service.createTenantWithLease(agencyId1, makeWithLeaseRequest(unitId))
            assert(false) { "Expected exception" }
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Unit not found"))
        }
    }

    @Test
    fun `createTenantWithLease does not create user for existing AGENT`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)
        // Pre-create a user with AGENT role at the same email
        TestFixtures.createUser(agencyId, email = "agent.tenant@test.com", role = UserRole.AGENT)

        service.createTenantWithLease(
            agencyId,
            makeWithLeaseRequest(unitId).copy(email = "agent.tenant@test.com")
        )
        delay(200)

        // Role should remain AGENT — no change
        val user = transaction {
            UsersTable.selectAll()
                .where { UsersTable.email eq "agent.tenant@test.com" }
                .single()
        }
        assertEquals(UserRole.AGENT, user[UsersTable.role])

        val userCount = transaction {
            UsersTable.selectAll()
                .where { UsersTable.email eq "agent.tenant@test.com" }
                .count()
        }
        assertEquals(1L, userCount, "Should not create a second user for an existing agent")
    }
}
