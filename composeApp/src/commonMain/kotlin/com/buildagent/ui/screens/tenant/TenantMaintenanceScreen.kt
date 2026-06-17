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
import com.buildagent.shared.models.CreateMaintenanceRequest
import com.buildagent.shared.models.MaintenanceCategory
import com.buildagent.shared.models.MaintenancePriority
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.components.StatusBadge
import com.buildagent.ui.components.maintenanceStatusBadge
import com.buildagent.ui.components.priorityBadge
import com.buildagent.ui.theme.*

// Tenant-friendly priority labels mapped to actual priorities
private data class PriorityOption(val label: String, val value: MaintenancePriority)
private val tenantPriorityOptions = listOf(
    PriorityOption("Low", MaintenancePriority.LOW),
    PriorityOption("Medium", MaintenancePriority.ROUTINE),
    PriorityOption("High", MaintenancePriority.URGENT),
    PriorityOption("Emergency", MaintenancePriority.EMERGENCY),
)

@Composable
fun TenantMaintenanceScreen() {
    val vm = koinInject<TenantPortalViewModel>()
    val maintenance by vm.maintenance.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadMaintenance() }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Maintenance", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (showForm) "Cancel" else "+ New Request")
            }
        }
        Spacer(Modifier.height(12.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }

        if (showForm) {
            TenantMaintenanceForm(
                onSubmit = { request ->
                    vm.createMaintenance(request, onSuccess = { showForm = false })
                }
            )
            Spacer(Modifier.height(20.dp))
        }

        Text("My Requests", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(10.dp))

        if (loading) {
            LoadingContent()
        } else if (maintenance.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No maintenance requests yet.", color = Gray500)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(maintenance) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(req.title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                StatusBadge(req.priority.name, priorityBadge)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(req.status.name, maintenanceStatusBadge)
                                Text(req.createdAt, fontSize = 11.sp, color = Gray500)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantMaintenanceForm(onSubmit: (CreateMaintenanceRequest) -> Unit) {
    val categories = MaintenanceCategory.entries.toList()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MaintenanceCategory.OTHER) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf(tenantPriorityOptions[1]) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("New Maintenance Request", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }

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
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedPriority.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = priorityExpanded, onDismissRequest = { priorityExpanded = false }) {
                    tenantPriorityOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.label) },
                            onClick = { selectedPriority = opt; priorityExpanded = false }
                        )
                    }
                }
            }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = { selectedCategory = cat; categoryExpanded = false }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = "Title is required."
                        return@Button
                    }
                    onSubmit(
                        CreateMaintenanceRequest(
                            unitId = "",
                            category = selectedCategory,
                            priority = selectedPriority.value,
                            title = title.trim(),
                            description = description.trim()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Submit Request")
            }
        }
    }
}
