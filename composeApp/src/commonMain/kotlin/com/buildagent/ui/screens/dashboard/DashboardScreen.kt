package com.buildagent.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.buildagent.ui.components.*
import com.buildagent.ui.theme.Gray100
import com.buildagent.ui.theme.Gray500
import com.buildagent.ui.theme.Success600

@Composable
fun DashboardScreen() {
    val vm = koinInject<DashboardViewModel>()
    val state by vm.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Portfolio overview", fontSize = 13.sp, color = Gray500)
        Spacer(Modifier.height(20.dp))

        when (val s = state) {
            is DashboardUiState.Loading -> LoadingContent(Modifier.height(200.dp))
            is DashboardUiState.Error -> ErrorContent(s.message)
            is DashboardUiState.Success -> {
                val d = s.data
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Buildings",        d.buildings.toString(),         Modifier.weight(1f))
                    StatCard("Occupancy",        "${d.occupancyRate}%",          Modifier.weight(1f))
                    StatCard("Vacant Units",     d.units.vacant.toString(),      Modifier.weight(1f), alert = d.units.vacant > 0)
                    StatCard("Overdue Payments", d.overduePayments.toString(),   Modifier.weight(1f), alert = d.overduePayments > 0)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Expiring Leases (60d)", d.expiringLeases.toString(),  Modifier.weight(1f), alert = d.expiringLeases > 0)
                    StatCard("Open Maintenance",      d.openMaintenance.toString(), Modifier.weight(1f))
                    StatCard("SLA Breached",          d.slaBreached.toString(),     Modifier.weight(1f), alert = d.slaBreached > 0)
                }
                Spacer(Modifier.height(20.dp))
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Unit Occupancy", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { d.occupancyRate / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Success600,
                            trackColor = Gray100
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("${d.units.occupied} / ${d.units.total} occupied", fontSize = 13.sp, color = Gray500)
                    }
                }
            }
        }
    }
}
