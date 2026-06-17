package com.buildagent.backend.services

import com.buildagent.backend.TestDatabase
import com.buildagent.backend.TestFixtures
import com.buildagent.backend.db.tables.UsersTable
import com.buildagent.shared.models.CreateTenantRequest
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
}
