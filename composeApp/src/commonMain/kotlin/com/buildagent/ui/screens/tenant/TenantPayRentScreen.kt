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
import com.buildagent.shared.models.PaymentType
import com.buildagent.shared.models.RecordPaymentRequest
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.components.StatusBadge
import com.buildagent.ui.components.paymentStatusBadge
import com.buildagent.ui.theme.*
import com.buildagent.ui.utils.fmt2dp

@Composable
fun TenantPayRentScreen() {
    val vm = koinInject<TenantPortalViewModel>()
    val lease by vm.lease.collectAsState()
    val payments by vm.payments.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    var showPayDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pay Rent", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (lease != null) {
                Button(
                    onClick = { showPayDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Record Payment")
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }

        // Payment instructions note card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Brand100)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("How to Pay", fontWeight = FontWeight.SemiBold, color = Brand600, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Pay via M-Pesa Paybill or bank transfer as configured by your agent. " +
                    "Contact your agent if you need payment details.",
                    fontSize = 13.sp,
                    color = Gray700
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Payment History", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        if (loading) {
            LoadingContent()
        } else if (payments.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                Text("No payment records found.", color = Gray500)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(payments) { payment ->
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
                                Text(
                                    "${payment.periodFrom} – ${payment.periodTo}",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(payment.paymentType.name, fontSize = 12.sp, color = Gray500)
                                payment.referenceNo?.let { Text("Ref: $it", fontSize = 11.sp, color = Gray500) }
                                payment.paymentDate?.let { Text("Date: $it", fontSize = 11.sp, color = Gray500) }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("A$${payment.amount.fmt2dp()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                StatusBadge(payment.status.name, paymentStatusBadge)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPayDialog && lease != null) {
        RecordTenantPaymentDialog(
            leaseId = lease!!.id,
            defaultAmount = lease!!.rentAmount,
            onDismiss = { showPayDialog = false },
            onSave = { request ->
                vm.recordPayment(
                    request,
                    onSuccess = { showPayDialog = false },
                    onError = { }
                )
            }
        )
    }
}

@Composable
fun RecordTenantPaymentDialog(
    leaseId: String,
    defaultAmount: Double,
    onDismiss: () -> Unit,
    onSave: (RecordPaymentRequest) -> Unit
) {
    var amount by remember { mutableStateOf(defaultAmount.fmt2dp()) }
    var periodFrom by remember { mutableStateOf("") }
    var periodTo by remember { mutableStateOf("") }
    var referenceNo by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Rent Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = periodFrom,
                    onValueChange = { periodFrom = it },
                    label = { Text("Period From (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = periodTo,
                    onValueChange = { periodTo = it },
                    label = { Text("Period To (YYYY-MM-DD) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text("Reference No (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || periodFrom.isBlank() || periodTo.isBlank()) {
                        errorMsg = "Amount and period dates are required."
                        return@Button
                    }
                    onSave(
                        RecordPaymentRequest(
                            leaseId = leaseId,
                            amount = parsedAmount,
                            paymentType = PaymentType.RENT,
                            periodFrom = periodFrom.trim(),
                            periodTo = periodTo.trim(),
                            referenceNo = referenceNo.trim().ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
