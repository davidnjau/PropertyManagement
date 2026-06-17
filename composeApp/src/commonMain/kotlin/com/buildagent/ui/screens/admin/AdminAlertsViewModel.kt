package com.buildagent.ui.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.Alert
import com.buildagent.shared.models.CreateAlertRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminAlertsViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadAlerts() }

    fun loadAlerts() {
        screenModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _alerts.value = client.getAlerts().data
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun createAlert(request: CreateAlertRequest, onSuccess: () -> Unit) {
        screenModelScope.launch {
            _sending.value = true
            try {
                client.createAlert(request)
                loadAlerts()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _sending.value = false
            }
        }
    }
}
