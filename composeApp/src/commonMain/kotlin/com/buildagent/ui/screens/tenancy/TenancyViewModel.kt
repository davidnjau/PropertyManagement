package com.buildagent.ui.screens.tenancy

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.CreateLeaseRequest
import com.buildagent.shared.models.CreateTenantRequest
import com.buildagent.shared.models.Lease
import com.buildagent.shared.models.Tenant
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TenancyViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _tenants = MutableStateFlow<List<Tenant>>(emptyList())
    val tenants: StateFlow<List<Tenant>> = _tenants

    private val _leases = MutableStateFlow<List<Lease>>(emptyList())
    val leases: StateFlow<List<Lease>> = _leases

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        loadTenants()
        loadLeases()
    }

    fun loadTenants() {
        screenModelScope.launch {
            _loading.value = true
            try { _tenants.value = client.getTenants().data ?: emptyList() }
            catch (e: Exception) { }
            finally { _loading.value = false }
        }
    }

    fun loadLeases() {
        screenModelScope.launch {
            try { _leases.value = client.getLeases().data ?: emptyList() }
            catch (e: Exception) { }
        }
    }

    fun createTenant(request: CreateTenantRequest, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                val tenant = client.createTenant(request).data ?: error("No tenant returned")
                loadTenants()
                onSuccess(tenant.id)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create tenant.")
            }
        }
    }

    fun createLease(request: CreateLeaseRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                client.createLease(request)
                loadLeases()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create lease.")
            }
        }
    }
}
