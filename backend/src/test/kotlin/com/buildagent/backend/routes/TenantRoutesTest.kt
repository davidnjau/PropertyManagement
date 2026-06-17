package com.buildagent.backend.routes

import com.buildagent.backend.TEST_AGENCY_ID
import com.buildagent.backend.configureTestRoutes
import com.buildagent.backend.jsonClient
import com.buildagent.backend.services.TenantService
import com.buildagent.backend.testToken
import com.buildagent.shared.models.*
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

class TenantRoutesTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val fakeTenant = Tenant(
        id = "bbbbbbbb-0000-0000-0000-000000000001",
        agencyId = TEST_AGENCY_ID,
        fullName = "Alice Tenant",
        email = "alice@tenant.com",
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )

    @Test
    fun `GET tenants returns list`() = testApplication {
        val service = mockk<TenantService>()
        coEvery { service.listTenants(any(), any(), any()) } returns (listOf(fakeTenant) to 1)
        configureTestRoutes { tenantRoutes(service) }

        val client = jsonClient()
        val response = client.get("/api/v1/tenants") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Alice Tenant"))
    }

    @Test
    fun `GET tenants returns 401 without token`() = testApplication {
        val service = mockk<TenantService>()
        configureTestRoutes { tenantRoutes(service) }

        val response = client.get("/api/v1/tenants")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST tenants creates tenant and returns 201`() = testApplication {
        val service = mockk<TenantService>()
        coEvery { service.createTenant(any(), any()) } returns fakeTenant
        configureTestRoutes { tenantRoutes(service) }

        val client = jsonClient()
        val response = client.post("/api/v1/tenants") {
            bearerAuth(testToken())
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    CreateTenantRequest(
                        fullName = "Alice Tenant",
                        email = "alice@tenant.com"
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("alice@tenant.com"))
    }

    @Test
    fun `GET tenant by id returns 404 when not found`() = testApplication {
        val service = mockk<TenantService>()
        coEvery { service.getTenantEnriched(any(), any()) } returns null
        configureTestRoutes { tenantRoutes(service) }

        val client = jsonClient()
        val response = client.get("/api/v1/tenants/00000000-0000-0000-0000-000000000000") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE tenant returns 409 when active lease exists`() = testApplication {
        val service = mockk<TenantService>()
        coEvery { service.deleteTenant(any(), any()) } returns false
        configureTestRoutes { tenantRoutes(service) }

        val client = jsonClient()
        val response = client.delete("/api/v1/tenants/bbbbbbbb-0000-0000-0000-000000000001") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `DELETE tenant returns 204 on success`() = testApplication {
        val service = mockk<TenantService>()
        coEvery { service.deleteTenant(any(), any()) } returns true
        configureTestRoutes { tenantRoutes(service) }

        val client = jsonClient()
        val response = client.delete("/api/v1/tenants/bbbbbbbb-0000-0000-0000-000000000001") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.NoContent, response.status)
    }
}
