package com.buildagent.backend.kafka.producers

import com.buildagent.backend.kafka.KafkaFactory
import com.buildagent.shared.events.*

class DomainEventProducer {
    fun paymentRecorded(event: PaymentRecordedEvent) = KafkaFactory.sendPaymentRecorded(event)
    fun paymentAdjusted(event: PaymentAdjustedEvent) = KafkaFactory.sendPaymentAdjusted(event)
    fun leaseCreated(event: LeaseCreatedEvent) = KafkaFactory.sendLeaseCreated(event)
    fun leaseExpiring(event: LeaseExpiringEvent) = KafkaFactory.sendLeaseExpiring(event)
    fun leaseTerminated(event: LeaseTerminatedEvent) = KafkaFactory.sendLeaseTerminated(event)
    fun maintenanceCreated(event: MaintenanceCreatedEvent) = KafkaFactory.sendMaintenanceCreated(event)
    fun maintenanceStatusChanged(event: MaintenanceStatusChangedEvent) = KafkaFactory.sendMaintenanceStatusChanged(event)
    fun maintenanceSlaBreached(event: MaintenanceSlaBreachedEvent) = KafkaFactory.sendMaintenanceSlaBreached(event)
    fun auditEvent(event: AuditStreamEvent) = KafkaFactory.sendAuditEvent(event)
}
