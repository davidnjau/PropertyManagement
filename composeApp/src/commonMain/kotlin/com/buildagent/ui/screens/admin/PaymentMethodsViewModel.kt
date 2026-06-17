package com.buildagent.ui.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.PaymentMethodsConfig
import com.buildagent.shared.models.UpdateMpesaConfigRequest
import com.buildagent.shared.models.UpdatePaypalConfigRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentMethodsViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _config = MutableStateFlow<PaymentMethodsConfig?>(null)
    val config: StateFlow<PaymentMethodsConfig?> = _config

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadConfig() }

    fun loadConfig() {
        screenModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _config.value = client.getPaymentMethods().data
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleMethod(id: String, enabled: Boolean) {
        screenModelScope.launch {
            try {
                client.togglePaymentMethod(id, enabled)
                loadConfig()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun toggleBank(bankId: String, enabled: Boolean) {
        screenModelScope.launch {
            try {
                client.toggleBank(bankId, enabled)
                loadConfig()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateMpesa(request: UpdateMpesaConfigRequest, onSuccess: () -> Unit) {
        screenModelScope.launch {
            _saving.value = true
            try {
                client.updateMpesaConfig(request)
                loadConfig()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _saving.value = false
            }
        }
    }

    fun updatePaypal(request: UpdatePaypalConfigRequest, onSuccess: () -> Unit) {
        screenModelScope.launch {
            _saving.value = true
            try {
                client.updatePaypalConfig(request)
                loadConfig()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _saving.value = false
            }
        }
    }
}
