package com.buildagent.ui.screens.tenancy

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
import com.buildagent.shared.models.CreateLeaseRequest
import com.buildagent.shared.models.RentFrequency
import com.buildagent.ui.components.*
import com.buildagent.ui.utils.fmt0dp
import com.buildagent.ui.theme.*

@Composable
fun LeasesScreen() {
    val vm = koinInject<TenancyViewModel>()
    val leases by vm.leases.collectAsState()
    val tenants by vm.tenants.collectAsState()
    val loading by vm.loading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Leases", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ New Lease")
            }
        }
        Spacer(Modifier.height(20.dp))

        if (loading) { LoadingContent() } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(leases) { lease ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lease.tenant?.fullName ?: "—", fontWeight = FontWeight.Medium)
                                Text("A$${lease.rentAmount.fmt0dp()}/mo", fontSize = 13.sp, color = Gray500)
                                Text("${lease.startDate} — ${lease.endDate ?: "Periodic"}", fontSize = 12.sp, color = Gray500)
                            }
                            val displayStatus = (lease.computedStatus ?: lease.status).name
                            StatusBadge(displayStatus, leaseStatusBadge)
                        }
                    }
                }
                if (leases.isEmpty() && !loading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No leases yet.", color = Gray500)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateLeaseDialog(
            tenants = tenants.map { it.id to it.fullName },
            onDismiss = { showDialog = false },
            onSave = { request ->
                vm.createLease(
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
fun CreateLeaseDialog(
    tenants: List<Pair<String, String>>,
    preselectedTenantId: String? = null,
    onDismiss: () -> Unit,
    onSave: (CreateLeaseRequest) -> Unit
) {
    var unitId by remember { mutableStateOf("") }
    var selectedTenantId by remember { mutableStateOf(preselectedTenantId ?: tenants.firstOrNull()?.first ?: "") }
    var tenantMenuExpanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var rentAmount by remember { mutableStateOf("") }
    var bondAmount by remember { mutableStateOf("") }
    var paymentDay by remember { mutableStateOf("1") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val selectedTenantName = tenants.firstOrNull { it.first == selectedTenantId }?.second ?: "Select tenant"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Lease", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }

                if (tenants.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = tenantMenuExpanded,
                        onExpandedChange = { tenantMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTenantName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tenant *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenantMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = tenantMenuExpanded,
                            onDismissRequest = { tenantMenuExpanded = false }
                        ) {
                            tenants.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = { selectedTenantId = id; tenantMenuExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = selectedTenantId,
                        onValueChange = { selectedTenantId = it },
                        label = { Text("Tenant ID *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = unitId,
                    onValueChange = { unitId = it },
                    label = { Text("Unit ID *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (YYYY-MM-DD, optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rentAmount,
                    onValueChange = { rentAmount = it },
                    label = { Text("Rent Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bondAmount,
                    onValueChange = { bondAmount = it },
                    label = { Text("Bond Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = paymentDay,
                    onValueChange = { paymentDay = it },
                    label = { Text("Payment Day (1-28)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rent = rentAmount.toDoubleOrNull()
                    val bond = bondAmount.toDoubleOrNull()
                    val day = paymentDay.toIntOrNull() ?: 1
                    if (selectedTenantId.isBlank() || unitId.isBlank() || startDate.isBlank() || rent == null || bond == null) {
                        errorMsg = "Tenant, unit, start date, rent and bond are required."
                        return@Button
                    }
                    onSave(
                        CreateLeaseRequest(
                            unitId = unitId.trim(),
                            tenantId = selectedTenantId.trim(),
                            startDate = startDate.trim(),
                            endDate = endDate.trim().ifBlank { null },
                            rentAmount = rent,
                            bondAmount = bond,
                            paymentDay = day
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
