package com.buildagent.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.shared.models.Building
import com.buildagent.shared.models.BuildingUnit
import com.buildagent.ui.theme.*
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingUnitPicker(
    selectedBuildingId: String,
    selectedUnitId: String,
    onBuildingSelected: (id: String, name: String) -> Unit,
    onUnitSelected: (id: String, name: String) -> Unit,
    client: BuildAgentClient = koinInject()
) {
    var buildings by remember { mutableStateOf<List<Building>>(emptyList()) }
    var units by remember { mutableStateOf<List<BuildingUnit>>(emptyList()) }
    var buildingExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        buildings = try { client.getBuildings().data ?: emptyList() } catch (e: Exception) { emptyList() }
    }
    LaunchedEffect(selectedBuildingId) {
        if (selectedBuildingId.isNotBlank()) {
            units = try { client.getUnits(selectedBuildingId).data ?: emptyList() } catch (e: Exception) { emptyList() }
        } else {
            units = emptyList()
        }
    }

    val selectedBuilding = buildings.firstOrNull { it.id == selectedBuildingId }
    val selectedUnit = units.firstOrNull { it.id == selectedUnitId }

    ExposedDropdownMenuBox(expanded = buildingExpanded, onExpandedChange = { buildingExpanded = it }) {
        OutlinedTextField(
            value = selectedBuilding?.let { it.name ?: it.address } ?: "Select Building",
            onValueChange = {},
            readOnly = true,
            label = { Text("Building *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
        )
        ExposedDropdownMenu(expanded = buildingExpanded, onDismissRequest = { buildingExpanded = false }) {
            if (buildings.isEmpty()) {
                DropdownMenuItem(text = { Text("No buildings available") }, onClick = {})
            } else {
                buildings.forEach { b ->
                    DropdownMenuItem(
                        text = { Text(b.name ?: b.address) },
                        onClick = { onBuildingSelected(b.id, b.name ?: b.address); buildingExpanded = false }
                    )
                }
            }
        }
    }

    if (selectedBuildingId.isNotBlank()) {
        ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
            OutlinedTextField(
                value = selectedUnit?.let { "Unit ${it.unitNumber}" } ?: "Select Unit",
                onValueChange = {},
                readOnly = true,
                label = { Text("Unit *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
            )
            ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                if (units.isEmpty()) {
                    DropdownMenuItem(text = { Text("No units found for this building") }, onClick = {})
                } else {
                    units.forEach { u ->
                        DropdownMenuItem(
                            text = { Text("Unit ${u.unitNumber}  (${u.status.name})") },
                            onClick = { onUnitSelected(u.id, "Unit ${u.unitNumber}"); unitExpanded = false }
                        )
                    }
                }
            }
        }
    }
}
