package com.buildagent.ui.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.Document
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminDocumentsViewModel(private val client: BuildAgentClient) : ScreenModel {
    private val _documents = MutableStateFlow<List<Document>>(emptyList())
    val documents: StateFlow<List<Document>> = _documents

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init { loadDocuments() }

    fun loadDocuments(entityType: String? = null) {
        screenModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _documents.value = client.getAdminDocuments(entityType).data ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteDocument(id: String, onSuccess: () -> Unit) {
        screenModelScope.launch {
            try {
                client.deleteAdminDocument(id)
                _documents.value = _documents.value.filter { it.id != id }
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
