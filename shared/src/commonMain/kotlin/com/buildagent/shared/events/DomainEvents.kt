package com.buildagent.shared.events

import kotlinx.serialization.Serializable

const val TOPIC_PAYMENTS_RECORDED = "buildagent.payments.recorded"
const val TOPIC_PAYMENTS_ADJUSTED = "buildagent.payments.adjusted"
const val TOPIC_LEASES_CREATED = "buildagent.leases.created"
const val TOPIC_LEASES_EXPIRING = "buildagent.leases.expiring"
const val TOPIC_LEASES_TERMINATED = "buildagent.leases.terminated"
const val TOPIC_MAINTENANCE_CREATED = "buildagent.maintenance.created"
const val TOPIC_MAINTENANCE_STATUS_CHANGED = "buildagent.maintenance.status-changed"
const val TOPIC_MAINTENANCE_SLA_BREACHED = "buildagent.maintenance.sla-breached"
const val TOPIC_NOTIFICATIONS_EMAIL = "buildagent.notifications.email"
const val TOPIC_AUDIT_EVENTS = "buildagent.audit.events"

@Serializable
data class PaymentRecordedEvent(
    val eventId: String,
    val agencyId: String,
    val paymentId: String,
    val leaseId: String,
    val tenantId: String,
    val tenantEmail: String,
    val amount: Double,
    val paymentType: String,
    val status: String,
    val periodFrom: String,
    val periodTo: String,
    val occurredAt: String
)

@Serializable
data class PaymentAdjustedEvent(
    val eventId: String,
    val agencyId: String,
    val adjustmentId: String,
    val originalPaymentId: String,
    val leaseId: String,
    val newAmount: Double,
    val reason: String,
    val actorId: String,
    val occurredAt: String
)

@Serializable
data class LeaseCreatedEvent(
    val eventId: String,
    val agencyId: String,
    val leaseId: String,
    val unitId: String,
    val tenantId: String,
    val tenantEmail: String,
    val startDate: String,
    val endDate: String?,
    val rentAmount: Double,
    val occurredAt: String
)

@Serializable
data class LeaseExpiringEvent(
    val eventId: String,
    val agencyId: String,
    val leaseId: String,
    val unitId: String,
    val tenantId: String,
    val tenantEmail: String,
    val tenantName: String,
    val endDate: String,
    val daysUntilExpiry: Int,
    val occurredAt: String
)

@Serializable
data class LeaseTerminatedEvent(
    val eventId: String,
    val agencyId: String,
    val leaseId: String,
    val unitId: String,
    val tenantId: String,
    val moveOutDate: String,
    val actorId: String,
    val occurredAt: String
)

@Serializable
data class MaintenanceCreatedEvent(
    val eventId: String,
    val agencyId: String,
    val requestId: String,
    val unitId: String,
    val title: String,
    val priority: String,
    val category: String,
    val slaTargetDate: String,
    val occurredAt: String
)

@Serializable
data class MaintenanceStatusChangedEvent(
    val eventId: String,
    val agencyId: String,
    val requestId: String,
    val previousStatus: String,
    val newStatus: String,
    val actorId: String,
    val occurredAt: String
)

@Serializable
data class MaintenanceSlaBreachedEvent(
    val eventId: String,
    val agencyId: String,
    val requestId: String,
    val unitId: String,
    val title: String,
    val priority: String,
    val slaTargetDate: String,
    val daysOverdue: Int,
    val occurredAt: String
)

@Serializable
data class EmailNotificationEvent(
    val eventId: String,
    val agencyId: String,
    val to: String,
    val subject: String,
    val templateId: String,
    val templateData: Map<String, String>,
    val occurredAt: String
)

@Serializable
data class AuditStreamEvent(
    val eventId: String,
    val agencyId: String,
    val actorId: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val diffJson: String?,
    val occurredAt: String
)
