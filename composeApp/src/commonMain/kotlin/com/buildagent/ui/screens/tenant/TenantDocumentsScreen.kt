package com.buildagent.ui.screens.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.buildagent.shared.models.Document
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*
import com.buildagent.ui.utils.fmt1dp

@Composable
fun TenantDocumentsScreen() {
    val vm = koinInject<TenantPortalViewModel>()
    val documents by vm.documents.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(Unit) { vm.loadDocuments() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Documents", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Gray100)
        ) {
            Text(
                "Document uploads are handled by your agent.",
                color = Gray500,
                fontSize = 13.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }

        if (loading) {
            LoadingContent()
        } else if (documents.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No documents available.", color = Gray500)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(documents) { doc -> TenantDocumentRow(doc) }
            }
        }
    }
}

@Composable
fun TenantDocumentRow(doc: Document) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(doc.fileName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("Type: ${doc.docType}  |  Size: ${formatDocSize(doc.fileSize)}", fontSize = 12.sp, color = Gray500)
                Text("Uploaded: ${doc.uploadedAt}", fontSize = 12.sp, color = Gray500)
            }
            Text(
                text = "📥",
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

private fun formatDocSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${(bytes / 1024.0).fmt1dp()} KB"
    return "${(bytes / (1024.0 * 1024)).fmt1dp()} MB"
}
