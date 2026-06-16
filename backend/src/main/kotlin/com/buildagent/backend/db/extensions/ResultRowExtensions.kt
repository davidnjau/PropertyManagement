package com.buildagent.backend.db.extensions

import com.buildagent.backend.db.tables.*
import com.buildagent.shared.models.*
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toBuilding(unitCount: Int = 0): Building = Building(
    id = this[BuildingsTable.id].value.toString(),
    agencyId = this[BuildingsTable.agencyId].value.toString(),
    clientId = this[BuildingsTable.clientId]?.value?.toString(),
    name = this[BuildingsTable.name],
    address = this[BuildingsTable.address],
    suburb = this[BuildingsTable.suburb],
    state = this[BuildingsTable.state],
    postcode = this[BuildingsTable.postcode],
    country = this[BuildingsTable.country],
    buildingType = this[BuildingsTable.buildingType],
    yearBuilt = this[BuildingsTable.yearBuilt],
    councilRatesPa = this[BuildingsTable.councilRatesPa]?.toDouble(),
    strataLevyPq = this[BuildingsTable.strataLevyPq]?.toDouble(),
    notes = this[BuildingsTable.notes],
    isActive = this[BuildingsTable.isActive],
    createdAt = this[BuildingsTable.createdAt].toString(),
    updatedAt = this[BuildingsTable.updatedAt].toString(),
    unitCount = unitCount
)

fun ResultRow.toUnit(): BuildingUnit = BuildingUnit(
    id = this[UnitsTable.id].value.toString(),
    buildingId = this[UnitsTable.buildingId].value.toString(),
    unitNumber = this[UnitsTable.unitNumber],
    floor = this[UnitsTable.floor],
    bedrooms = this[UnitsTable.bedrooms],
    bathrooms = this[UnitsTable.bathrooms],
    parkingSpaces = this[UnitsTable.parkingSpaces],
    areaSqm = this[UnitsTable.areaSqm]?.toDouble(),
    rentAmount = this[UnitsTable.rentAmount]?.toDouble(),
    rentFrequency = this[UnitsTable.rentFrequency],
    status = this[UnitsTable.status],
    notes = this[UnitsTable.notes],
    createdAt = this[UnitsTable.createdAt].toString(),
    updatedAt = this[UnitsTable.updatedAt].toString()
)

fun ResultRow.toTenant(): Tenant = Tenant(
    id = this[TenantsTable.id].value.toString(),
    agencyId = this[TenantsTable.agencyId].value.toString(),
    fullName = this[TenantsTable.fullName],
    email = this[TenantsTable.email],
    phone = this[TenantsTable.phone],
    dateOfBirth = this[TenantsTable.dateOfBirth]?.toString(),
    idType = this[TenantsTable.idType],
    idReference = this[TenantsTable.idReference],
    emergencyContactName = this[TenantsTable.emergencyContactName],
    emergencyContactPhone = this[TenantsTable.emergencyContactPhone],
    notes = this[TenantsTable.notes],
    isActive = this[TenantsTable.isActive],
    createdAt = this[TenantsTable.createdAt].toString(),
    updatedAt = this[TenantsTable.updatedAt].toString()
)

fun ResultRow.toLease(): Lease = Lease(
    id = this[LeasesTable.id].value.toString(),
    unitId = this[LeasesTable.unitId].value.toString(),
    tenantId = this[LeasesTable.tenantId].value.toString(),
    startDate = this[LeasesTable.startDate].toString(),
    endDate = this[LeasesTable.endDate]?.toString(),
    rentAmount = this[LeasesTable.rentAmount].toDouble(),
    rentFrequency = this[LeasesTable.rentFrequency],
    bondAmount = this[LeasesTable.bondAmount].toDouble(),
    bondLodged = this[LeasesTable.bondLodged],
    bondLodgedDate = this[LeasesTable.bondLodgedDate]?.toString(),
    bondLodgementRef = this[LeasesTable.bondLodgementRef],
    status = this[LeasesTable.status],
    paymentDay = this[LeasesTable.paymentDay],
    specialConditions = this[LeasesTable.specialConditions],
    moveInDate = this[LeasesTable.moveInDate]?.toString(),
    moveOutDate = this[LeasesTable.moveOutDate]?.toString(),
    createdAt = this[LeasesTable.createdAt].toString(),
    updatedAt = this[LeasesTable.updatedAt].toString()
)

fun ResultRow.toPayment(): Payment = Payment(
    id = this[PaymentsTable.id].value.toString(),
    leaseId = this[PaymentsTable.leaseId].value.toString(),
    agencyId = this[PaymentsTable.agencyId].value.toString(),
    amount = this[PaymentsTable.amount].toDouble(),
    paymentType = this[PaymentsTable.paymentType],
    status = this[PaymentsTable.status],
    periodFrom = this[PaymentsTable.periodFrom].toString(),
    periodTo = this[PaymentsTable.periodTo].toString(),
    referenceNo = this[PaymentsTable.referenceNo],
    notes = this[PaymentsTable.notes],
    recordedBy = this[PaymentsTable.recordedBy]?.value?.toString(),
    paymentDate = this[PaymentsTable.paymentDate]?.toString(),
    isAdjustment = this[PaymentsTable.isAdjustment],
    adjustmentReason = this[PaymentsTable.adjustmentReason],
    adjustedPaymentId = this[PaymentsTable.adjustedPaymentId]?.toString(),
    agentNotes = this[PaymentsTable.agentNotes],
    voided = this[PaymentsTable.voided],
    voidedAt = this[PaymentsTable.voidedAt]?.toString(),
    createdAt = this[PaymentsTable.createdAt].toString(),
    updatedAt = this[PaymentsTable.updatedAt].toString()
)

fun ResultRow.toMaintenance(): MaintenanceRequest = MaintenanceRequest(
    id = this[MaintenanceRequestsTable.id].value.toString(),
    agencyId = this[MaintenanceRequestsTable.agencyId].value.toString(),
    unitId = this[MaintenanceRequestsTable.unitId].value.toString(),
    reportedByType = this[MaintenanceRequestsTable.reportedByType],
    reportedById = this[MaintenanceRequestsTable.reportedById].toString(),
    category = this[MaintenanceRequestsTable.category],
    priority = this[MaintenanceRequestsTable.priority],
    status = this[MaintenanceRequestsTable.status],
    title = this[MaintenanceRequestsTable.title],
    description = this[MaintenanceRequestsTable.description],
    contractorName = this[MaintenanceRequestsTable.contractorName],
    contractorPhone = this[MaintenanceRequestsTable.contractorPhone],
    assignedDate = this[MaintenanceRequestsTable.assignedDate]?.toString(),
    attendedDate = this[MaintenanceRequestsTable.attendedDate]?.toString(),
    completedDate = this[MaintenanceRequestsTable.completedDate]?.toString(),
    closedDate = this[MaintenanceRequestsTable.closedDate]?.toString(),
    invoiceReference = this[MaintenanceRequestsTable.invoiceReference],
    invoiceAmount = this[MaintenanceRequestsTable.invoiceAmount]?.toDouble(),
    agentSignOff = this[MaintenanceRequestsTable.agentSignOff]?.toString(),
    slaTargetDate = this[MaintenanceRequestsTable.slaTargetDate]?.toString(),
    notes = this[MaintenanceRequestsTable.notes],
    createdAt = this[MaintenanceRequestsTable.createdAt].toString(),
    updatedAt = this[MaintenanceRequestsTable.updatedAt].toString()
)
