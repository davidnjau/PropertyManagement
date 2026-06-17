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
import com.buildagent.shared.models.CreateTenantRequest
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
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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

    if (showDialog) {
        CreateTenantDialog(
            onDismiss = { showDialog = false },
            onSave = { request ->
                vm.createTenant(
                    request,
                    onSuccess = { showDialog = false },
                    onError = { }
                )
            }
        )
    }
}

@Composable
fun CreateTenantDialog(
    onDismiss: () -> Unit,
    onSave: (CreateTenantRequest) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Tenant", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank() || email.isBlank()) {
                        errorMsg = "Full name and email are required."
                        return@Button
                    }
                    onSave(
                        CreateTenantRequest(
                            fullName = fullName.trim(),
                            email = email.trim(),
                            phone = phone.trim().ifBlank { null }
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
