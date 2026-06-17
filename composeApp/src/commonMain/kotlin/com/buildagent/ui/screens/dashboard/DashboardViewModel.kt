package com.buildagent.ui.screens.dashboard

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.DashboardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state

    init { load() }

    fun load() {
        screenModelScope.launch {
            _state.value = DashboardUiState.Loading
            try {
                val data = client.getAgentDashboard()
                _state.value = DashboardUiState.Success(data.data ?: error(data.message ?: "No data"))
            } catch (e: Exception) {
                _state.value = DashboardUiState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }
}
