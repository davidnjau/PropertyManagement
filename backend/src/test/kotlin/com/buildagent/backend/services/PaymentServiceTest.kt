package com.buildagent.backend.services

import com.buildagent.backend.TestDatabase
import com.buildagent.backend.TestFixtures
import com.buildagent.shared.models.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentServiceTest {

    private lateinit var service: PaymentService
    private lateinit var leaseService: LeaseService
    private val auditService = AuditService()

    @BeforeAll
    fun setUpDatabase() {
        TestDatabase.init()
    }

    @BeforeEach
    fun setUp() {
        TestDatabase.resetAll()
        service = PaymentService(auditService)
        leaseService = LeaseService()
    }

    private fun makeLeaseRequest(unitId: String, tenantId: String) = CreateLeaseRequest(
        unitId = unitId,
        tenantId = tenantId,
        startDate = "2025-01-01",
        endDate = "2026-01-01",
        rentAmount = 1500.0,
        rentFrequency = RentFrequency.MONTHLY,
        bondAmount = 3000.0,
        paymentDay = 1
    )

    private fun makePaymentRequest(leaseId: String) = RecordPaymentRequest(
        leaseId = leaseId,
        amount = 1500.0,
        paymentType = PaymentType.RENT,
        status = PaymentStatus.RECEIVED,
        periodFrom = "2025-01-01",
        periodTo = "2025-01-31",
        referenceNo = "REF-001"
    )

    @Test
    fun `record creates a payment`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val agencyUUID = UUID.fromString(agencyId)
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)
        val tenantId = TestFixtures.createTenant(agencyId)
        val actorId = UUID.fromString(TestFixtures.createUser(agencyId))

        val lease = leaseService.createLease(agencyId, makeLeaseRequest(unitId, tenantId))
        val payment = service.record(agencyUUID, actorId, makePaymentRequest(lease.id))

        assertNotNull(payment.id)
        assertEquals(1500.0, payment.amount)
        assertEquals(PaymentType.RENT, payment.paymentType)
        assertEquals(PaymentStatus.RECEIVED, payment.status)
        assertEquals(agencyId, payment.agencyId)
    }

    @Test
    fun `list returns payments for agency`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val agencyUUID = UUID.fromString(agencyId)
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)
        val tenantId = TestFixtures.createTenant(agencyId)
        val actorId = UUID.fromString(TestFixtures.createUser(agencyId))

        val lease = leaseService.createLease(agencyId, makeLeaseRequest(unitId, tenantId))
        service.record(agencyUUID, actorId, makePaymentRequest(lease.id))
        service.record(agencyUUID, actorId, makePaymentRequest(lease.id).copy(referenceNo = "REF-002"))

        val (payments, total) = service.list(agencyUUID, 1, 20)
        assertEquals(2, payments.size)
        assertEquals(2, total)
    }

    @Test
    fun `list filtered by leaseId returns only that lease payments`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val agencyUUID = UUID.fromString(agencyId)
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId1 = TestFixtures.createUnit(buildingId, unitNumber = "1A")
        val unitId2 = TestFixtures.createUnit(buildingId, unitNumber = "1B")
        val tenantId1 = TestFixtures.createTenant(agencyId, email = "t1@test.com")
        val tenantId2 = TestFixtures.createTenant(agencyId, email = "t2@test.com")
        val actorId = UUID.fromString(TestFixtures.createUser(agencyId))

        val lease1 = leaseService.createLease(agencyId, makeLeaseRequest(unitId1, tenantId1))
        val lease2 = leaseService.createLease(agencyId, makeLeaseRequest(unitId2, tenantId2))
        service.record(agencyUUID, actorId, makePaymentRequest(lease1.id))
        service.record(agencyUUID, actorId, makePaymentRequest(lease2.id))

        val (payments, total) = service.list(agencyUUID, 1, 20, leaseId = UUID.fromString(lease1.id))
        assertEquals(1, payments.size)
        assertEquals(1, total)
        assertEquals(lease1.id, payments.first().leaseId)
    }

    @Test
    fun `getById returns payment for matching agency`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val agencyUUID = UUID.fromString(agencyId)
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)
        val tenantId = TestFixtures.createTenant(agencyId)
        val actorId = UUID.fromString(TestFixtures.createUser(agencyId))

        val lease = leaseService.createLease(agencyId, makeLeaseRequest(unitId, tenantId))
        val payment = service.record(agencyUUID, actorId, makePaymentRequest(lease.id))

        val found = service.getById(agencyUUID, UUID.fromString(payment.id))
        assertNotNull(found)
        assertEquals(payment.id, found.id)
    }

    @Test
    fun `getById returns null for unknown payment`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val result = service.getById(UUID.fromString(agencyId), UUID.randomUUID())
        assertNull(result)
    }

    @Test
    fun `voidPayment marks payment as voided`() = runBlocking {
        val agencyId = TestFixtures.createAgency()
        val agencyUUID = UUID.fromString(agencyId)
        val buildingId = TestFixtures.createBuilding(agencyId)
        val unitId = TestFixtures.createUnit(buildingId)
        val tenantId = TestFixtures.createTenant(agencyId)
        val actorId = UUID.fromString(TestFixtures.createUser(agencyId))

        val lease = leaseService.createLease(agencyId, makeLeaseRequest(unitId, tenantId))
        val payment = service.record(agencyUUID, actorId, makePaymentRequest(lease.id))

        val voided = service.voidPayment(agencyUUID, actorId, UUID.fromString(payment.id))
        assertTrue(voided)

        val updated = service.getById(agencyUUID, UUID.fromString(payment.id))
        assertNotNull(updated)
        assertTrue(updated.voided)
    }
}
