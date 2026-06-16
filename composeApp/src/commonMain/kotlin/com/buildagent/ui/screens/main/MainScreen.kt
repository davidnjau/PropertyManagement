package com.buildagent.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.buildagent.ui.components.AppDrawer
import com.buildagent.ui.screens.dashboard.DashboardScreen
import com.buildagent.ui.screens.maintenance.MaintenanceHubScreen
import com.buildagent.ui.screens.payments.PaymentLedgerScreen
import com.buildagent.ui.screens.portfolio.BuildingsScreen
import com.buildagent.ui.screens.tenancy.LeasesScreen
import com.buildagent.ui.screens.tenancy.TenantsScreen

class MainScreen : Screen {
    @Composable
    override fun Content() {
        var selectedIndex by remember { mutableIntStateOf(0) }

        Row(modifier = Modifier.fillMaxSize()) {
            AppDrawer(selectedIndex = selectedIndex, onSelect = { selectedIndex = it })

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                when (selectedIndex) {
                    0 -> DashboardScreen()
                    1 -> BuildingsScreen()
                    2 -> TenantsScreen()
                    3 -> LeasesScreen()
                    4 -> PaymentLedgerScreen()
                    5 -> MaintenanceHubScreen()
                    else -> DashboardScreen()
                }
            }
        }
    }
}
