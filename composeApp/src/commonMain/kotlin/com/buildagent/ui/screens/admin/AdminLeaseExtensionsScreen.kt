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
        Text("Lease Extension Requests", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }

        TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
            tabStatuses.forEachIndexed { idx, status ->
                val count = requests.count { it.status == status }
                Tab(
                    selected = tab == idx,
                    onClick = { tab = idx },
                    text = { Text("$status ($count)", fontSize = 13.sp) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (loading) {
            LoadingContent()
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No ${tabStatuses[tab].lowercase()} requests.", color = Gray500)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Lease: …${request.leaseId.takeLast(8)}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Current end: ${request.currentEndDate}  →  Proposed: ${request.proposedEndDate}", fontSize = 13.sp, color = Gray700)
                request.durationMonths?.let { Text("Duration: $it months", fontSize = 12.sp, color = Gray500) }
                Text("Submitted: ${request.submittedAt}", fontSize = 12.sp, color = Gray500)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(request.status, extensionStatusBadge)
                if (onReview != null) {
                    OutlinedButton(onClick = onReview, shape = RoundedCornerShape(8.dp)) {
                        Text("Review", fontSize = 13.sp)
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
                ) {
                    Text("Approve")
                }
                Button(
                    onClick = { onResolve("REJECTED", agentNotes.trim().ifBlank { null }) },
                    enabled = !resolving,
                    colors = ButtonDefaults.buttonColors(containerColor = Danger600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reject")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
