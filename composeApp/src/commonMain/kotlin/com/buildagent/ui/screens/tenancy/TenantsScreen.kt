package com.buildagent.ui.screens.tenancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.buildagent.shared.models.CreateTenantWithLeaseRequest
import com.buildagent.shared.models.RentFrequency
import com.buildagent.ui.components.BuildingUnitPicker
import com.buildagent.ui.components.DatePickerField
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*

@Composable
fun TenantsScreen() {
    val vm = koinInject<TenancyViewModel>()
    val tenants by vm.tenants.collectAsState()
    val loading by vm.loading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tenants", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${tenants.size} tenants", fontSize = 13.sp, color = Gray500)
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ New Tenant")
            }
        }
        Spacer(Modifier.height(20.dp))

        if (loading) { LoadingContent() } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tenants) { tenant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        border = BorderStroke(1.dp, Gray300),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            Box(
                                modifier = Modifier.width(4.dp).fillMaxHeight()
                                    .background(Brand600)
                            )
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(tenant.fullName, fontWeight = FontWeight.Medium)
                                    Text(tenant.email, fontSize = 13.sp, color = Gray500)
                                    tenant.phone?.let { Text(it, fontSize = 12.sp, color = Gray500) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateTenantAndLeaseDialog(
            onDismiss = { showDialog = false },
            onSave = { request ->
                vm.createTenantWithLease(
                    request,
                    onSuccess = { showDialog = false },
                    onError = { }
                )
            }
        )
    }
}

@Composable
fun CreateTenantAndLeaseDialog(
    onDismiss: () -> Unit,
    onSave: (CreateTenantWithLeaseRequest) -> Unit
) {
    // Tenant fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    // Lease fields
    var selectedBuildingId by remember { mutableStateOf("") }
    var unitId by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var rentAmount by remember { mutableStateOf("") }
    var depositAmount by remember { mutableStateOf("") }
    var paymentDay by remember { mutableStateOf("1") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary,
        title = { Text("New Tenant & Lease", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }

                Text("Tenant Details", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray500)

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )

                HorizontalDivider()
                Text("Lease Details", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray500)

                BuildingUnitPicker(
                    selectedBuildingId = selectedBuildingId,
                    selectedUnitId = unitId,
                    onBuildingSelected = { id, _ -> selectedBuildingId = id; unitId = "" },
                    onUnitSelected = { id, _ -> unitId = id }
                )
                DatePickerField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = "Start Date *"
                )
                DatePickerField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = "End Date (optional)"
                )
                OutlinedTextField(
                    value = rentAmount,
                    onValueChange = { rentAmount = it },
                    label = { Text("Rent Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = depositAmount,
                    onValueChange = { depositAmount = it },
                    label = { Text("Deposit Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = paymentDay,
                    onValueChange = { paymentDay = it },
                    label = { Text("Payment Day of Month *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )

                Text(
                    "Portal access will be sent to the tenant's email/phone automatically.",
                    fontSize = 12.sp,
                    color = Gray500
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rent = rentAmount.toDoubleOrNull()
                    val bond = depositAmount.toDoubleOrNull()
                    val day = paymentDay.toIntOrNull()
                    when {
                        fullName.isBlank() || email.isBlank() -> errorMsg = "Full name and email are required."
                        unitId.isBlank() || startDate.isBlank() -> errorMsg = "Building, unit and start date are required."
                        rent == null || rent <= 0 -> errorMsg = "Enter a valid rent amount."
                        bond == null || bond < 0 -> errorMsg = "Enter a valid deposit amount."
                        day == null || day !in 1..28 -> errorMsg = "Payment day must be between 1 and 28."
                        else -> onSave(
                            CreateTenantWithLeaseRequest(
                                fullName = fullName.trim(),
                                email = email.trim(),
                                phone = phone.trim().ifBlank { null },
                                unitId = unitId.trim(),
                                startDate = startDate.trim(),
                                endDate = endDate.trim().ifBlank { null },
                                rentAmount = rent,
                                rentFrequency = RentFrequency.MONTHLY,
                                bondAmount = bond,
                                paymentDay = day
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
