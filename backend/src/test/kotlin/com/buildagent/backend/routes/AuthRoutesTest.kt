package com.buildagent.backend.routes

import com.buildagent.backend.TEST_AGENCY_ID
import com.buildagent.backend.TEST_USER_ID
import com.buildagent.backend.configureTestRoutes
import com.buildagent.backend.jsonClient
import com.buildagent.backend.services.AuthService
import com.buildagent.backend.testToken
import com.buildagent.shared.models.AuthResponse
import com.buildagent.shared.models.AuthUser
import com.buildagent.shared.models.LoginRequest
import com.buildagent.shared.models.RegisterRequest
import com.buildagent.shared.models.UserType
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val fakeAuthUser = AuthUser(
        id = TEST_USER_ID,
        agencyId = TEST_AGENCY_ID,
        email = "admin@test.com",
        fullName = "Admin User",
        role = "ADMIN",
        userType = "ADMIN"
    )

    private val fakeAuthResult = AuthResponse(token = "mock-jwt-token", user = fakeAuthUser)

    @Test
    fun `POST login returns 200 with token`() = testApplication {
        val service = mockk<AuthService>()
        coEvery { service.login(any()) } returns fakeAuthResult
        configureTestRoutes { authRoutes(service) }

        val client = jsonClient()
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(LoginRequest("admin@test.com", "Secret123!")))
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("mock-jwt-token"))
    }

    @Test
    fun `POST register returns 201`() = testApplication {
        val service = mockk<AuthService>()
        coEvery { service.register(any()) } returns fakeAuthResult
        configureTestRoutes { authRoutes(service) }

        val client = jsonClient()
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    RegisterRequest(
                        userType = UserType.AGENCY,
                        agencyName = "Test Agency",
                        fullName = "Admin",
                        email = "admin@test.com",
                        password = "Secret123!"
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `GET me returns user when authenticated`() = testApplication {
        val service = mockk<AuthService>()
        coEvery { service.me(TEST_USER_ID) } returns fakeAuthUser
        configureTestRoutes { authRoutes(service) }

        val client = jsonClient()
        val response = client.get("/api/v1/auth/me") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("admin@test.com"))
    }

    @Test
    fun `GET me returns 401 without token`() = testApplication {
        val service = mockk<AuthService>()
        configureTestRoutes { authRoutes(service) }

        val response = client.get("/api/v1/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST login returns 400 for invalid credentials`() = testApplication {
        val service = mockk<AuthService>()
        coEvery { service.login(any()) } throws IllegalArgumentException("Invalid credentials")
        configureTestRoutes { authRoutes(service) }

        val client = jsonClient()
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(LoginRequest("bad@test.com", "wrong")))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
