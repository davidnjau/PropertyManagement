package com.buildagent.ui.screens.tenant

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.CreateLeaseExtensionRequest
import com.buildagent.shared.models.CreateMaintenanceRequest
import com.buildagent.shared.models.Document
import com.buildagent.shared.models.Lease
import com.buildagent.shared.models.MaintenanceRequest
import com.buildagent.shared.models.Payment
import com.buildagent.shared.models.TenantOverview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TenantPortalViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _overview = MutableStateFlow<TenantOverview?>(null)
    val overview: StateFlow<TenantOverview?> = _overview

    private val _lease = MutableStateFlow<Lease?>(null)
    val lease: StateFlow<Lease?> = _lease

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    private val _maintenance = MutableStateFlow<List<MaintenanceRequest>>(emptyList())
    val maintenance: StateFlow<List<MaintenanceRequest>> = _maintenance

    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadAll() }

    fun loadAll() {
        screenModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _overview.value = client.getTenantOverview().data
                _lease.value = client.getTenantLease().data
                _payments.value = client.getTenantPayments().data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMaintenance() {
        screenModelScope.launch {
            try { _maintenance.value = client.getTenantMaintenance().data ?: emptyList() }
            catch (e: Exception) { _error.value = e.message }
        }
    }

    fun loadDocuments() {
        screenModelScope.launch {
            try { _documents.value = client.getTenantDocuments().data ?: emptyList() }
            catch (e: Exception) { _error.value = e.message }
        }
    }

    fun createMaintenance(request: CreateMaintenanceRequest, onSuccess: () -> Unit) {
        screenModelScope.launch {
            try {
                client.createTenantMaintenance(request)
                loadMaintenance()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun submitLeaseExtension(request: CreateLeaseExtensionRequest, onSuccess: () -> Unit) {
        screenModelScope.launch {
            try {
                client.submitLeaseExtension(request)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
