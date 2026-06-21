package com.buildagent.ui.screens.portfolio

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
import androidx.compose.ui.graphics.Brush
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Portfolio", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text(
                    if (buildings.isEmpty()) "No buildings yet" else "${buildings.size} buildings managed",
                    fontSize = 13.sp, color = Gray500
                )
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("+ New Building", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(20.dp))

        when {
            loading -> LoadingContent()
            error != null -> Text("Error: $error", color = Danger600)
            buildings.isEmpty() -> EmptyPortfolioState()
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(buildings) { building -> BuildingCard(building) }
            }
        }
    }

    if (showDialog) {
        CreateBuildingDialog(
            onDismiss = { showDialog = false },
            onSave = { request, onDialogError ->
                vm.createBuilding(
                    request,
                    onSuccess = { showDialog = false },
                    onError = onDialogError
                )
            }
        )
    }
}

@Composable
private fun EmptyPortfolioState() {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏢", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("No buildings yet", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gray700)
            Spacer(Modifier.height(4.dp))
            Text("Add your first building to get started.", fontSize = 13.sp, color = Gray500)
        }
    }
}

@Composable
fun BuildingCard(building: Building) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .background(Brush.verticalGradient(listOf(Brand600, Cyan500)))
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Building icon box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Brand50, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏢", fontSize = 22.sp)
                }
                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = building.name ?: building.address,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Gray900
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${building.address}, ${building.suburb}",
                        fontSize = 13.sp,
                        color = Gray500
                    )
                    Text(
                        text = "${building.state} ${building.postcode}",
                        fontSize = 12.sp,
                        color = Gray500
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip(label = "🏠 ${building.unitCount} units", containerColor = Brand50, textColor = Brand600)
                        building.client?.let {
                            Chip(label = "👤 ${it.fullName}", containerColor = Gray100, textColor = Gray700)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(label: String, containerColor: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = containerColor) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun CreateBuildingDialog(
    onDismiss: () -> Unit,
    onSave: (CreateBuildingRequest, onError: (String) -> Unit) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var suburb by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var postcode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary,
        title = { Text("New Building", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = suburb,
                    onValueChange = { suburb = it },
                    label = { Text("Suburb *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = postcode,
                    onValueChange = { postcode = it },
                    label = { Text("Postcode *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
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
                    ) { err -> errorMsg = err }
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
