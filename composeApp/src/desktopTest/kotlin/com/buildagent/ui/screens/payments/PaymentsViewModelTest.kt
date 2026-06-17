package com.buildagent.ui.screens.payments

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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val client = mockk<BuildAgentClient>()

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
        coEvery { client.getPayments() } returns ApiResponse(listOf(fakePayment))
        coEvery { client.getOverduePayments() } returns ApiResponse(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads payments`() = runTest {
        val vm = PaymentsViewModel(client)
        assertEquals(1, vm.payments.value.size)
        assertEquals("pay-001", vm.payments.value.first().id)
    }

    @Test
    fun `loadOverdue populates overduePayments`() = runTest {
        val overduePayment = fakePayment.copy(id = "pay-overdue", status = PaymentStatus.OVERDUE)
        coEvery { client.getOverduePayments() } returns ApiResponse(listOf(overduePayment))

        val vm = PaymentsViewModel(client)
        vm.loadOverdue()

        assertEquals(1, vm.overduePayments.value.size)
        assertEquals("pay-overdue", vm.overduePayments.value.first().id)
    }

    @Test
    fun `recordPayment calls onSuccess and refreshes list`() = runTest {
        coEvery { client.recordPayment(any()) } returns ApiResponse(fakePayment)

        val vm = PaymentsViewModel(client)

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
        coVerify { client.recordPayment(any()) }
    }

    @Test
    fun `recordPayment calls onError on failure`() = runTest {
        coEvery { client.recordPayment(any()) } throws RuntimeException("network failure")

        val vm = PaymentsViewModel(client)

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

        assertTrue(errorMsg != null)
    }

    @Test
    fun `loading is false after init completes`() = runTest {
        val vm = PaymentsViewModel(client)
        assertFalse(vm.loading.value)
    }
}
