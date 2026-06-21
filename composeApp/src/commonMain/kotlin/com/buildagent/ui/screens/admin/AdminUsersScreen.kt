package com.buildagent.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildagent.shared.models.AdminUserResponse
import com.buildagent.shared.models.CreateUserRequest
import com.buildagent.shared.models.UserType
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*
import org.koin.compose.koinInject

private fun generatePassword(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"
    var result = ""
    repeat(10) { result += chars[(chars.length * kotlin.random.Random.nextFloat()).toInt()] }
    return result
}

@Composable
fun AdminUsersScreen() {
    val vm = koinInject<AdminUsersViewModel>()
    val users by vm.users.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<AdminUserResponse?>(null) }

    if (selectedUser != null) {
        UserDetailView(
            user = selectedUser!!,
            onBack = { selectedUser = null }
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
                Text("Team Members", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text(
                    if (users.isEmpty()) "No members yet" else "${users.size} members",
                    fontSize = 13.sp, color = Gray500
                )
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("+ Add Agent", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(20.dp))

        error?.let {
            Text("Error: $it", color = Danger600, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }

        if (loading) {
            LoadingContent()
        } else if (users.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👥", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No team members yet", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gray700)
                    Spacer(Modifier.height(4.dp))
                    Text("Add your first agent to get started.", fontSize = 13.sp, color = Gray500)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(users) { user -> TeamMemberCard(user, onClick = { selectedUser = user }) }
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

@Composable
private fun UserDetailView(user: AdminUserResponse, onBack: () -> Unit) {
    val initials = user.fullName
        .split(" ")
        .take(2)
        .joinToString("") { it.first().uppercase() }
    val isAdmin = "ADMIN" in user.roles || "AGENCY" in user.roles
    val isAgent = "AGENT" in user.roles

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Header with back
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Gray100,
                modifier = Modifier.clickable { onBack() }
            ) {
                Text("< Back", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp, color = Brand600, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))
            Text("Member Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900)
        }

        Spacer(Modifier.height(24.dp))

        // Avatar + name hero
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
                        .background(
                            Brush.verticalGradient(
                                if (isAdmin) listOf(Brand600, Cyan500) else listOf(Gray300, Gray300)
                            )
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.linearGradient(
                                    if (isAdmin) listOf(Brand600, Brand700)
                                    else if (isAgent) listOf(Cyan500, Brand600)
                                    else listOf(Gray500, Gray700)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Gray900)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            user.roles.forEach { role ->
                                val adminRole = role == "ADMIN" || role == "AGENCY"
                                val agentRole = role == "AGENT"
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        adminRole -> Brand100
                                        agentRole -> Cyan100
                                        else -> Gray100
                                    }
                                ) {
                                    Text(
                                        text = role,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when {
                                            adminRole -> Brand600
                                            agentRole -> Cyan500
                                            else -> Gray700
                                        }
                                    )
                                }
                            }
                        }
                    }
                    // Status dot
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (user.isActive) Success100 else Gray100
                    ) {
                        Text(
                            text = if (user.isActive) "● Active" else "● Inactive",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (user.isActive) Success600 else Gray500
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Contact details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, Gray300),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Contact", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900)
                Spacer(Modifier.height(12.dp))
                UserDetailRow(label = "Email", value = user.email)
                user.phone?.let { UserDetailRow(label = "Phone", value = it) }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = Gray100)
                Spacer(Modifier.height(8.dp))
                UserDetailRow(label = "User Type", value = user.userType)
                UserDetailRow(label = "Member Since", value = user.createdAt.take(10))
            }
        }
    }
}

@Composable
private fun UserDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = Gray500, modifier = Modifier.width(110.dp))
        Text(value, fontSize = 13.sp, color = Gray900, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TeamMemberCard(user: AdminUserResponse, onClick: () -> Unit = {}) {
    val initials = user.fullName
        .split(" ")
        .take(2)
        .joinToString("") { it.first().uppercase() }
    val isAdmin = "ADMIN" in user.roles || "AGENCY" in user.roles
    val isAgent = "AGENT" in user.roles

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left accent stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            if (isAdmin) listOf(Brand600, Cyan500)
                            else listOf(Gray300, Gray300)
                        )
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with initials
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            Brush.linearGradient(
                                if (isAdmin) listOf(Brand600, Brand700)
                                else if (isAgent) listOf(Cyan500, Brand600)
                                else listOf(Gray500, Gray700)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(user.fullName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Gray900)
                    Spacer(Modifier.height(2.dp))
                    Text(user.email, fontSize = 13.sp, color = Gray500)
                    user.phone?.let {
                        Text(it, fontSize = 12.sp, color = Gray500)
                    }
                }

                // Role badges + chevron
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        user.roles.forEach { role ->
                            val adminRole = role == "ADMIN" || role == "AGENCY"
                            val agentRole = role == "AGENT"
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    adminRole -> Brand100
                                    agentRole -> Cyan100
                                    else -> Gray100
                                }
                            ) {
                                Text(
                                    text = role,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        adminRole -> Brand600
                                        agentRole -> Cyan500
                                        else -> Gray700
                                    }
                                )
                            }
                        }
                    }
                    Text("›", fontSize = 20.sp, color = Gray300)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAgentDialog(onDismiss: () -> Unit, onSave: (CreateUserRequest) -> Unit) {
    val userTypes = listOf(UserType.AGENT, UserType.AGENCY)
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    val generatedPassword = remember { generatePassword() }
    var selectedType by remember { mutableStateOf(UserType.AGENT) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary,
        title = { Text("Add Team Member", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }
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
                    label = { Text("Phone (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = idNumber,
                    onValueChange = { idNumber = it },
                    label = { Text("Identification Number (optional)") },
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
                        label = { Text("Role *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Brand50
                ) {
                    Text(
                        "Auto-generated password: $generatedPassword",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = Brand600
                    )
                }
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
                        CreateUserRequest(
                            userType = selectedType,
                            fullName = fullName.trim(),
                            email = email.trim(),
                            password = generatedPassword,
                            phone = phone.trim().ifBlank { null }
                            // idNumber = idNumber.trim().ifBlank { null }
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
