package com.buildagent.ui.screens.maintenance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.koin.getScreenModel
import com.buildagent.shared.models.MaintenanceRequest
import com.buildagent.shared.models.MaintenanceStatus
import com.buildagent.ui.components.*
import com.buildagent.ui.theme.*

private val statusFilters = listOf("All", "REPORTED", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CLOSED")

@Composable
fun MaintenanceHubScreen() {
    val vm = getScreenModel<MaintenanceViewModel>()
    val requests by vm.requests.collectAsState()
    val loading by vm.loading.collectAsState()
    var filter by remember { mutableStateOf("All") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Maintenance Hub", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("${requests.size} requests", fontSize = 13.sp, color = Gray500)
        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(statusFilters) { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f; vm.load(if (f == "All") null else f) },
                    label = { Text(f, fontSize = 12.sp) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        if (loading) { LoadingContent() } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(requests) { request -> MaintenanceCard(request) }
            }
        }
    }
}

@Composable
fun MaintenanceCard(request: MaintenanceRequest) {
    val closedStatuses = listOf(MaintenanceStatus.COMPLETED, MaintenanceStatus.CLOSED, MaintenanceStatus.CANCELLED)
    val slaBreached = request.slaTargetDate != null && request.status !in closedStatuses

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (slaBreached) Danger100 else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(request.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                StatusBadge(request.priority.name, priorityBadge)
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(request.status.name, maintenanceStatusBadge)
                if (slaBreached) {
                    Text("SLA Breached", fontSize = 11.sp, color = Danger600, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(request.description, fontSize = 13.sp, color = Gray700, maxLines = 2)
            request.contractorName?.let {
                Spacer(Modifier.height(4.dp))
                Text("Contractor: $it", fontSize = 12.sp, color = Gray500)
            }
        }
    }
}
