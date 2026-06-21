package com.buildagent.ui.screens.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.buildagent.shared.models.CreateLeaseExtensionRequest
import com.buildagent.shared.models.Lease
import com.buildagent.ui.components.DatePickerField
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.utils.fmt2dp
import com.buildagent.ui.components.StatusBadge
import com.buildagent.ui.components.leaseStatusBadge
import com.buildagent.ui.theme.*

private val durationOptions = listOf(3, 6, 12, 24)

@Composable
fun TenantLeaseScreen() {
    val vm = koinInject<TenantPortalViewModel>()
    val lease by vm.lease.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    var showExtendDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("My Lease", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }

        if (loading || lease == null) {
            LoadingContent()
            return@Column
        }

        val l = lease!!

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Lease Details", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    StatusBadge(l.status.name, leaseStatusBadge)
                }
                LeaseDetailRow("Unit ID", l.unitId)
                LeaseDetailRow("Start Date", l.startDate)
                LeaseDetailRow("End Date", l.endDate ?: "Ongoing (Periodic)")
                LeaseDetailRow("Rent Amount", "A$${l.rentAmount.fmt2dp()} / ${l.rentFrequency.name.lowercase()}")
                LeaseDetailRow("Bond Amount", "A$${l.bondAmount.fmt2dp()}")
                LeaseDetailRow("Payment Day", "Day ${l.paymentDay} of each period")
                l.specialConditions?.let { LeaseDetailRow("Special Conditions", it) }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { showExtendDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Brand600),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Extend Lease", fontWeight = FontWeight.SemiBold)
        }
    }

    if (showExtendDialog && lease != null) {
        ExtendLeaseDialog(
            leaseId = lease!!.id,
            onDismiss = { showExtendDialog = false },
            onSubmit = { request ->
                vm.submitLeaseExtension(request, onSuccess = { showExtendDialog = false })
            }
        )
    }
}

@Composable
private fun LeaseDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Gray500, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ExtendLeaseDialog(
    leaseId: String,
    onDismiss: () -> Unit,
    onSubmit: (CreateLeaseExtensionRequest) -> Unit
) {
    var selectedMonths by remember { mutableStateOf<Int?>(12) }
    var customDate by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Extend Lease", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }

                Text("Duration", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    durationOptions.forEach { months ->
                        FilterChip(
                            selected = !useCustom && selectedMonths == months,
                            onClick = { selectedMonths = months; useCustom = false },
                            label = { Text("${months}m") }
                        )
                    }
                    FilterChip(
                        selected = useCustom,
                        onClick = { useCustom = true; selectedMonths = null },
                        label = { Text("Custom") }
                    )
                }

                if (useCustom) {
                    DatePickerField(
                        value = customDate,
                        onValueChange = { customDate = it },
                        label = "End Date"
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (useCustom && customDate.isBlank()) {
                        errorMsg = "Enter a custom end date."
                        return@Button
                    }
                    if (!useCustom && selectedMonths == null) {
                        errorMsg = "Select a duration."
                        return@Button
                    }
                    onSubmit(
                        CreateLeaseExtensionRequest(
                            leaseId = leaseId,
                            durationMonths = if (!useCustom) selectedMonths else null,
                            customEndDate = if (useCustom) customDate.trim() else null,
                            notes = notes.trim().ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
