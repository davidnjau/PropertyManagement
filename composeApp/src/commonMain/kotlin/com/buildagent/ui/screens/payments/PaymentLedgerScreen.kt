package com.buildagent.ui.screens.payments

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
    val paymentTypes = PaymentType.entries.toList()
    var leaseId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PaymentType.RENT) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var periodFrom by remember { mutableStateOf("") }
    var periodTo by remember { mutableStateOf("") }
    var referenceNo by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }
                OutlinedTextField(
                    value = leaseId,
                    onValueChange = { leaseId = it },
                    label = { Text("Lease ID *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
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
                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text("Reference No (optional)") },
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
                    if (leaseId.isBlank() || parsedAmount == null || periodFrom.isBlank() || periodTo.isBlank()) {
                        errorMsg = "Lease ID, amount, and period dates are required."
                        return@Button
                    }
                    onSave(
                        RecordPaymentRequest(
                            leaseId = leaseId.trim(),
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (payment.status.name == "OVERDUE") Danger100 else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
