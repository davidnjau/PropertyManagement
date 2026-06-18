package com.buildagent.ui.screens.tenancy

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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TenancyViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val client = mockk<BuildAgentClient>()

    private val fakeTenant = Tenant(
        id = "tenant-001",
        agencyId = "agency-001",
        fullName = "Alice",
        email = "alice@test.com",
        createdAt = "2025-01-01T00:00:00Z",
        updatedAt = "2025-01-01T00:00:00Z"
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { client.getTenants() } returns ApiResponse(listOf(fakeTenant))
        coEvery { client.getLeases() } returns ApiResponse(listOf(fakeLease))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads tenants and leases`() = runTest {
        val vm = TenancyViewModel(client)

        assertEquals(1, vm.tenants.value.size)
        assertEquals("Alice", vm.tenants.value.first().fullName)
        assertEquals(1, vm.leases.value.size)
    }

    @Test
    fun `createTenant calls onSuccess with tenant id`() = runTest {
        coEvery { client.createTenant(any()) } returns ApiResponse(fakeTenant)
        val vm = TenancyViewModel(client)

        var successId: String? = null
        vm.createTenant(
            CreateTenantRequest(fullName = "Alice", email = "alice@test.com"),
            onSuccess = { successId = it },
            onError = {}
        )

        assertEquals("tenant-001", successId)
        coVerify { client.createTenant(any()) }
    }

    @Test
    fun `createTenant calls onError on failure`() = runTest {
        coEvery { client.createTenant(any()) } throws RuntimeException("server error")
        val vm = TenancyViewModel(client)

        var errorMsg: String? = null
        vm.createTenant(
            CreateTenantRequest(fullName = "Bad", email = "bad@test.com"),
            onSuccess = {},
            onError = { errorMsg = it }
        )

        assertTrue(errorMsg != null)
    }

    @Test
    fun `createTenantWithLease calls onSuccess and reloads both tenants and leases`() = runTest {
        val newTenant = fakeTenant.copy(id = "tenant-002", fullName = "Dana")
        val newLease = fakeLease.copy(id = "lease-002", tenantId = "tenant-002")
        coEvery { client.createTenantWithLease(any()) } returns ApiResponse(
            TenantWithLeaseResponse(newTenant, newLease)
        )
        coEvery { client.getTenants() } returns ApiResponse(listOf(fakeTenant, newTenant))
        coEvery { client.getLeases() } returns ApiResponse(listOf(fakeLease, newLease))
        val vm = TenancyViewModel(client)

        var successCalled = false
        vm.createTenantWithLease(
            CreateTenantWithLeaseRequest(
                fullName = "Dana",
                email = "dana@test.com",
                unitId = "unit-002",
                startDate = "2025-01-01",
                rentAmount = 1800.0,
                rentFrequency = RentFrequency.MONTHLY,
                bondAmount = 3600.0,
                paymentDay = 5
            ),
            onSuccess = { successCalled = true },
            onError = {}
        )

        assertTrue(successCalled)
        coVerify { client.createTenantWithLease(any()) }
        assertEquals(2, vm.tenants.value.size)
        assertEquals(2, vm.leases.value.size)
    }

    @Test
    fun `createTenantWithLease calls onError on failure`() = runTest {
        coEvery { client.createTenantWithLease(any()) } throws RuntimeException("unit not found")
        val vm = TenancyViewModel(client)

        var errorMsg: String? = null
        vm.createTenantWithLease(
            CreateTenantWithLeaseRequest(
                fullName = "Dana",
                email = "dana@test.com",
                unitId = "bad-unit",
                startDate = "2025-01-01",
                rentAmount = 1800.0,
                rentFrequency = RentFrequency.MONTHLY,
                bondAmount = 3600.0,
                paymentDay = 5
            ),
            onSuccess = {},
            onError = { errorMsg = it }
        )

        assertTrue(errorMsg != null)
    }

    @Test
    fun `createLease calls onSuccess and reloads leases`() = runTest {
        val newLease = fakeLease.copy(id = "lease-002")
        coEvery { client.createLease(any()) } returns ApiResponse(newLease)
        coEvery { client.getLeases() } returns ApiResponse(listOf(fakeLease, newLease))
        val vm = TenancyViewModel(client)

        var successCalled = false
        vm.createLease(
            CreateLeaseRequest(
                unitId = "unit-001",
                tenantId = "tenant-001",
                startDate = "2025-01-01",
                rentAmount = 1500.0,
                rentFrequency = RentFrequency.MONTHLY,
                bondAmount = 3000.0,
                paymentDay = 1
            ),
            onSuccess = { successCalled = true },
            onError = {}
        )

        assertTrue(successCalled)
        assertEquals(2, vm.leases.value.size)
    }
}
