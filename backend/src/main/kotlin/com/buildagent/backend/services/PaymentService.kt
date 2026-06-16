package com.buildagent.backend.services

import com.buildagent.backend.db.dbQuery
import com.buildagent.backend.db.tables.*
import com.buildagent.backend.kafka.KafkaFactory
import com.buildagent.shared.events.PaymentAdjustedEvent
import com.buildagent.shared.events.PaymentRecordedEvent
import com.buildagent.shared.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import java.util.UUID

class PaymentService(private val auditService: AuditService) {

    suspend fun list(agencyId: UUID, page: Int, limit: Int, leaseId: UUID? = null): Pair<List<Payment>, Int> = dbQuery {
        val offset = ((page - 1) * limit).toLong()
        var where: Op<Boolean> = PaymentsTable.agencyId eq agencyId
        if (leaseId != null) where = where and (PaymentsTable.leaseId eq leaseId)

        val total = PaymentsTable.selectAll().where(where).count().toInt()
        val rows = PaymentsTable.selectAll().where(where)
            .orderBy(PaymentsTable.createdAt, SortOrder.DESC)
            .limit(limit, offset)
            .map { it.toPayment() }
        rows to total
    }

    suspend fun record(agencyId: UUID, actorId: UUID, req: RecordPaymentRequest): Payment = dbQuery {
        val leaseId = UUID.fromString(req.leaseId)
        val now: Instant = Clock.System.now()
        val id = PaymentsTable.insertAndGetId {
            it[PaymentsTable.leaseId] = leaseId
            it[PaymentsTable.agencyId] = agencyId
            it[amount] = req.amount.toBigDecimal()
            it[paymentType] = req.paymentType
            it[status] = req.status
            it[periodFrom] = LocalDate.parse(req.periodFrom)
            it[periodTo] = LocalDate.parse(req.periodTo)
            it[referenceNo] = req.referenceNo
            it[notes] = req.notes
            it[recordedBy] = actorId
            it[paymentDate] = req.paymentDate?.let { d -> LocalDate.parse(d) }
            it[createdAt] = now
            it[updatedAt] = now
        }
        val payment = PaymentsTable.selectAll().where { PaymentsTable.id eq id }.single().toPayment()

        KafkaFactory.sendPaymentRecorded(
            PaymentRecordedEvent(
                eventId = UUID.randomUUID().toString(),
                agencyId = agencyId.toString(),
                paymentId = payment.id,
                leaseId = req.leaseId,
                tenantId = "",
                tenantEmail = "",
                amount = req.amount,
                paymentType = req.paymentType.name,
                status = req.status.name,
                periodFrom = req.periodFrom,
                periodTo = req.periodTo,
                occurredAt = Clock.System.now().toString()
            )
        )

        payment
    }

    suspend fun adjust(agencyId: UUID, actorId: UUID, originalId: UUID, req: AdjustPaymentRequest): Payment {
        val original = dbQuery {
            PaymentsTable.selectAll()
                .where { PaymentsTable.id eq originalId and (PaymentsTable.agencyId eq agencyId) }
                .singleOrNull()
        } ?: throw NoSuchElementException("Payment not found")

        return dbQuery {
            val now: Instant = Clock.System.now()
            val id = PaymentsTable.insertAndGetId {
                it[leaseId] = original[PaymentsTable.leaseId]
                it[PaymentsTable.agencyId] = agencyId
                it[amount] = req.amount.toBigDecimal()
                it[paymentType] = original[PaymentsTable.paymentType]
                it[status] = PaymentStatus.RECEIVED
                it[periodFrom] = original[PaymentsTable.periodFrom]
                it[periodTo] = original[PaymentsTable.periodTo]
                it[recordedBy] = actorId
                it[isAdjustment] = true
                it[adjustmentReason] = req.reason
                it[adjustedPaymentId] = originalId
                it[notes] = req.notes
                it[createdAt] = now
                it[updatedAt] = now
            }
            val adjustment = PaymentsTable.selectAll().where { PaymentsTable.id eq id }.single().toPayment()

            KafkaFactory.sendPaymentAdjusted(
                PaymentAdjustedEvent(
                    eventId = UUID.randomUUID().toString(),
                    agencyId = agencyId.toString(),
                    adjustmentId = adjustment.id,
                    originalPaymentId = originalId.toString(),
                    leaseId = original[PaymentsTable.leaseId].value.toString(),
                    newAmount = req.amount,
                    reason = req.reason,
                    actorId = actorId.toString(),
                    occurredAt = Clock.System.now().toString()
                )
            )

            adjustment
        }
    }

    suspend fun overdue(agencyId: UUID): List<Payment> = dbQuery {
        val graceCutoff = Clock.System.todayIn(TimeZone.currentSystemDefault()).minus(3, DateTimeUnit.DAY)
        PaymentsTable.selectAll().where {
            (PaymentsTable.agencyId eq agencyId) and
            (PaymentsTable.status inList listOf(PaymentStatus.PENDING, PaymentStatus.PARTIAL)) and
            (PaymentsTable.periodTo less graceCutoff) and
            (PaymentsTable.isAdjustment eq false)
        }.orderBy(PaymentsTable.periodTo, SortOrder.ASC).map { it.toPayment() }
    }

    private fun ResultRow.toPayment() = Payment(
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
        createdAt = this[PaymentsTable.createdAt].toString(),
        updatedAt = this[PaymentsTable.updatedAt].toString()
    )
}
