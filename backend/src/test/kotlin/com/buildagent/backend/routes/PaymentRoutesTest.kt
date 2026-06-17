package com.buildagent.backend.routes

import com.buildagent.backend.TEST_AGENCY_ID
import com.buildagent.backend.configureTestRoutes
import com.buildagent.backend.jsonClient
import com.buildagent.backend.services.PaymentService
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

class PaymentRoutesTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val fakePayment = Payment(
        id = "cccccccc-0000-0000-0000-000000000001",
        leaseId = "dddddddd-0000-0000-0000-000000000001",
        agencyId = TEST_AGENCY_ID,
        amount = 1500.0,
        paymentType = PaymentType.RENT,
        status = PaymentStatus.RECEIVED,
        periodFrom = "2025-01-01",
        periodTo = "2025-01-31",
        isAdjustment = false,
        voided = false,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )

    @Test
    fun `GET payments returns list`() = testApplication {
        val service = mockk<PaymentService>()
        coEvery { service.list(any(), any(), any(), any()) } returns (listOf(fakePayment) to 1)
        configureTestRoutes { paymentRoutes(service) }

        val client = jsonClient()
        val response = client.get("/api/v1/payments") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("cccccccc"))
    }

    @Test
    fun `GET payments returns 401 without token`() = testApplication {
        val service = mockk<PaymentService>()
        configureTestRoutes { paymentRoutes(service) }

        val response = client.get("/api/v1/payments")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST payments records payment and returns 201`() = testApplication {
        val service = mockk<PaymentService>()
        coEvery { service.record(any(), any(), any()) } returns fakePayment
        configureTestRoutes { paymentRoutes(service) }

        val client = jsonClient()
        val response = client.post("/api/v1/payments") {
            bearerAuth(testToken())
            contentType(ContentType.Application.Json)
            setBody(
                json.encodeToString(
                    RecordPaymentRequest(
                        leaseId = "dddddddd-0000-0000-0000-000000000001",
                        amount = 1500.0,
                        paymentType = PaymentType.RENT,
                        status = PaymentStatus.RECEIVED,
                        periodFrom = "2025-01-01",
                        periodTo = "2025-01-31"
                    )
                )
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("1500"))
    }

    @Test
    fun `GET payment by id returns 404 when not found`() = testApplication {
        val service = mockk<PaymentService>()
        coEvery { service.getById(any(), any()) } returns null
        configureTestRoutes { paymentRoutes(service) }

        val client = jsonClient()
        val response = client.get("/api/v1/payments/00000000-0000-0000-0000-000000000000") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE payment voids and returns 204`() = testApplication {
        val service = mockk<PaymentService>()
        coEvery { service.voidPayment(any(), any(), any()) } returns true
        configureTestRoutes { paymentRoutes(service) }

        val client = jsonClient()
        val response = client.delete("/api/v1/payments/cccccccc-0000-0000-0000-000000000001") {
            bearerAuth(testToken())
        }
        assertEquals(HttpStatusCode.NoContent, response.status)
    }
}
