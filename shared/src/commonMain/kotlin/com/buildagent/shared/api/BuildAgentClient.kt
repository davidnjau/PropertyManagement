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

    suspend fun createTenantWithLease(request: CreateTenantWithLeaseRequest): ApiResponse<TenantWithLeaseResponse> =
        http.post(url("/api/v1/tenants/with-lease")) {
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

    // ── Auth ─────────────────────────────────────────────────────────────────

    suspend fun signIn(email: String, password: String, role: String? = null): ApiResponse<AuthResponse> =
        http.post(url("/api/v1/auth/signin")) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password, role))
        }.body()

    suspend fun signUp(request: RegisterRequest): ApiResponse<AuthResponse> =
        http.post(url("/api/v1/auth/signup")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun forgotPassword(email: String): ApiResponse<Unit> =
        http.post(url("/api/v1/auth/forgot-password")) {
            contentType(ContentType.Application.Json)
            setBody(ForgotPasswordRequest(email))
        }.body()

    suspend fun verifyOtp(email: String, otp: String): ApiResponse<Unit> =
        http.post(url("/api/v1/auth/verify-otp")) {
            contentType(ContentType.Application.Json)
            setBody(VerifyOtpRequest(email, otp))
        }.body()

    // ── Admin Users ───────────────────────────────────────────────────────────

    suspend fun getAdminUsers(): ApiResponse<List<AdminUserResponse>> =
        http.get(url("/api/v1/admin/users")) { auth(this) }.body()

    suspend fun createAdminUser(request: CreateUserRequest): ApiResponse<AdminUserResponse> =
        http.post(url("/api/v1/admin/users")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Admin Alerts ──────────────────────────────────────────────────────────

    suspend fun getAlerts(): ApiResponse<List<Alert>> =
        http.get(url("/api/v1/admin/alerts")) { auth(this) }.body()

    suspend fun createAlert(request: CreateAlertRequest): ApiResponse<Alert> =
        http.post(url("/api/v1/admin/alerts")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Admin Documents ───────────────────────────────────────────────────────

    suspend fun getAdminDocuments(entityType: String? = null): ApiResponse<List<Document>> =
        http.get(url("/api/v1/admin/documents")) {
            auth(this)
            entityType?.let { parameter("entityType", it) }
        }.body()

    suspend fun deleteAdminDocument(id: String): Unit =
        http.delete(url("/api/v1/admin/documents/$id")) { auth(this) }.body()

    // ── Admin Payment Methods ─────────────────────────────────────────────────

    suspend fun getPaymentMethods(): ApiResponse<PaymentMethodsConfig> =
        http.get(url("/api/v1/admin/payment-methods")) { auth(this) }.body()

    suspend fun togglePaymentMethod(id: String, enabled: Boolean): Unit =
        http.put(url("/api/v1/admin/payment-methods/$id/toggle")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(ToggleMethodRequest(enabled))
        }.body()

    suspend fun toggleBank(bankId: String, enabled: Boolean): Unit =
        http.put(url("/api/v1/admin/payment-methods/bank/$bankId/toggle")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(ToggleMethodRequest(enabled))
        }.body()

    suspend fun updateMpesaConfig(request: UpdateMpesaConfigRequest): Unit =
        http.put(url("/api/v1/admin/payment-methods/mpesa")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updatePaypalConfig(request: UpdatePaypalConfigRequest): Unit =
        http.put(url("/api/v1/admin/payment-methods/paypal")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ── Admin Lease Extensions ────────────────────────────────────────────────

    suspend fun getLeaseExtensions(): ApiResponse<List<LeaseExtensionRequest>> =
        http.get(url("/api/v1/admin/lease-extension-requests")) { auth(this) }.body()

    suspend fun resolveLeaseExtension(id: String, status: String, agentNotes: String? = null): ApiResponse<LeaseExtensionRequest> =
        http.patch(url("/api/v1/admin/lease-extension-requests/$id")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(ResolveLeaseExtensionRequest(status, agentNotes))
        }.body()

    // ── Tenant Portal ─────────────────────────────────────────────────────────

    suspend fun getTenantOverview(): ApiResponse<TenantOverview> =
        http.get(url("/api/v1/tenant/overview")) { auth(this) }.body()

    suspend fun getTenantLease(): ApiResponse<Lease> =
        http.get(url("/api/v1/tenant/lease")) { auth(this) }.body()

    suspend fun getTenantPayments(): ApiResponse<List<Payment>> =
        http.get(url("/api/v1/tenant/payments")) { auth(this) }.body()

    suspend fun getTenantMaintenance(): ApiResponse<List<MaintenanceRequest>> =
        http.get(url("/api/v1/tenant/maintenance")) { auth(this) }.body()

    suspend fun createTenantMaintenance(request: CreateMaintenanceRequest): ApiResponse<MaintenanceRequest> =
        http.post(url("/api/v1/tenant/maintenance")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getTenantDocuments(): ApiResponse<List<Document>> =
        http.get(url("/api/v1/tenant/documents")) { auth(this) }.body()

    suspend fun getTenantPaymentMethods(): ApiResponse<PaymentMethodsConfig> =
        http.get(url("/api/v1/tenant/payment-methods")) { auth(this) }.body()

    suspend fun submitLeaseExtension(request: CreateLeaseExtensionRequest): ApiResponse<LeaseExtensionRequest> =
        http.post(url("/api/v1/tenant/lease/extension-request")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun recordTenantPayment(request: RecordPaymentRequest): ApiResponse<Payment> =
        http.post(url("/api/v1/tenant/payments")) {
            auth(this)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
