package com.buildagent.ui.screens.admin

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
import com.buildagent.ui.utils.fmt1dp
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*

@Composable
fun AdminDocumentsScreen() {
    val vm = koinInject<AdminDocumentsViewModel>()
    val documents by vm.documents.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Documents", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0; vm.loadDocuments("TENANT") },
                text = { Text("Tenant Docs") }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1; vm.loadDocuments("BUILDING") },
                text = { Text("Building Docs") }
            )
        }
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Warning100)
        ) {
            Text(
                text = "Document uploads are managed via the web portal. Browse and delete documents here.",
                color = Warning600,
                fontSize = 13.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(Modifier.height(12.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp) }

        if (loading) {
            LoadingContent()
        } else if (documents.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No documents found.", color = Gray500)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(documents) { doc -> DocumentRow(doc, onDelete = { vm.deleteDocument(doc.id) {} }) }
            }
        }
    }
}

@Composable
fun DocumentRow(doc: Document, onDelete: () -> Unit) {
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
                Text("Type: ${doc.docType}  |  Size: ${formatFileSize(doc.fileSize)}", fontSize = 12.sp, color = Gray500)
                Text("Uploaded: ${doc.uploadedAt}", fontSize = 12.sp, color = Gray500)
            }
            Spacer(Modifier.width(12.dp))
            TextButton(onClick = onDelete) {
                Text("Delete", color = Danger600, fontSize = 13.sp)
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${(bytes / 1024.0).fmt1dp()} KB"
    return "${(bytes / (1024.0 * 1024)).fmt1dp()} MB"
}
