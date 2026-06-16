package com.buildagent.shared.api

import com.buildagent.shared.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class BuildAgentClient(
    private val baseUrl: String,
    private val tokenProvider: suspend () -> String
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    val http = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(Logging) { level = LogLevel.INFO }
    }

    private fun url(path: String) = "$baseUrl$path"

    private suspend fun auth(builder: HttpRequestBuilder) {
        builder.header(HttpHeaders.Authorization, "Bearer ${tokenProvider()}")
    }

    // ── Dashboard ────────────────────────────────────────────────────────────

    suspend fun getAgentDashboard(): ApiResponse<DashboardData> =
        http.get(url("/api/v1/dashboard/agent")) { auth(this) }.body()

    // ── Buildings ────────────────────────────────────────────────────────────

    suspend fun getBuildings(page: Int = 1, limit: Int = 20): ApiResponse<List<Building>> =
        http.get(url("/api/v1/buildings")) {
            auth(this)
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun getBuilding(id: String): ApiResponse<Building> =
        http.get(url("/api/v1/buildings/$id")) { auth(this) }.body()

    suspend fun getBuildingSummary(id: String): ApiResponse<BuildingSummaryData> =
        http.get(url("/api/v1/buildings/$id/summary")) { auth(this) }.body()

    suspend fun createBuilding(request: CreateBuildingRequest): ApiResponse<Building> =
        http.post(url("/api/v1/buildings")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Units ────────────────────────────────────────────────────────────────

    suspend fun getUnits(buildingId: String): ApiResponse<List<BuildingUnit>> =
        http.get(url("/api/v1/buildings/$buildingId/units")) { auth(this) }.body()

    suspend fun getUnit(id: String): ApiResponse<BuildingUnit> =
        http.get(url("/api/v1/units/$id")) { auth(this) }.body()

    suspend fun createUnit(buildingId: String, request: CreateUnitRequest): ApiResponse<BuildingUnit> =
        http.post(url("/api/v1/buildings/$buildingId/units")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Tenants ──────────────────────────────────────────────────────────────

    suspend fun getTenants(page: Int = 1, limit: Int = 20): ApiResponse<List<Tenant>> =
        http.get(url("/api/v1/tenants")) {
            auth(this)
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun getTenant(id: String): ApiResponse<Tenant> =
        http.get(url("/api/v1/tenants/$id")) { auth(this) }.body()

    suspend fun createTenant(request: CreateTenantRequest): ApiResponse<Tenant> =
        http.post(url("/api/v1/tenants")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Leases ───────────────────────────────────────────────────────────────

    suspend fun getLeases(page: Int = 1, limit: Int = 20): ApiResponse<List<Lease>> =
        http.get(url("/api/v1/leases")) {
            auth(this)
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun getLease(id: String): ApiResponse<Lease> =
        http.get(url("/api/v1/leases/$id")) { auth(this) }.body()

    suspend fun createLease(request: CreateLeaseRequest): ApiResponse<Lease> =
        http.post(url("/api/v1/leases")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun terminateLease(id: String, request: TerminateLeaseRequest): ApiResponse<Lease> =
        http.post(url("/api/v1/leases/$id/terminate")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Payments ─────────────────────────────────────────────────────────────

    suspend fun getPayments(page: Int = 1, limit: Int = 20, leaseId: String? = null): ApiResponse<List<Payment>> =
        http.get(url("/api/v1/payments")) {
            auth(this)
            parameter("page", page)
            parameter("limit", limit)
            leaseId?.let { parameter("leaseId", it) }
        }.body()

    suspend fun getOverduePayments(): ApiResponse<List<Payment>> =
        http.get(url("/api/v1/payments/overdue")) { auth(this) }.body()

    suspend fun recordPayment(request: RecordPaymentRequest): ApiResponse<Payment> =
        http.post(url("/api/v1/payments")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun adjustPayment(id: String, request: AdjustPaymentRequest): ApiResponse<Payment> =
        http.post(url("/api/v1/payments/$id/adjust")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Maintenance ──────────────────────────────────────────────────────────

    suspend fun getMaintenance(status: String? = null, page: Int = 1): ApiResponse<List<MaintenanceRequest>> =
        http.get(url("/api/v1/maintenance")) {
            auth(this)
            parameter("page", page)
            status?.let { parameter("status", it) }
        }.body()

    suspend fun createMaintenance(request: CreateMaintenanceRequest): ApiResponse<MaintenanceRequest> =
        http.post(url("/api/v1/maintenance")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateMaintenance(id: String, request: UpdateMaintenanceRequest): ApiResponse<MaintenanceRequest> =
        http.patch(url("/api/v1/maintenance/$id")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun closeMaintenance(id: String, request: CloseMaintenanceRequest): ApiResponse<MaintenanceRequest> =
        http.post(url("/api/v1/maintenance/$id/close")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
