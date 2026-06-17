package com.buildagent.ui.screens.portfolio

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.Building
import com.buildagent.shared.models.CreateBuildingRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PortfolioViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _buildings = MutableStateFlow<List<Building>>(emptyList())
    val buildings: StateFlow<List<Building>> = _buildings

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadBuildings() }

    fun loadBuildings() {
        screenModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _buildings.value = client.getBuildings().data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun createBuilding(request: CreateBuildingRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                client.createBuilding(request)
                loadBuildings()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create building.")
            }
        }
    }

    suspend fun getBuilding(id: String) = client.getBuilding(id).data
    suspend fun getUnits(buildingId: String) = client.getUnits(buildingId).data
    suspend fun getUnit(id: String) = client.getUnit(id).data
}
