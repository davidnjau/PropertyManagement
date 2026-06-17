package com.buildagent.ui.screens.portfolio

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
import com.buildagent.shared.models.Building
import com.buildagent.shared.models.BuildingType
import com.buildagent.shared.models.CreateBuildingRequest
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*

@Composable
fun BuildingsScreen() {
    val vm = koinInject<PortfolioViewModel>()
    val buildings by vm.buildings.collectAsState()
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
                Text("Portfolio", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${buildings.size} buildings", fontSize = 13.sp, color = Gray500)
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ New Building")
            }
        }
        Spacer(Modifier.height(20.dp))

        when {
            loading -> LoadingContent()
            error != null -> Text("Error: $error", color = Danger600)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(buildings) { building -> BuildingCard(building) }
            }
        }
    }

    if (showDialog) {
        CreateBuildingDialog(
            onDismiss = { showDialog = false },
            onSave = { request ->
                vm.createBuilding(
                    request,
                    onSuccess = { showDialog = false },
                    onError = { }
                )
            }
        )
    }
}

@Composable
fun CreateBuildingDialog(
    onDismiss: () -> Unit,
    onSave: (CreateBuildingRequest) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var suburb by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Building", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = suburb,
                    onValueChange = { suburb = it },
                    label = { Text("Suburb *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = postcode,
                    onValueChange = { postcode = it },
                    label = { Text("Postcode *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (address.isBlank() || suburb.isBlank() || state.isBlank() || postcode.isBlank()) {
                        errorMsg = "Address, suburb, state, and postcode are required."
                        return@Button
                    }
                    onSave(
                        CreateBuildingRequest(
                            address = address.trim(),
                            suburb = suburb.trim(),
                            state = state.trim(),
                            postcode = postcode.trim(),
                            name = name.trim().ifBlank { null },
                            buildingType = BuildingType.RESIDENTIAL
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
fun BuildingCard(building: Building) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = building.name ?: building.address,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${building.suburb}, ${building.state} ${building.postcode}",
                fontSize = 13.sp,
                color = Gray500
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${building.unitCount} units", fontSize = 12.sp, color = Gray700)
                building.client?.let {
                    Text("Owner: ${it.fullName}", fontSize = 12.sp, color = Gray700)
                }
            }
        }
    }
}
