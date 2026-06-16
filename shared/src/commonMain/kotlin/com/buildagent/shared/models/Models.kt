package com.buildagent.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Agency(
    val id: String,
    val name: String,
    val subscriptionTier: String = "starter",
    val contactEmail: String,
    val contactPhone: String? = null,
    val address: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class User(
    val id: String,
    val agencyId: String,
    val auth0Sub: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val phone: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class Client(
    val id: String,
    val agencyId: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val address: String? = null,
    val managementFeePct: Double = 8.0,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class Building(
    val id: String,
    val agencyId: String,
    val clientId: String? = null,
    val name: String? = null,
    val address: String,
    val suburb: String,
    val state: String,
    val postcode: String,
    val country: String = "Australia",
    val buildingType: BuildingType = BuildingType.RESIDENTIAL,
    val yearBuilt: Int? = null,
    val councilRatesPa: Double? = null,
    val strataLevyPq: Double? = null,
    val insurancePolicyNo: String? = null,
    val insuranceExpiry: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val client: ClientSummary? = null,
    val unitCount: Int = 0,
    val units: List<BuildingUnit>? = null
)

@Serializable
data class ClientSummary(
    val id: String,
    val fullName: String,
    val email: String
)

@Serializable
data class BuildingUnit(
    val id: String,
    val buildingId: String,
    val unitNumber: String,
    val floor: Int? = null,
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val parkingSpaces: Int = 0,
    val areaSqm: Double? = null,
    val rentAmount: Double? = null,
    val rentFrequency: RentFrequency = RentFrequency.MONTHLY,
    val status: UnitStatus = UnitStatus.VACANT,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val building: BuildingSummary? = null,
    val leases: List<Lease>? = null
)

@Serializable
data class BuildingSummary(
    val id: String,
    val address: String,
    val suburb: String,
    val agencyId: String
)

@Serializable
data class Tenant(
    val id: String,
    val agencyId: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val idType: String? = null,
    val idReference: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    val leases: List<Lease>? = null
)

@Serializable
data class TenantSummary(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null
)

@Serializable
data class Lease(
    val id: String,
    val unitId: String,
    val tenantId: String,
    val startDate: String,
    val endDate: String? = null,
    val rentAmount: Double,
    val rentFrequency: RentFrequency = RentFrequency.MONTHLY,
    val bondAmount: Double,
    val bondLodged: Boolean = false,
    val bondLodgedDate: String? = null,
    val bondLodgementRef: String? = null,
    val status: LeaseStatus = LeaseStatus.ACTIVE,
    val computedStatus: LeaseStatus? = null,
    val paymentDay: Int = 1,
    val specialConditions: String? = null,
    val moveInDate: String? = null,
    val moveOutDate: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val tenant: TenantSummary? = null,
    val unit: BuildingUnit? = null,
    val payments: List<Payment>? = null
)

@Serializable
data class Payment(
    val id: String,
    val leaseId: String,
    val agencyId: String,
    val amount: Double,
    val paymentType: PaymentType,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val periodFrom: String,
    val periodTo: String,
    val referenceNo: String? = null,
    val notes: String? = null,
    val recordedBy: String? = null,
    val paymentDate: String? = null,
    val isAdjustment: Boolean = false,
    val adjustmentReason: String? = null,
    val adjustedPaymentId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val lease: LeaseWithTenantUnit? = null,
    val recordedByUser: UserSummary? = null
)

@Serializable
data class LeaseWithTenantUnit(
    val id: String,
    val tenant: TenantSummary? = null,
    val unit: UnitWithBuilding? = null
)

@Serializable
data class UnitWithBuilding(
    val id: String,
    val unitNumber: String,
    val building: BuildingAddressSummary? = null
)

@Serializable
data class BuildingAddressSummary(
    val address: String,
    val suburb: String
)

@Serializable
data class UserSummary(
    val id: String,
    val fullName: String
)

@Serializable
data class MaintenanceRequest(
    val id: String,
    val agencyId: String,
    val unitId: String,
    val reportedByType: ReporterType = ReporterType.TENANT,
    val reportedById: String,
    val category: MaintenanceCategory,
    val priority: MaintenancePriority = MaintenancePriority.ROUTINE,
    val status: MaintenanceStatus = MaintenanceStatus.REPORTED,
    val title: String,
    val description: String,
    val contractorName: String? = null,
    val contractorPhone: String? = null,
    val assignedDate: String? = null,
    val attendedDate: String? = null,
    val completedDate: String? = null,
    val closedDate: String? = null,
    val invoiceReference: String? = null,
    val invoiceAmount: Double? = null,
    val agentSignOff: String? = null,
    val slaTargetDate: String? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val unit: UnitWithBuilding? = null
)

@Serializable
data class AuditEvent(
    val id: String,
    val agencyId: String,
    val actorId: String,
    val action: AuditAction,
    val entityType: String,
    val entityId: String,
    val diffJson: String? = null,
    val ipAddress: String? = null,
    val createdAt: String
)
