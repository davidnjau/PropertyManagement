package com.buildagent.ui.screens.maintenance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.buildagent.shared.models.CreateMaintenanceRequest
import com.buildagent.shared.models.MaintenanceCategory
import com.buildagent.shared.models.MaintenancePriority
import com.buildagent.shared.models.MaintenanceRequest
import com.buildagent.shared.models.MaintenanceStatus
import com.buildagent.ui.components.*
import com.buildagent.ui.theme.*

private val statusFilters = listOf("All", "REPORTED", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CLOSED")

@Composable
fun MaintenanceHubScreen() {
    val vm = koinInject<MaintenanceViewModel>()
    val requests by vm.requests.collectAsState()
    val loading by vm.loading.collectAsState()
    var filter by remember { mutableStateOf("All") }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Maintenance Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${requests.size} requests", fontSize = 13.sp, color = Gray500)
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ New Request")
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(statusFilters) { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f; vm.load(if (f == "All") null else f) },
                    label = { Text(f, fontSize = 12.sp) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (loading) { LoadingContent() } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(requests) { request -> MaintenanceCard(request) }
            }
        }
    }

    if (showDialog) {
        CreateMaintenanceDialog(
            onDismiss = { showDialog = false },
            onSave = { request ->
                vm.createMaintenance(
                    request,
                    onSuccess = { showDialog = false },
                    onError = { }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMaintenanceDialog(
    onDismiss: () -> Unit,
    onSave: (CreateMaintenanceRequest) -> Unit
) {
    val categories = MaintenanceCategory.entries.toList()
    val priorities = MaintenancePriority.entries.toList()
    var unitId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MaintenanceCategory.OTHER) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(MaintenancePriority.ROUTINE) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Maintenance Request", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }
                OutlinedTextField(
                    value = unitId,
                    onValueChange = { unitId = it },
                    label = { Text("Unit ID *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = { selectedCategory = cat; categoryExpanded = false }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPriority.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        priorities.forEach { pri ->
                            DropdownMenuItem(
                                text = { Text(pri.name) },
                                onClick = { selectedPriority = pri; priorityExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (unitId.isBlank() || title.isBlank() || description.isBlank()) {
                        errorMsg = "Unit ID, title, and description are required."
                        return@Button
                    }
                    onSave(
                        CreateMaintenanceRequest(
                            unitId = unitId.trim(),
                            category = selectedCategory,
                            priority = selectedPriority,
                            title = title.trim(),
                            description = description.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MaintenanceCard(request: MaintenanceRequest) {
    val closedStatuses = listOf(MaintenanceStatus.COMPLETED, MaintenanceStatus.CLOSED, MaintenanceStatus.CANCELLED)
    val slaBreached = request.slaTargetDate != null && request.status !in closedStatuses

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (slaBreached) Danger100 else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                StatusBadge(request.priority.name, priorityBadge)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(request.status.name, maintenanceStatusBadge)
                if (slaBreached) {
                    Text("SLA Breached", fontSize = 11.sp, color = Danger600, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(request.description, fontSize = 13.sp, color = Gray700, maxLines = 2)
            request.contractorName?.let {
                Spacer(Modifier.height(4.dp))
                Text("Contractor: $it", fontSize = 12.sp, color = Gray500)
            }
        }
    }
}
