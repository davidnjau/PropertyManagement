package com.buildagent.shared.models

import kotlinx.serialization.Serializable

// ── Requests ────────────────────────────────────────────────────────────────

@Serializable
data class CreateBuildingRequest(
    val clientId: String? = null,
    val name: String? = null,
    val address: String,
    val suburb: String,
    val state: String,
    val postcode: String,
    val country: String = "Australia",
    val buildingType: BuildingType = BuildingType.RESIDENTIAL,
    val yearBuilt: Int? = null,
    val notes: String? = null
)

@Serializable
data class CreateUnitRequest(
    val unitNumber: String,
    val floor: Int? = null,
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val parkingSpaces: Int = 0,
    val areaSqm: Double? = null,
    val rentAmount: Double? = null,
    val rentFrequency: RentFrequency = RentFrequency.MONTHLY,
    val notes: String? = null
)

@Serializable
data class CreateTenantRequest(
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val idType: String? = null,
    val idReference: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val notes: String? = null
)

@Serializable
data class CreateLeaseRequest(
    val unitId: String,
    val tenantId: String,
    val startDate: String,
    val endDate: String? = null,
    val rentAmount: Double,
    val rentFrequency: RentFrequency = RentFrequency.MONTHLY,
    val bondAmount: Double,
    val paymentDay: Int = 1,
    val specialConditions: String? = null,
    val moveInDate: String? = null
)

@Serializable
data class TerminateLeaseRequest(val moveOutDate: String)

@Serializable
data class RecordPaymentRequest(
    val leaseId: String,
    val amount: Double,
    val paymentType: PaymentType,
    val status: PaymentStatus = PaymentStatus.RECEIVED,
    val periodFrom: String,
    val periodTo: String,
    val referenceNo: String? = null,
    val notes: String? = null,
    val paymentDate: String? = null
)

@Serializable
data class AdjustPaymentRequest(
    val amount: Double,
    val reason: String,
    val notes: String? = null
)

@Serializable
data class CreateMaintenanceRequest(
    val unitId: String,
    val category: MaintenanceCategory,
    val priority: MaintenancePriority = MaintenancePriority.ROUTINE,
    val title: String,
    val description: String
)

@Serializable
data class UpdateMaintenanceRequest(
    val status: MaintenanceStatus? = null,
    val contractorName: String? = null,
    val contractorPhone: String? = null,
    val assignedDate: String? = null,
    val attendedDate: String? = null,
    val notes: String? = null
)

@Serializable
data class CloseMaintenanceRequest(
    val completedDate: String,
    val invoiceReference: String? = null,
    val invoiceAmount: Double? = null,
    val notes: String? = null
)

// ── Responses ───────────────────────────────────────────────────────────────

@Serializable
data class ApiResponse<T>(
    val data: T,
    val meta: PaginationMeta? = null
)

@Serializable
data class PaginationMeta(
    val total: Int,
    val page: Int,
    val limit: Int,
    val pages: Int
)

@Serializable
data class ErrorResponse(val error: String, val details: List<FieldError>? = null)

@Serializable
data class FieldError(val field: String, val message: String)

@Serializable
data class DashboardData(
    val buildings: Int,
    val units: UnitStats,
    val occupancyRate: Int,
    val overduePayments: Int,
    val expiringLeases: Int,
    val openMaintenance: Int,
    val slaBreached: Int
)

@Serializable
data class UnitStats(val total: Int, val occupied: Int, val vacant: Int)

@Serializable
data class BuildingSummaryData(
    val total: Int,
    val occupied: Int,
    val vacant: Int,
    val occupancyRate: Double,
    val monthlyIncome: Double
)
