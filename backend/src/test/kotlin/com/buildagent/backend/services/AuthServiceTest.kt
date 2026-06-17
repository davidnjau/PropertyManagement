package com.buildagent.backend.services

import com.buildagent.backend.TestDatabase
import com.buildagent.backend.auth.LocalJwtService
import com.buildagent.shared.models.RegisterRequest
import com.buildagent.shared.models.LoginRequest
import com.buildagent.shared.models.UserType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceTest {

    private val jwtService = LocalJwtService("test-secret-key-for-auth-service")
    private lateinit var service: AuthService

    @BeforeAll
    fun setUpDatabase() {
        TestDatabase.init()
    }

    @BeforeEach
    fun setUp() {
        TestDatabase.resetAll()
        service = AuthService(jwtService)
    }

    @Test
    fun `register as AGENCY creates agency, user, and returns token`() = runBlocking {
        val req = RegisterRequest(
            userType = UserType.AGENCY,
            agencyName = "Test Agency",
            fullName = "Admin User",
            email = "admin@test.com",
            password = "Secret123!"
        )
        val result = service.register(req)
        assertNotNull(result.token)
        assertEquals("admin@test.com", result.user.email)
        assertEquals("ADMIN", result.user.role)
        assertNotNull(result.user.agencyId)
    }

    @Test
    fun `register with duplicate email throws exception`() = runBlocking<Unit> {
        val req = RegisterRequest(
            userType = UserType.AGENCY,
            agencyName = "Agency",
            fullName = "User",
            email = "dup@test.com",
            password = "password"
        )
        service.register(req)
        assertFailsWith<IllegalArgumentException> {
            service.register(req)
        }
    }

    @Test
    fun `register as AGENT requires agencyId`() {
        assertFailsWith<IllegalArgumentException> {
            runBlocking {
                service.register(
                    RegisterRequest(
                        userType = UserType.AGENT,
                        fullName = "Agent",
                        email = "agent@test.com",
                        password = "password"
                        // no agencyId
                    )
                )
            }
        }
    }

    @Test
    fun `login with correct credentials returns token`() = runBlocking {
        val email = "login@test.com"
        val password = "ValidPass1!"
        service.register(
            RegisterRequest(UserType.AGENCY, agencyName = "Login Corp", fullName = "Login User", email = email, password = password)
        )
        val result = service.login(LoginRequest(email, password))
        assertNotNull(result.token)
        assertEquals(email, result.user.email)
    }

    @Test
    fun `login with wrong password throws exception`() = runBlocking<Unit> {
        val email = "wrongpw@test.com"
        service.register(
            RegisterRequest(UserType.AGENCY, agencyName = "WP Corp", fullName = "WP User", email = email, password = "correct")
        )
        assertFailsWith<IllegalArgumentException> {
            service.login(LoginRequest(email, "wrong"))
        }
    }

    @Test
    fun `login with non-existent email throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            runBlocking { service.login(LoginRequest("nobody@test.com", "pass")) }
        }
    }

    @Test
    fun `me returns user info for valid userId`() = runBlocking {
        val registered = service.register(
            RegisterRequest(UserType.AGENCY, agencyName = "Me Corp", fullName = "Me User", email = "me@test.com", password = "pass")
        )
        val user = service.me(registered.user.id)
        assertEquals("me@test.com", user.email)
        assertEquals("Me User", user.fullName)
    }
}
