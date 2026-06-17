package com.buildagent.ui.screens.payments

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.Payment
import com.buildagent.shared.models.RecordPaymentRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentsViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments

    private val _overduePayments = MutableStateFlow<List<Payment>>(emptyList())
    val overduePayments: StateFlow<List<Payment>> = _overduePayments

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init { loadPayments() }

    fun loadPayments() {
        screenModelScope.launch {
            _loading.value = true
            try { _payments.value = client.getPayments().data ?: emptyList() }
            catch (e: Exception) { }
            finally { _loading.value = false }
        }
    }

    fun loadOverdue() {
        screenModelScope.launch {
            _loading.value = true
            try { _overduePayments.value = client.getOverduePayments().data ?: emptyList() }
            catch (e: Exception) { }
            finally { _loading.value = false }
        }
    }

    fun recordPayment(request: RecordPaymentRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        screenModelScope.launch {
            try {
                client.recordPayment(request)
                loadPayments()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to record payment.")
            }
        }
    }
}
