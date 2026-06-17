package com.buildagent.ui.screens.admin

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
class AdminUsersViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val client = mockk<BuildAgentClient>()

    private val fakeUser = AdminUserResponse(
        id = "user-001",
        agencyId = "agency-001",
        email = "agent@agency.com",
        fullName = "Jane Agent",
        role = "AGENT",
        userType = "AGENT",
        isActive = true,
        createdAt = "2025-01-01T00:00:00Z"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { client.getAdminUsers() } returns ApiResponse(listOf(fakeUser))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads users`() = runTest {
        val vm = AdminUsersViewModel(client)
        assertEquals(1, vm.users.value.size)
        assertEquals("Jane Agent", vm.users.value.first().fullName)
    }

    @Test
    fun `init sets error when api fails`() = runTest {
        coEvery { client.getAdminUsers() } throws RuntimeException("unauthorized")

        val vm = AdminUsersViewModel(client)

        assertNotNull(vm.error.value)
        assertTrue(vm.users.value.isEmpty())
    }

    @Test
    fun `loading is false after init completes`() = runTest {
        val vm = AdminUsersViewModel(client)
        assertFalse(vm.loading.value)
    }

    @Test
    fun `createUser calls onSuccess and refreshes list`() = runTest {
        coEvery { client.createAdminUser(any()) } returns ApiResponse(fakeUser)

        val vm = AdminUsersViewModel(client)

        var successCalled = false
        vm.createUser(
            CreateUserRequest(
                userType = UserType.AGENT,
                fullName = "Jane Agent",
                email = "agent@agency.com",
                password = "Secret123!"
            ),
            onSuccess = { successCalled = true },
            onError = {}
        )

        assertTrue(successCalled)
        coVerify { client.createAdminUser(any()) }
    }

    @Test
    fun `createUser calls onError on failure`() = runTest {
        coEvery { client.createAdminUser(any()) } throws RuntimeException("email taken")

        val vm = AdminUsersViewModel(client)

        var errorMsg: String? = null
        vm.createUser(
            CreateUserRequest(
                userType = UserType.AGENT,
                fullName = "Dup User",
                email = "dup@agency.com",
                password = "Secret123!"
            ),
            onSuccess = {},
            onError = { errorMsg = it }
        )

        assertNotNull(errorMsg)
    }

    @Test
    fun `loadUsers clears error on success`() = runTest {
        coEvery { client.getAdminUsers() } throws RuntimeException("temporary error")
        val vm = AdminUsersViewModel(client)
        assertNotNull(vm.error.value)

        coEvery { client.getAdminUsers() } returns ApiResponse(listOf(fakeUser))
        vm.loadUsers()

        assertNull(vm.error.value)
        assertEquals(1, vm.users.value.size)
    }
}
