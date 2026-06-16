package com.buildagent.backend.db.tables

import com.buildagent.shared.models.*
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.ReferenceOption

object AgenciesTable : UUIDTable("agencies") {
    val name = varchar("name", 255)
    val subscriptionTier = varchar("subscription_tier", 50).default("starter")
    val contactEmail = varchar("contact_email", 255)
    val contactPhone = varchar("contact_phone", 50).nullable()
    val address = text("address").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object UsersTable : UUIDTable("users") {
    val agencyId = reference("agency_id", AgenciesTable, onDelete = ReferenceOption.CASCADE)
    val auth0Sub = varchar("auth0_sub", 255).uniqueIndex()
    val email = varchar("email", 255)
    val fullName = varchar("full_name", 255)
    val role = enumerationByName("role", 20, UserRole::class)
    val phone = varchar("phone", 50).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object ClientsTable : UUIDTable("clients") {
    val agencyId = reference("agency_id", AgenciesTable, onDelete = ReferenceOption.CASCADE)
    val fullName = varchar("full_name", 255)
    val email = varchar("email", 255)
    val phone = varchar("phone", 50).nullable()
    val address = text("address").nullable()
    val auth0Sub = varchar("auth0_sub", 255).nullable().uniqueIndex()
    val managementFeePct = decimal("management_fee_pct", 5, 2).default(8.toBigDecimal())
    val notes = text("notes").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object BuildingsTable : UUIDTable("buildings") {
    val agencyId = reference("agency_id", AgenciesTable, onDelete = ReferenceOption.CASCADE)
    val clientId = reference("client_id", ClientsTable).nullable()
    val name = varchar("name", 255).nullable()
    val address = varchar("address", 500)
    val suburb = varchar("suburb", 255)
    val state = varchar("state", 50)
    val postcode = varchar("postcode", 20)
    val country = varchar("country", 100).default("Australia")
    val buildingType = enumerationByName("building_type", 20, BuildingType::class).default(BuildingType.RESIDENTIAL)
    val yearBuilt = integer("year_built").nullable()
    val councilRatesPa = decimal("council_rates_pa", 10, 2).nullable()
    val strataLevyPq = decimal("strata_levy_pq", 10, 2).nullable()
    val insurancePolicyNo = varchar("insurance_policy_no", 100).nullable()
    val insuranceExpiry = date("insurance_expiry").nullable()
    val notes = text("notes").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object UnitsTable : UUIDTable("units") {
    val buildingId = reference("building_id", BuildingsTable, onDelete = ReferenceOption.CASCADE)
    val unitNumber = varchar("unit_number", 50)
    val floor = integer("floor").nullable()
    val bedrooms = integer("bedrooms").default(0)
    val bathrooms = integer("bathrooms").default(0)
    val parkingSpaces = integer("parking_spaces").default(0)
    val areaSqm = decimal("area_sqm", 8, 2).nullable()
    val rentAmount = decimal("rent_amount", 10, 2).nullable()
    val rentFrequency = enumerationByName("rent_frequency", 20, RentFrequency::class).default(RentFrequency.MONTHLY)
    val status = enumerationByName("status", 30, UnitStatus::class).default(UnitStatus.VACANT)
    val notes = text("notes").nullable()
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object TenantsTable : UUIDTable("tenants") {
    val agencyId = reference("agency_id", AgenciesTable, onDelete = ReferenceOption.CASCADE)
    val fullName = varchar("full_name", 255)
    val email = varchar("email", 255)
    val phone = varchar("phone", 50).nullable()
    val dateOfBirth = date("date_of_birth").nullable()
    val idType = varchar("id_type", 100).nullable()
    val idReference = varchar("id_reference", 100).nullable()
    val emergencyContactName = varchar("emergency_contact_name", 255).nullable()
    val emergencyContactPhone = varchar("emergency_contact_phone", 50).nullable()
    val auth0Sub = varchar("auth0_sub", 255).nullable().uniqueIndex()
    val notes = text("notes").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object LeasesTable : UUIDTable("leases") {
    val unitId = reference("unit_id", UnitsTable)
    val tenantId = reference("tenant_id", TenantsTable)
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
    val rentAmount = decimal("rent_amount", 10, 2)
    val rentFrequency = enumerationByName("rent_frequency", 20, RentFrequency::class).default(RentFrequency.MONTHLY)
    val bondAmount = decimal("bond_amount", 10, 2)
    val bondLodged = bool("bond_lodged").default(false)
    val bondLodgedDate = date("bond_lodged_date").nullable()
    val bondLodgementRef = varchar("bond_lodgement_ref", 100).nullable()
    val status = enumerationByName("status", 30, LeaseStatus::class).default(LeaseStatus.ACTIVE)
    val paymentDay = integer("payment_day").default(1)
    val specialConditions = text("special_conditions").nullable()
    val moveInDate = date("move_in_date").nullable()
    val moveOutDate = date("move_out_date").nullable()
    val conditionReportRef = varchar("condition_report_ref", 255).nullable()
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object PaymentsTable : UUIDTable("payments") {
    val leaseId = reference("lease_id", LeasesTable)
    val agencyId = reference("agency_id", AgenciesTable)
    val amount = decimal("amount", 10, 2)
    val paymentType = enumerationByName("payment_type", 20, PaymentType::class)
    val status = enumerationByName("status", 20, PaymentStatus::class).default(PaymentStatus.PENDING)
    val periodFrom = date("period_from")
    val periodTo = date("period_to")
    val referenceNo = varchar("reference_no", 100).nullable()
    val notes = text("notes").nullable()
    val recordedBy = reference("recorded_by", UsersTable).nullable()
    val paymentDate = date("payment_date").nullable()
    val isAdjustment = bool("is_adjustment").default(false)
    val adjustmentReason = text("adjustment_reason").nullable()
    val adjustedPaymentId = uuid("adjusted_payment_id").nullable()
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object MaintenanceRequestsTable : UUIDTable("maintenance_requests") {
    val agencyId = reference("agency_id", AgenciesTable)
    val unitId = reference("unit_id", UnitsTable)
    val reportedByType = enumerationByName("reported_by_type", 20, ReporterType::class).default(ReporterType.TENANT)
    val reportedById = uuid("reported_by_id")
    val category = enumerationByName("category", 30, MaintenanceCategory::class)
    val priority = enumerationByName("priority", 20, MaintenancePriority::class).default(MaintenancePriority.ROUTINE)
    val status = enumerationByName("status", 30, MaintenanceStatus::class).default(MaintenanceStatus.REPORTED)
    val title = varchar("title", 500)
    val description = text("description")
    val contractorName = varchar("contractor_name", 255).nullable()
    val contractorPhone = varchar("contractor_phone", 50).nullable()
    val assignedDate = date("assigned_date").nullable()
    val attendedDate = date("attended_date").nullable()
    val completedDate = date("completed_date").nullable()
    val closedDate = date("closed_date").nullable()
    val invoiceReference = varchar("invoice_reference", 100).nullable()
    val invoiceAmount = decimal("invoice_amount", 10, 2).nullable()
    val agentSignOff = uuid("agent_sign_off").nullable()
    val slaTargetDate = date("sla_target_date").nullable()
    val notes = text("notes").nullable()
    val createdAt: Column<Instant> = timestamp("created_at")
    val updatedAt: Column<Instant> = timestamp("updated_at")
}

object AuditEventsTable : UUIDTable("audit_events") {
    val agencyId = reference("agency_id", AgenciesTable)
    val actorId = reference("actor_id", UsersTable)
    val action = enumerationByName("action", 40, AuditAction::class)
    val entityType = varchar("entity_type", 100)
    val entityId = uuid("entity_id")
    val diffJson = text("diff_json").nullable()
    val ipAddress = varchar("ip_address", 50).nullable()
    val userAgent = text("user_agent").nullable()
    val createdAt: Column<Instant> = timestamp("created_at")
}
