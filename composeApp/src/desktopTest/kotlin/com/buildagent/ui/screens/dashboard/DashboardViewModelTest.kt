package com.buildagent.ui.screens.dashboard

import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.ApiResponse
import com.buildagent.shared.models.DashboardData
import com.buildagent.shared.models.UnitStats
import io.mockk.coEvery
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val client = mockk<BuildAgentClient>()

    private val fakeDashboard = DashboardData(
        buildings = 2,
        units = UnitStats(total = 10, occupied = 8, vacant = 2),
        occupancyRate = 80,
        overduePayments = 1,
        expiringLeases = 2,
        openMaintenance = 3,
        slaBreached = 0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load transitions to Success when data returned`() = runTest {
        coEvery { client.getAgentDashboard() } returns ApiResponse(fakeDashboard)

        val vm = DashboardViewModel(client)

        val state = vm.state.value
        assertIs<DashboardUiState.Success>(state)
        assertEquals(10, (state as DashboardUiState.Success).data.units.total)
    }

    @Test
    fun `load transitions to Error on exception`() = runTest {
        coEvery { client.getAgentDashboard() } throws RuntimeException("network error")

        val vm = DashboardViewModel(client)

        val state = vm.state.value
        assertIs<DashboardUiState.Error>(state)
        assertNotNull((state as DashboardUiState.Error).message)
    }

    @Test
    fun `load transitions to Error when data is null`() = runTest {
        coEvery { client.getAgentDashboard() } returns ApiResponse(null)

        val vm = DashboardViewModel(client)

        assertIs<DashboardUiState.Error>(vm.state.value)
    }

    @Test
    fun `reload refreshes data`() = runTest {
        coEvery { client.getAgentDashboard() } returns ApiResponse(fakeDashboard)

        val vm = DashboardViewModel(client)
        vm.load()

        assertIs<DashboardUiState.Success>(vm.state.value)
    }
}
