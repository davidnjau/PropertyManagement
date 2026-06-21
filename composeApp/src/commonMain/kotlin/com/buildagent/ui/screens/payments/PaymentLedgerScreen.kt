package com.buildagent.ui.screens.payments

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
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.Building
import com.buildagent.shared.models.Lease
import com.buildagent.shared.models.Payment
import com.buildagent.shared.models.PaymentType
import com.buildagent.shared.models.RecordPaymentRequest
import com.buildagent.ui.components.*
import com.buildagent.ui.theme.*
import com.buildagent.ui.utils.fmt2dp

@Composable
fun PaymentLedgerScreen() {
    val vm = koinInject<PaymentsViewModel>()
    val payments by vm.payments.collectAsState()
    val overdue by vm.overduePayments.collectAsState()
    val loading by vm.loading.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Payment Ledger", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Record Payment")
            }
        }
        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
            Tab(selected = tab == 0, onClick = { tab = 0; vm.loadPayments() }, text = { Text("All Payments") })
            Tab(selected = tab == 1, onClick = { tab = 1; vm.loadOverdue() }, text = { Text("Overdue") })
        }
        Spacer(Modifier.height(16.dp))

        val list = if (tab == 0) payments else overdue
        if (loading) { LoadingContent() } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { payment -> PaymentRow(payment) }
                if (list.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No payments found", color = Gray500)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        RecordPaymentDialog(
            onDismiss = { showDialog = false },
            onSave = { request ->
                vm.recordPayment(
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
fun RecordPaymentDialog(
    onDismiss: () -> Unit,
    onSave: (RecordPaymentRequest) -> Unit
) {
    val client = koinInject<BuildAgentClient>()
    val paymentTypes = PaymentType.entries.toList()

    var buildings by remember { mutableStateOf<List<Building>>(emptyList()) }
    var allLeases by remember { mutableStateOf<List<Lease>>(emptyList()) }
    var selectedBuildingId by remember { mutableStateOf("") }
    var buildingExpanded by remember { mutableStateOf(false) }
    var selectedLeaseId by remember { mutableStateOf("") }
    var leaseExpanded by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PaymentType.RENT) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var periodFrom by remember { mutableStateOf("") }
    var periodTo by remember { mutableStateOf("") }

    // Payment method: "MPESA", "PAYPAL", "BANK"
    var paymentMethod by remember { mutableStateOf("MPESA") }
    var referenceNo by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        buildings = try { client.getBuildings(limit = 100).data ?: emptyList() } catch (e: Exception) { emptyList() }
        allLeases = try { client.getLeases(limit = 100).data ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    val leasesForBuilding = if (selectedBuildingId.isBlank()) emptyList()
        else allLeases.filter { it.unit?.building?.id == selectedBuildingId }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary,
        title = { Text("Record Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }

                // Building selector
                ExposedDropdownMenuBox(expanded = buildingExpanded, onExpandedChange = { buildingExpanded = it }) {
                    OutlinedTextField(
                        value = buildings.firstOrNull { it.id == selectedBuildingId }?.let { it.name ?: it.address } ?: "Select Building",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Building *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                    ExposedDropdownMenu(expanded = buildingExpanded, onDismissRequest = { buildingExpanded = false }) {
                        if (buildings.isEmpty()) {
                            DropdownMenuItem(text = { Text("No buildings available") }, onClick = {})
                        } else {
                            buildings.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b.name ?: b.address) },
                                    onClick = { selectedBuildingId = b.id; selectedLeaseId = ""; buildingExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Tenant/lease selector
                if (selectedBuildingId.isNotBlank()) {
                    ExposedDropdownMenuBox(expanded = leaseExpanded, onExpandedChange = { leaseExpanded = it }) {
                        OutlinedTextField(
                            value = leasesForBuilding.firstOrNull { it.id == selectedLeaseId }?.tenant?.fullName ?: "Select Tenant",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tenant *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = leaseExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                        )
                        ExposedDropdownMenu(expanded = leaseExpanded, onDismissRequest = { leaseExpanded = false }) {
                            if (leasesForBuilding.isEmpty()) {
                                DropdownMenuItem(text = { Text("No active leases for this building") }, onClick = {})
                            } else {
                                leasesForBuilding.forEach { lease ->
                                    DropdownMenuItem(
                                        text = { Text(lease.tenant?.fullName ?: lease.id) },
                                        onClick = { selectedLeaseId = lease.id; leaseExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        paymentTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = { selectedType = type; typeMenuExpanded = false }
                            )
                        }
                    }
                }
                DatePickerField(
                    value = periodFrom,
                    onValueChange = { periodFrom = it },
                    label = "Period From *"
                )
                DatePickerField(
                    value = periodTo,
                    onValueChange = { periodTo = it },
                    label = "Period To *"
                )

                // Payment method chips
                Text("Payment Method", fontSize = 13.sp, color = Gray700, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MPESA" to "M-Pesa", "PAYPAL" to "PayPal", "BANK" to "Bank Transfer").forEach { (key, label) ->
                        FilterChip(
                            selected = paymentMethod == key,
                            onClick = { paymentMethod = key; referenceNo = "" },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                val refLabel = when (paymentMethod) {
                    "MPESA" -> "M-Pesa Reference"
                    "PAYPAL" -> "Transaction ID"
                    else -> "Transfer Reference"
                }
                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text("$refLabel (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (selectedLeaseId.isBlank() || parsedAmount == null || periodFrom.isBlank() || periodTo.isBlank()) {
                        errorMsg = "Building, tenant, amount, and period dates are required."
                        return@Button
                    }
                    onSave(
                        RecordPaymentRequest(
                            leaseId = selectedLeaseId,
                            amount = parsedAmount,
                            paymentType = selectedType,
                            periodFrom = periodFrom.trim(),
                            periodTo = periodTo.trim(),
                            referenceNo = referenceNo.trim().ifBlank { null }
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
fun PaymentRow(payment: Payment) {
    val isOverdue = payment.status.name == "OVERDUE"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isOverdue) Danger100 else White),
        border = BorderStroke(1.dp, if (isOverdue) Danger100 else Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.width(4.dp).fillMaxHeight()
                    .background(if (isOverdue) Danger600 else Gray300)
            )
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(payment.lease?.tenant?.fullName ?: "—", fontWeight = FontWeight.Medium)
                    Text(payment.paymentType.name, fontSize = 12.sp, color = Gray500)
                    Text("${payment.periodFrom} — ${payment.periodTo}", fontSize = 12.sp, color = Gray500)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("A$${payment.amount.fmt2dp()}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    StatusBadge(payment.status.name, paymentStatusBadge)
                }
            }
        }
    }
}
