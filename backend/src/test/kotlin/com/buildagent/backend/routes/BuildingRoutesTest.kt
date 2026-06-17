package com.buildagent.backend.routes

import com.buildagent.backend.TEST_AGENCY_ID
import com.buildagent.backend.configureTestRoutes
import com.buildagent.backend.jsonClient
import com.buildagent.backend.services.BuildingService
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

class BuildingRoutesTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val fakeBuilding = Building(
        id = "aaaaaaaa-0000-0000-0000-000000000001",
        agencyId = TEST_AGENCY_ID,
        name = "Test Tower",
        address = "123 Main St",
        suburb = "CBD",
        state = "VIC",
        postcode = "3000",
        country = "Australia",
        buildingType = BuildingType.RESIDENTIAL,
        unitCount = 0,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )

    @Test
    fun `GET buildings returns list`() = testApplication {
        val service = mockk<BuildingService>()
        coEvery { service.list(any(), any(), any()) } returns (listOf(fakeBuilding) to 1)
        configureTestRoutes { buildingRoutes(service) }

        val client = jsonClient()
        val response = client.get("/api/v1/buildings") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test Tower"))
    }

    @Test
    fun `GET buildings returns 401 without token`() = testApplication {
        val service = mockk<BuildingService>()
        configureTestRoutes { buildingRoutes(service) }

        val response = client.get("/api/v1/buildings")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST buildings creates building and returns 201`() = testApplication {
        val service = mockk<BuildingService>()
        coEvery { service.create(any(), any()) } returns fakeBuilding
        configureTestRoutes { buildingRoutes(service) }

        val client = jsonClient()
        val response = client.post("/api/v1/buildings") {
            bearerAuth(testToken())
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    CreateBuildingRequest(
                        name = "Test Tower",
                        address = "123 Main St",
                        suburb = "CBD",
                        state = "VIC",
                        postcode = "3000",
                        country = "Australia",
                        buildingType = BuildingType.RESIDENTIAL
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("Test Tower"))
    }

    @Test
    fun `GET building by id returns 404 when not found`() = testApplication {
        val service = mockk<BuildingService>()
        coEvery { service.get(any(), any()) } returns null
        configureTestRoutes { buildingRoutes(service) }

        val client = jsonClient()
        val response = client.get("/api/v1/buildings/00000000-0000-0000-0000-000000000000") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE building returns 409 when active leases exist`() = testApplication {
        val service = mockk<BuildingService>()
        coEvery { service.delete(any(), any()) } returns false
        configureTestRoutes { buildingRoutes(service) }

        val client = jsonClient()
        val response = client.delete("/api/v1/buildings/aaaaaaaa-0000-0000-0000-000000000001") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }
}
