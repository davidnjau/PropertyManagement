package com.buildagent.ui.screens.tenancy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.buildagent.shared.models.CreateTenantWithLeaseRequest
import com.buildagent.shared.models.Lease
import com.buildagent.shared.models.LeaseStatus
import com.buildagent.shared.models.RentFrequency
import com.buildagent.shared.models.Tenant
import com.buildagent.ui.components.BuildingUnitPicker
import com.buildagent.ui.components.DatePickerField
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*
import com.buildagent.ui.utils.fmt2dp

@Composable
fun TenantsScreen() {
    val vm = koinInject<TenancyViewModel>()
    val tenants by vm.tenants.collectAsState()
    val leases by vm.leases.collectAsState()
    val loading by vm.loading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedTenant by remember { mutableStateOf<Tenant?>(null) }

    if (selectedTenant != null) {
        TenantDetailView(
            tenant = selectedTenant!!,
            leases = leases.filter { it.tenantId == selectedTenant!!.id },
            onBack = { selectedTenant = null }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tenants", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
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

        if (loading) {
            LoadingContent()
        } else if (tenants.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👥", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No tenants yet", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gray700)
                    Spacer(Modifier.height(4.dp))
                    Text("Add your first tenant to get started.", fontSize = 13.sp, color = Gray500)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tenants) { tenant ->
                    TenantCard(tenant = tenant, onClick = { selectedTenant = tenant })
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
private fun TenantCard(tenant: Tenant, onClick: () -> Unit) {
    val isActive = tenant.isActive
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.width(4.dp).fillMaxHeight()
                    .background(if (isActive) Brand600 else Gray300)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(40.dp)
                            .background(if (isActive) Brand50 else Gray100, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tenant.fullName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isActive) Brand600 else Gray500
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(tenant.fullName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900)
                        Text(tenant.email, fontSize = 13.sp, color = Gray500)
                        tenant.phone?.let { Text(it, fontSize = 12.sp, color = Gray500) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isActive) Brand100 else Gray100
                    ) {
                        Text(
                            if (isActive) "Active" else "Inactive",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isActive) Brand600 else Gray500
                        )
                    }
                    Text("›", fontSize = 20.sp, color = Gray300)
                }
            }
        }
    }
}

@Composable
private fun TenantDetailView(
    tenant: Tenant,
    leases: List<Lease>,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Header with back
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Gray100,
                modifier = Modifier.clickable { onBack() }
            ) {
                Text(
                    "< Back",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp, color = Brand600, fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(tenant.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text(tenant.email, fontSize = 13.sp, color = Gray500)
            }
        }

        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Profile card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                border = BorderStroke(1.dp, Gray300),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        modifier = Modifier.width(4.dp).fillMaxHeight()
                            .background(Brush.verticalGradient(listOf(Brand600, Cyan500)))
                    )
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(52.dp)
                                    .background(Brand50, RoundedCornerShape(26.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    tenant.fullName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Brand600
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(tenant.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Gray900)
                                Surface(shape = RoundedCornerShape(6.dp), color = if (tenant.isActive) Brand100 else Gray100) {
                                    Text(
                                        if (tenant.isActive) "Active" else "Inactive",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                        color = if (tenant.isActive) Brand600 else Gray500
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Gray100)
                        Spacer(Modifier.height(10.dp))
                        Text("Contact", fontSize = 11.sp, color = Gray500, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        TenantDetailRow("Email", tenant.email)
                        tenant.phone?.let { TenantDetailRow("Phone", it) }
                        tenant.dateOfBirth?.let { TenantDetailRow("Date of Birth", it.take(10)) }

                        if (tenant.idType != null || tenant.idReference != null) {
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = Gray100)
                            Spacer(Modifier.height(10.dp))
                            Text("Identification", fontSize = 11.sp, color = Gray500, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            tenant.idType?.let { TenantDetailRow("ID Type", it) }
                            tenant.idReference?.let { TenantDetailRow("ID Number", it) }
                        }

                        if (tenant.emergencyContactName != null || tenant.emergencyContactPhone != null) {
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = Gray100)
                            Spacer(Modifier.height(10.dp))
                            Text("Emergency Contact", fontSize = 11.sp, color = Gray500, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            tenant.emergencyContactName?.let { TenantDetailRow("Name", it) }
                            tenant.emergencyContactPhone?.let { TenantDetailRow("Phone", it) }
                        }

                        tenant.notes?.let {
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = Gray100)
                            Spacer(Modifier.height(10.dp))
                            Text("Notes", fontSize = 11.sp, color = Gray500, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(it, fontSize = 13.sp, color = Gray700)
                        }
                    }
                }
            }

            // Leases section
            Text("Leases", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gray900)

            if (leases.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No leases found for this tenant.", fontSize = 13.sp, color = Gray500)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    leases.forEach { lease -> TenantLeaseCard(lease) }
                }
            }
        }
    }
}

@Composable
private fun TenantLeaseCard(lease: Lease) {
    val isActive = lease.status == LeaseStatus.ACTIVE || lease.computedStatus == LeaseStatus.ACTIVE
    val accentColor = if (isActive) Brand600 else Gray300
    val statusColor = when {
        isActive -> Brand600
        lease.status == LeaseStatus.EXPIRED -> Danger600
        else -> Gray500
    }
    val statusBg = when {
        isActive -> Brand100
        lease.status == LeaseStatus.EXPIRED -> Danger600.copy(alpha = 0.1f)
        else -> Gray100
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
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        lease.unit?.let { unit ->
                            Text(
                                "Unit ${unit.unitNumber}",
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900
                            )
                            val buildingLabel = unit.building?.address ?: unit.buildingId
                            Text(buildingLabel, fontSize = 12.sp, color = Gray500)
                        } ?: Text("Unit ID: ${lease.unitId}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900)
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = statusBg) {
                        Text(
                            (lease.computedStatus ?: lease.status).name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Gray100)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column {
                        Text("Rent", fontSize = 11.sp, color = Gray500)
                        Text(
                            "A$${lease.rentAmount.fmt2dp()} / ${lease.rentFrequency.name.lowercase()}",
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gray900
                        )
                    }
                    Column {
                        Text("Deposit", fontSize = 11.sp, color = Gray500)
                        Text("A$${lease.bondAmount.fmt2dp()}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                    }
                    Column {
                        Text("Pay Day", fontSize = 11.sp, color = Gray500)
                        Text("Day ${lease.paymentDay}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                    }
                }
                Spacer(Modifier.height(6.dp))
                val dateRange = buildString {
                    append("From: ${lease.startDate.take(10)}")
                    lease.endDate?.let { append("  To: ${it.take(10)}") }
                }
                Text(dateRange, fontSize = 12.sp, color = Gray500)
            }
        }
    }
}

@Composable
private fun TenantDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontSize = 12.sp, color = Gray500, modifier = Modifier.width(120.dp))
        Text(value, fontSize = 13.sp, color = Gray900, fontWeight = FontWeight.Medium)
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
