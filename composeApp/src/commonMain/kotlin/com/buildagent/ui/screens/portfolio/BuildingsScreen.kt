package com.buildagent.ui.screens.portfolio

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
import com.buildagent.shared.models.Building
import com.buildagent.shared.models.BuildingType
import com.buildagent.shared.models.BuildingUnit
import com.buildagent.shared.models.CreateBuildingRequest
import com.buildagent.shared.models.CreateUnitRequest
import com.buildagent.shared.models.UnitStatus
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*
import com.buildagent.ui.utils.fmt2dp

@Composable
fun BuildingsScreen() {
    val vm = koinInject<PortfolioViewModel>()
    val buildings by vm.buildings.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }

    if (selectedBuilding != null) {
        BuildingDetailView(
            building = selectedBuilding!!,
            vm = vm,
            onBack = { selectedBuilding = null }
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
                items(buildings) { building ->
                    BuildingCard(building, onClick = { selectedBuilding = building })
                }
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
private fun BuildingDetailView(
    building: Building,
    vm: PortfolioViewModel,
    onBack: () -> Unit
) {
    var units by remember { mutableStateOf<List<BuildingUnit>>(emptyList()) }
    var unitsLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var showAddUnitsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(building.id, refreshTrigger) {
        unitsLoading = true
        units = vm.getUnits(building.id) ?: emptyList()
        unitsLoading = false
    }

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
            Column {
                Text(building.name ?: building.address, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text("${building.suburb}, ${building.state} ${building.postcode}", fontSize = 13.sp, color = Gray500)
            }
        }

        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            // Building info card
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
                        Text("Building Details", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Gray900)
                        Spacer(Modifier.height(12.dp))
                        DetailRow("Address", building.address)
                        DetailRow("Suburb", building.suburb)
                        DetailRow("State", building.state)
                        DetailRow("Postcode", building.postcode)
                        DetailRow("Type", building.buildingType.name)
                        building.yearBuilt?.let { DetailRow("Year Built", it.toString()) }
                        building.client?.let {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Gray100)
                            Spacer(Modifier.height(8.dp))
                            Text("Owner", fontSize = 11.sp, color = Gray500, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(it.fullName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900)
                            Text(it.email, fontSize = 12.sp, color = Gray500)
                        }
                        building.notes?.let {
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Gray100)
                            Spacer(Modifier.height(8.dp))
                            Text("Notes", fontSize = 11.sp, color = Gray500, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(it, fontSize = 13.sp, color = Gray700)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Units section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Units", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gray900)
                    if (!unitsLoading) {
                        val occupied = units.count { it.status == UnitStatus.OCCUPIED }
                        Text("$occupied/${units.size} occupied", fontSize = 13.sp, color = Gray500)
                    }
                }
                Button(
                    onClick = { showAddUnitsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("+ Add Units", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(10.dp))

            if (unitsLoading) {
                LoadingContent()
            } else if (units.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No units registered.", fontSize = 13.sp, color = Gray500)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    units.forEach { unit -> UnitCard(unit) }
                }
            }
        }
    }

    if (showAddUnitsDialog) {
        AddUnitsDialog(
            buildingId = building.id,
            vm = vm,
            onDismiss = { showAddUnitsDialog = false },
            onSuccess = {
                showAddUnitsDialog = false
                refreshTrigger++
            }
        )
    }
}

@Composable
private fun AddUnitsDialog(
    buildingId: String,
    vm: PortfolioViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var prefix by remember { mutableStateOf("A") }
    var startFloor by remember { mutableStateOf("1") }
    var endFloor by remember { mutableStateOf("5") }
    var unitsPerFloor by remember { mutableStateOf("4") }
    var bedrooms by remember { mutableStateOf("") }
    var bathrooms by remember { mutableStateOf("") }
    var rentAmount by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val previewLines = remember(prefix, startFloor, endFloor, unitsPerFloor) {
        val sf = startFloor.toIntOrNull() ?: return@remember emptyList<String>()
        val ef = endFloor.toIntOrNull() ?: return@remember emptyList<String>()
        val upf = unitsPerFloor.toIntOrNull()?.coerceAtLeast(1) ?: return@remember emptyList<String>()
        if (sf > ef) return@remember emptyList<String>()
        (sf..ef).map { floor ->
            val names = (1..upf).joinToString(", ") { i -> "$prefix$floor${i.toString().padStart(2, '0')}" }
            "Floor $floor: $names"
        }
    }

    val totalUnits = remember(previewLines, unitsPerFloor) {
        val upf = unitsPerFloor.toIntOrNull()?.coerceAtLeast(1) ?: 0
        val sf = startFloor.toIntOrNull() ?: 0
        val ef = endFloor.toIntOrNull() ?: 0
        if (sf > ef || upf == 0) 0 else (ef - sf + 1) * upf
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.primary,
        title = { Text("Add Units", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMsg?.let { Text(it, color = Danger600, fontSize = 13.sp) }

                // Naming scheme
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = prefix,
                        onValueChange = { prefix = it.uppercase().take(3) },
                        label = { Text("Prefix") },
                        placeholder = { Text("e.g. A") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                    OutlinedTextField(
                        value = unitsPerFloor,
                        onValueChange = { unitsPerFloor = it.filter { c -> c.isDigit() } },
                        label = { Text("Units/Floor") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = startFloor,
                        onValueChange = { startFloor = it.filter { c -> c.isDigit() } },
                        label = { Text("Start Floor") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                    OutlinedTextField(
                        value = endFloor,
                        onValueChange = { endFloor = it.filter { c -> c.isDigit() } },
                        label = { Text("End Floor") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                }

                HorizontalDivider(color = Gray100)

                // Optional unit specs
                Text("Unit Specs (optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Gray500)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = bedrooms,
                        onValueChange = { bedrooms = it.filter { c -> c.isDigit() } },
                        label = { Text("Bedrooms") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                    OutlinedTextField(
                        value = bathrooms,
                        onValueChange = { bathrooms = it.filter { c -> c.isDigit() } },
                        label = { Text("Bathrooms") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                }

                OutlinedTextField(
                    value = rentAmount,
                    onValueChange = { rentAmount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Rent Amount (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                )

                // Preview
                if (previewLines.isNotEmpty()) {
                    HorizontalDivider(color = Gray100)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Brand50
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "$totalUnits units will be created",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Brand600
                            )
                            Spacer(Modifier.height(4.dp))
                            previewLines.forEach { line ->
                                Text(line, fontSize = 12.sp, color = Brand600)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sf = startFloor.toIntOrNull()
                    val ef = endFloor.toIntOrNull()
                    val upf = unitsPerFloor.toIntOrNull()?.coerceAtLeast(1)
                    if (prefix.isBlank() || sf == null || ef == null || upf == null) {
                        errorMsg = "Please fill in all required fields."
                        return@Button
                    }
                    if (sf > ef) {
                        errorMsg = "Start floor must be <= end floor."
                        return@Button
                    }
                    val requests = mutableListOf<CreateUnitRequest>()
                    for (floor in sf..ef) {
                        for (unitIdx in 1..upf) {
                            val unitNumber = "$prefix$floor${unitIdx.toString().padStart(2, '0')}"
                            requests.add(
                                CreateUnitRequest(
                                    unitNumber = unitNumber,
                                    floor = floor,
                                    bedrooms = bedrooms.toIntOrNull() ?: 0,
                                    bathrooms = bathrooms.toIntOrNull() ?: 0,
                                    rentAmount = rentAmount.toDoubleOrNull()
                                )
                            )
                        }
                    }
                    saving = true
                    errorMsg = null
                    vm.createUnitsBulk(
                        buildingId = buildingId,
                        requests = requests,
                        onSuccess = { saving = false; onSuccess() },
                        onError = { saving = false; errorMsg = it }
                    )
                },
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = Brand600)
            ) {
                Text(if (saving) "Creating..." else "Create Units")
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!saving) onDismiss() }) { Text("Cancel") }
        }
    )
}

@Composable
private fun UnitCard(unit: BuildingUnit) {
    val isOccupied = unit.status == UnitStatus.OCCUPIED
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.width(4.dp).fillMaxHeight()
                    .background(if (isOccupied) Brand600 else Gray300)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp)
                        .background(if (isOccupied) Brand50 else Gray100, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isOccupied) "🏠" else "🔑", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Unit ${unit.unitNumber}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900)
                    val specs = buildList {
                        if (unit.bedrooms > 0) add("${unit.bedrooms}bd")
                        if (unit.bathrooms > 0) add("${unit.bathrooms}ba")
                        unit.areaSqm?.let { add("${it.toInt()}m²") }
                    }.joinToString(" · ")
                    if (specs.isNotEmpty()) Text(specs, fontSize = 12.sp, color = Gray500)
                    unit.rentAmount?.let {
                        Text("A$${it.fmt2dp()} / ${unit.rentFrequency.name.lowercase()}", fontSize = 12.sp, color = Gray500)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isOccupied) Brand100 else Gray100
                ) {
                    Text(
                        text = unit.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isOccupied) Brand600 else Gray700
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontSize = 12.sp, color = Gray500, modifier = Modifier.width(100.dp))
        Text(value, fontSize = 13.sp, color = Gray900, fontWeight = FontWeight.Medium)
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
fun BuildingCard(building: Building, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
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

                Text("›", fontSize = 20.sp, color = Gray300)
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
