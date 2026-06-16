package com.buildagent.backend.kafka

import com.buildagent.shared.events.*
import io.ktor.server.config.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.Properties

object KafkaFactory {
    private val log = LoggerFactory.getLogger("KafkaFactory")
    private lateinit var producer: KafkaProducer<String, String>
    private lateinit var bootstrapServers: String
    private lateinit var groupId: String

    private val json = Json { encodeDefaults = true }

    fun init(config: ApplicationConfig) {
        bootstrapServers = config.property("kafka.bootstrapServers").getString()
        groupId = config.property("kafka.groupId").getString()

        val producerProps = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.RETRIES_CONFIG, 3)
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
        }
        producer = KafkaProducer(producerProps)
        log.info("Kafka producer initialized: $bootstrapServers")
    }

    fun <T> send(topic: String, key: String, payload: String) {
        try {
            producer.send(ProducerRecord(topic, key, payload)) { meta, ex ->
                if (ex != null) log.error("Kafka send failed [$topic]: ${ex.message}")
                else log.debug("Kafka sent [$topic] partition=${meta.partition()} offset=${meta.offset()}")
            }
        } catch (e: Exception) {
            log.error("Kafka send error [$topic]: ${e.message}")
        }
    }

    fun sendPaymentRecorded(event: PaymentRecordedEvent) =
        send<PaymentRecordedEvent>(TOPIC_PAYMENTS_RECORDED, event.agencyId, json.encodeToString(event))

    fun sendPaymentAdjusted(event: PaymentAdjustedEvent) =
        send<PaymentAdjustedEvent>(TOPIC_PAYMENTS_ADJUSTED, event.agencyId, json.encodeToString(event))

    fun sendLeaseCreated(event: LeaseCreatedEvent) =
        send<LeaseCreatedEvent>(TOPIC_LEASES_CREATED, event.agencyId, json.encodeToString(event))

    fun sendLeaseExpiring(event: LeaseExpiringEvent) =
        send<LeaseExpiringEvent>(TOPIC_LEASES_EXPIRING, event.agencyId, json.encodeToString(event))

    fun sendLeaseTerminated(event: LeaseTerminatedEvent) =
        send<LeaseTerminatedEvent>(TOPIC_LEASES_TERMINATED, event.agencyId, json.encodeToString(event))

    fun sendMaintenanceCreated(event: MaintenanceCreatedEvent) =
        send<MaintenanceCreatedEvent>(TOPIC_MAINTENANCE_CREATED, event.agencyId, json.encodeToString(event))

    fun sendMaintenanceStatusChanged(event: MaintenanceStatusChangedEvent) =
        send<MaintenanceStatusChangedEvent>(TOPIC_MAINTENANCE_STATUS_CHANGED, event.agencyId, json.encodeToString(event))

    fun sendMaintenanceSlaBreached(event: MaintenanceSlaBreachedEvent) =
        send<MaintenanceSlaBreachedEvent>(TOPIC_MAINTENANCE_SLA_BREACHED, event.agencyId, json.encodeToString(event))

    fun sendAuditEvent(event: AuditStreamEvent) =
        send<AuditStreamEvent>(TOPIC_AUDIT_EVENTS, event.agencyId, json.encodeToString(event))

    fun newConsumer(): KafkaConsumer<String, String> {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true)
        }
        return KafkaConsumer(props)
    }

    fun close() {
        producer.close()
        log.info("Kafka producer closed")
    }
}
