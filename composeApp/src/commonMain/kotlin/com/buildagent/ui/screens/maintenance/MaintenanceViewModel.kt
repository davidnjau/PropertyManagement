package com.buildagent.ui.screens.maintenance

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.CreateMaintenanceRequest
import com.buildagent.shared.models.MaintenanceRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MaintenanceViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _requests = MutableStateFlow<List<MaintenanceRequest>>(emptyList())
    val requests: StateFlow<List<MaintenanceRequest>> = _requests

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init { load() }

    fun load(status: String? = null) {
        screenModelScope.launch {
            _loading.value = true
            try { _requests.value = client.getMaintenance(status).data }
            catch (e: Exception) { }
            finally { _loading.value = false }
        }
    }

    fun createMaintenance(request: CreateMaintenanceRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                client.createMaintenance(request)
                load()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create maintenance request.")
            }
        }
    }
}
