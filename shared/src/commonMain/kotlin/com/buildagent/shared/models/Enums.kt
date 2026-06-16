package com.buildagent.shared.models

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole { ADMIN, AGENT, CLIENT, TENANT, VENDOR }

@Serializable
enum class UserType { AGENCY, AGENT, CLIENT, TENANT, VENDOR }

fun UserType.toRole(): UserRole = when (this) {
    UserType.AGENCY -> UserRole.ADMIN
    UserType.AGENT  -> UserRole.AGENT
    UserType.CLIENT -> UserRole.CLIENT
    UserType.TENANT -> UserRole.TENANT
    UserType.VENDOR -> UserRole.VENDOR
}

fun UserRole.toUserType(): UserType = when (this) {
    UserRole.ADMIN  -> UserType.AGENCY
    UserRole.AGENT  -> UserType.AGENT
    UserRole.CLIENT -> UserType.CLIENT
    UserRole.TENANT -> UserType.TENANT
    UserRole.VENDOR -> UserType.VENDOR
}

@Serializable
enum class BuildingType { RESIDENTIAL, COMMERCIAL, MIXED }

@Serializable
enum class UnitStatus { VACANT, OCCUPIED, UNDER_MAINTENANCE, OFF_MARKET }

@Serializable
enum class RentFrequency { WEEKLY, FORTNIGHTLY, MONTHLY }

@Serializable
enum class LeaseStatus { ACTIVE, PERIODIC, EXPIRING_SOON, EXPIRED, VACATED, TERMINATED }

@Serializable
enum class PaymentType { RENT, BOND, WATER, FEE, OTHER }

@Serializable
enum class PaymentStatus { PENDING, RECEIVED, OVERDUE, PARTIAL, WAIVED }

@Serializable
enum class MaintenanceCategory {
    PLUMBING, ELECTRICAL, HVAC, STRUCTURAL, APPLIANCE, SECURITY, COMMON_AREA, OTHER
}

@Serializable
enum class MaintenancePriority { EMERGENCY, URGENT, ROUTINE, LOW }

@Serializable
enum class MaintenanceStatus {
    REPORTED, ASSESSED, ASSIGNED, IN_PROGRESS, COMPLETED, CLOSED, CANCELLED
}

@Serializable
enum class AuditAction {
    CREATED, UPDATED, DELETED, STATUS_CHANGED,
    PAYMENT_RECORDED, PAYMENT_ADJUSTED, PAYMENT_WAIVED,
    LEASE_CREATED, LEASE_TERMINATED,
    MAINTENANCE_ASSIGNED, MAINTENANCE_CLOSED
}

@Serializable
enum class ReporterType { TENANT, AGENT, ADMIN }

@Serializable
enum class DocumentEntity {
    BUILDING, UNIT, LEASE, TENANT, MAINTENANCE_REQUEST
}
