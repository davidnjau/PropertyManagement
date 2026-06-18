package com.buildagent.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildagent.shared.models.CreateUserRequest
import com.buildagent.shared.models.UserType
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*
import org.koin.compose.koinInject

@Composable
fun AdminUsersScreen() {
    val vm = koinInject<AdminUsersViewModel>()
    val users by vm.users.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Team Members", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${users.size} users", fontSize = 13.sp, color = Gray500)
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ Add Agent")
            }
        }
        Spacer(Modifier.height(20.dp))

        error?.let {
            Text("Error: $it", color = Danger600, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }

        if (loading) {
            LoadingContent()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users) { user ->
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
                                Text(user.fullName, fontWeight = FontWeight.Medium)
                                Text(user.email, fontSize = 13.sp, color = Gray500)
                                user.phone?.let { Text(it, fontSize = 12.sp, color = Gray500) }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (user.role == "ADMIN") Brand100 else Gray100
                            ) {
                                Text(
                                    user.role,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (user.role == "ADMIN") Brand600 else Gray700
                                )
                            }
                        }
                    }
                }
                if (users.isEmpty() && !loading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No team members yet.", color = Gray500)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddAgentDialog(
            onDismiss = { showDialog = false },
            onSave = { request ->
                vm.createUser(
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
fun AddAgentDialog(onDismiss: () -> Unit, onSave: (CreateUserRequest) -> Unit) {
    val userTypes = listOf(UserType.AGENT, UserType.AGENCY)
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(UserType.AGENT) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Team Member", fontWeight = FontWeight.Bold) },
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
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false }
                    ) {
                        userTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = { selectedType = type; typeMenuExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMsg = "Full name, email and password are required."
                        return@Button
                    }
                    onSave(
                        CreateUserRequest(
                            userType = selectedType,
                            fullName = fullName.trim(),
                            email = email.trim(),
                            password = password,
                            phone = phone.trim().ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
