package com.buildagent.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
        Column {
            Text("Documents", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Text("Browse and manage uploaded files", fontSize = 13.sp, color = Gray500)
        }
        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = tab, containerColor = White, contentColor = Brand600) {
            Tab(selected = tab == 0, onClick = { tab = 0; vm.loadDocuments("TENANT") },
                text = { Text("Tenant Docs", fontWeight = if (tab == 0) FontWeight.SemiBold else FontWeight.Normal) })
            Tab(selected = tab == 1, onClick = { tab = 1; vm.loadDocuments("BUILDING") },
                text = { Text("Building Docs", fontWeight = if (tab == 1) FontWeight.SemiBold else FontWeight.Normal) })
        }
        Spacer(Modifier.height(12.dp))

        // Info banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Brand50),
            border = BorderStroke(1.dp, Brand100),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("ℹ️", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Document uploads are managed via the web portal. Browse and delete documents here.",
                    color = Brand600,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp) }

        if (loading) {
            LoadingContent()
        } else if (documents.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📁", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No documents found.", color = Gray500, fontSize = 14.sp)
                }
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
    val ext = doc.fileName.substringAfterLast('.', "").uppercase()
    val iconEmoji = when (ext) {
        "PDF" -> "📄"
        "DOC", "DOCX" -> "📝"
        "XLS", "XLSX" -> "📊"
        "JPG", "JPEG", "PNG" -> "🖼️"
        else -> "📎"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(Brand600))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Brand50) {
                        Text(iconEmoji, modifier = Modifier.padding(8.dp), fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(doc.fileName, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Gray900)
                        Text(
                            "${doc.docType}  ·  ${formatFileSize(doc.fileSize)}",
                            fontSize = 12.sp, color = Gray500
                        )
                        Text("Uploaded: ${doc.uploadedAt.take(10)}", fontSize = 11.sp, color = Gray500)
                    }
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = Danger600, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${(bytes / 1024.0).fmt1dp()} KB"
    return "${(bytes / (1024.0 * 1024)).fmt1dp()} MB"
}
