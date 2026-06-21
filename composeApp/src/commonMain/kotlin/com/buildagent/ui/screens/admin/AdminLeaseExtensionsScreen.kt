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
import com.buildagent.shared.models.LeaseExtensionRequest
import com.buildagent.ui.components.BadgeConfig
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.components.StatusBadge
import com.buildagent.ui.theme.*

private val extensionStatusBadge = mapOf(
    "PENDING"  to BadgeConfig("Pending",  Warning100, Warning600),
    "APPROVED" to BadgeConfig("Approved", Success100, Success600),
    "REJECTED" to BadgeConfig("Rejected", Danger100,  Danger600),
)

private val tabStatuses = listOf("PENDING", "APPROVED", "REJECTED")

@Composable
fun AdminLeaseExtensionsScreen() {
    val vm = koinInject<AdminLeaseExtensionsViewModel>()
    val requests by vm.requests.collectAsState()
    val loading by vm.loading.collectAsState()
    val resolving by vm.resolving.collectAsState()
    val error by vm.error.collectAsState()
    var tab by remember { mutableIntStateOf(0) }
    var reviewTarget by remember { mutableStateOf<LeaseExtensionRequest?>(null) }

    val filtered = requests.filter { it.status == tabStatuses[tab] }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column {
            Text("Lease Extension Requests", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Text("Review and action tenant extension requests", fontSize = 13.sp, color = Gray500)
        }
        Spacer(Modifier.height(16.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }

        TabRow(selectedTabIndex = tab, containerColor = White, contentColor = Brand600) {
            tabStatuses.forEachIndexed { idx, status ->
                val count = requests.count { it.status == status }
                Tab(
                    selected = tab == idx,
                    onClick = { tab = idx },
                    text = {
                        Text(
                            "${status.lowercase().replaceFirstChar { it.uppercase() }} ($count)",
                            fontSize = 13.sp,
                            fontWeight = if (tab == idx) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (loading) {
            LoadingContent()
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No ${tabStatuses[tab].lowercase()} requests.",
                        color = Gray500, fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { req ->
                    LeaseExtensionCard(
                        request = req,
                        onReview = if (req.status == "PENDING") ({ reviewTarget = req }) else null
                    )
                }
            }
        }
    }

    reviewTarget?.let { req ->
        ReviewLeaseExtensionDialog(
            request = req,
            resolving = resolving,
            onDismiss = { reviewTarget = null },
            onResolve = { status, notes ->
                vm.resolve(req.id, status, notes, onSuccess = { reviewTarget = null })
            }
        )
    }
}

@Composable
fun LeaseExtensionCard(request: LeaseExtensionRequest, onReview: (() -> Unit)?) {
    val accentColor = when (request.status) {
        "APPROVED" -> Success600
        "REJECTED" -> Danger600
        else -> Warning600
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accentColor))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Lease …${request.leaseId.takeLast(8)}",
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(request.currentEndDate, fontSize = 13.sp, color = Gray700)
                        Text("  →  ", fontSize = 13.sp, color = Gray500)
                        Text(request.proposedEndDate, fontSize = 13.sp, color = Brand600, fontWeight = FontWeight.Medium)
                    }
                    request.durationMonths?.let {
                        Text("Duration: $it months", fontSize = 12.sp, color = Gray500)
                    }
                    Text("Submitted: ${request.submittedAt.take(10)}", fontSize = 11.sp, color = Gray500)
                }
                Spacer(Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(request.status, extensionStatusBadge)
                    if (onReview != null) {
                        Button(
                            onClick = onReview,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Review", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewLeaseExtensionDialog(
    request: LeaseExtensionRequest,
    resolving: Boolean,
    onDismiss: () -> Unit,
    onResolve: (String, String?) -> Unit
) {
    var agentNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary,
        title = { Text("Review Extension Request", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Lease ID: ${request.leaseId}", fontSize = 13.sp, color = Gray700)
                Text("Current end date: ${request.currentEndDate}", fontSize = 13.sp)
                Text("Proposed end date: ${request.proposedEndDate}", fontSize = 13.sp)
                request.durationMonths?.let { Text("Duration: $it months", fontSize = 13.sp) }
                request.notes?.let { Text("Tenant notes: $it", fontSize = 13.sp, color = Gray500) }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = agentNotes,
                    onValueChange = { agentNotes = it },
                    label = { Text("Agent Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onResolve("APPROVED", agentNotes.trim().ifBlank { null }) },
                    enabled = !resolving,
                    colors = ButtonDefaults.buttonColors(containerColor = Success600),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Approve") }
                Button(
                    onClick = { onResolve("REJECTED", agentNotes.trim().ifBlank { null }) },
                    enabled = !resolving,
                    colors = ButtonDefaults.buttonColors(containerColor = Danger600),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Reject") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
