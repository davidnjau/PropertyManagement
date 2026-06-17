package com.buildagent.ui.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.LeaseExtensionRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminLeaseExtensionsViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _requests = MutableStateFlow<List<LeaseExtensionRequest>>(emptyList())
    val requests: StateFlow<List<LeaseExtensionRequest>> = _requests

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _resolving = MutableStateFlow(false)
    val resolving: StateFlow<Boolean> = _resolving

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadRequests() }

    fun loadRequests() {
        screenModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _requests.value = client.getLeaseExtensions().data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun resolve(id: String, status: String, agentNotes: String?, onSuccess: () -> Unit) {
        screenModelScope.launch {
            _resolving.value = true
            try {
                client.resolveLeaseExtension(id, status, agentNotes)
                loadRequests()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _resolving.value = false
            }
        }
    }
}
