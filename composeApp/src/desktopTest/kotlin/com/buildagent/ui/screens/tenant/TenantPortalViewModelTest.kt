package com.buildagent.ui.screens.tenant

import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TenantPortalViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val client = mockk<BuildAgentClient>()

    private val fakeOverview = TenantOverview(
        rentDue = 1500.0,
        rentDueDate = "2025-01-01",
        leaseEndDate = "2026-01-01",
        leaseStatus = "ACTIVE",
        recentPayments = emptyList()
    )

    private val fakeLease = Lease(
        id = "lease-001",
        unitId = "unit-001",
        tenantId = "tenant-001",
        startDate = "2025-01-01",
        rentAmount = 1500.0,
        rentFrequency = RentFrequency.MONTHLY,
        bondAmount = 3000.0,
        paymentDay = 1,
        status = LeaseStatus.ACTIVE,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )

    private val fakePayment = Payment(
        id = "pay-001",
        leaseId = "lease-001",
        agencyId = "agency-001",
        amount = 1500.0,
        paymentType = PaymentType.RENT,
        status = PaymentStatus.RECEIVED,
        periodFrom = "2025-01-01",
        periodTo = "2025-01-31",
        isAdjustment = false,
        voided = false,
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { client.getTenantOverview() } returns ApiResponse(fakeOverview)
        coEvery { client.getTenantLease() } returns ApiResponse(fakeLease)
        coEvery { client.getTenantPayments() } returns ApiResponse(listOf(fakePayment))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads overview, lease, and payments`() = runTest {
        val vm = TenantPortalViewModel(client)

        assertNotNull(vm.overview.value)
        assertEquals(1500.0, vm.overview.value?.rentDue)
        assertNotNull(vm.lease.value)
        assertEquals(1, vm.payments.value.size)
    }

    @Test
    fun `init sets error when overview fails`() = runTest {
        coEvery { client.getTenantOverview() } throws RuntimeException("server error")

        val vm = TenantPortalViewModel(client)

        assertNotNull(vm.error.value)
    }

    @Test
    fun `loadMaintenance populates maintenance list`() = runTest {
        val fakeRequest = MaintenanceRequest(
            id = "mr-001",
            agencyId = "agency-001",
            unitId = "unit-001",
            reportedById = "tenant-001",
            category = MaintenanceCategory.PLUMBING,
            title = "Broken tap",
            description = "Tap dripping constantly",
            priority = MaintenancePriority.ROUTINE,
            status = MaintenanceStatus.REPORTED,
            createdAt = "2025-01-01T00:00:00Z",
            updatedAt = "2025-01-01T00:00:00Z"
        )
        coEvery { client.getTenantMaintenance() } returns ApiResponse(listOf(fakeRequest))

        val vm = TenantPortalViewModel(client)
        vm.loadMaintenance()

        assertEquals(1, vm.maintenance.value.size)
        assertEquals("Broken tap", vm.maintenance.value.first().title)
    }

    @Test
    fun `recordPayment calls onSuccess and reloads`() = runTest {
        coEvery { client.recordTenantPayment(any()) } returns ApiResponse(fakePayment)

        val vm = TenantPortalViewModel(client)

        var successCalled = false
        vm.recordPayment(
            RecordPaymentRequest(
                leaseId = "lease-001",
                amount = 1500.0,
                paymentType = PaymentType.RENT,
                status = PaymentStatus.RECEIVED,
                periodFrom = "2025-01-01",
                periodTo = "2025-01-31"
            ),
            onSuccess = { successCalled = true },
            onError = {}
        )

        assertTrue(successCalled)
        coVerify { client.recordTenantPayment(any()) }
    }

    @Test
    fun `recordPayment calls onError on failure`() = runTest {
        coEvery { client.recordTenantPayment(any()) } throws RuntimeException("payment failed")

        val vm = TenantPortalViewModel(client)

        var errorMsg: String? = null
        vm.recordPayment(
            RecordPaymentRequest(
                leaseId = "lease-001",
                amount = 1500.0,
                paymentType = PaymentType.RENT,
                status = PaymentStatus.RECEIVED,
                periodFrom = "2025-01-01",
                periodTo = "2025-01-31"
            ),
            onSuccess = {},
            onError = { errorMsg = it }
        )

        assertNotNull(errorMsg)
    }

    @Test
    fun `loading is false after init completes`() = runTest {
        val vm = TenantPortalViewModel(client)
        assertFalse(vm.loading.value)
    }
}
