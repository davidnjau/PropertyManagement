package com.buildagent.backend.services

import com.buildagent.backend.TestDatabase
import com.buildagent.backend.TestFixtures
import com.buildagent.shared.models.CreateUserRequest
import com.buildagent.shared.models.UserType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminServiceTest {

    private lateinit var service: AdminService

    @BeforeAll
    fun setUpDatabase() {
        TestDatabase.init()
    }

    @BeforeEach
    fun setUp() {
        TestDatabase.resetAll()
        service = AdminService()
    }

    @Test
    fun `createUser creates agent with correct role`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val req = CreateUserRequest(
            userType = UserType.AGENT,
            fullName = "Jane Agent",
            email = "jane@agency.com",
            password = "Secret123!",
            phone = "0400000001"
        )
        val result = service.createUser(agencyId, req)
        assertEquals("jane@agency.com", result.email)
        assertEquals("Jane Agent", result.fullName)
        assertEquals("AGENT", result.role)
        assertEquals("AGENT", result.userType)
        assertNotNull(result.id)
    }

    @Test
    fun `createUser with duplicate email throws exception`() = runBlocking<Unit> {
        val agencyId = TestFixtures.createAgency()
        val req = CreateUserRequest(
            userType = UserType.AGENT,
            fullName = "Dup User",
            email = "dup@agency.com",
            password = "Secret123!"
        )
        service.createUser(agencyId, req)
        assertFailsWith<IllegalArgumentException> {
            service.createUser(agencyId, req)
        }
    }

    @Test
    fun `listUsers returns only users for the given agency`() = runBlocking {
        val agencyId1 = TestFixtures.createAgency(name = "Agency One", email = "a1@test.com")
        val agencyId2 = TestFixtures.createAgency(name = "Agency Two", email = "a2@test.com")
        TestFixtures.createUser(agencyId1, email = "user1@a1.com")
        TestFixtures.createUser(agencyId1, email = "user2@a1.com")
        TestFixtures.createUser(agencyId2, email = "user1@a2.com")

        val users = service.listUsers(agencyId1)
        assertEquals(2, users.size)
        assertTrue(users.all { it.agencyId == agencyId1 })
    }

    @Test
    fun `listUsers returns empty list when no users exist`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val users = service.listUsers(agencyId)
        assertTrue(users.isEmpty())
    }

    @Test
    fun `createUser sets isActive to true`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val result = service.createUser(
            agencyId,
            CreateUserRequest(
                userType = UserType.AGENT,
                fullName = "Active User",
                email = "active@test.com",
                password = "pass"
            )
        )
        assertTrue(result.isActive)
    }
}
