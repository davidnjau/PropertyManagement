package com.buildagent.ui.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.AdminUserResponse
import com.buildagent.shared.models.CreateUserRequest
import com.buildagent.shared.models.UserType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminUsersViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _users = MutableStateFlow<List<AdminUserResponse>>(emptyList())
    val users: StateFlow<List<AdminUserResponse>> = _users

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadUsers() }

    fun loadUsers() {
        screenModelScope.launch {
            _loading.value = true
            _error.value = null
            try { _users.value = client.getAdminUsers().data ?: emptyList() }
            catch (e: Exception) { _error.value = e.message }
            finally { _loading.value = false }
        }
    }

    fun createUser(request: CreateUserRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                client.createAdminUser(request)
                loadUsers()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create user")
            }
        }
    }
}
