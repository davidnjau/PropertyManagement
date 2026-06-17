package com.buildagent.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.buildagent.ui.components.AppDrawer
import com.buildagent.ui.screens.admin.AdminAlertsScreen
import com.buildagent.ui.screens.admin.AdminDocumentsScreen
import com.buildagent.ui.screens.admin.AdminLeaseExtensionsScreen
import com.buildagent.ui.screens.admin.PaymentMethodsScreen
import com.buildagent.ui.screens.dashboard.DashboardScreen
import com.buildagent.ui.screens.maintenance.MaintenanceHubScreen
import com.buildagent.ui.screens.payments.PaymentLedgerScreen
import com.buildagent.ui.screens.portfolio.BuildingsScreen
import com.buildagent.ui.screens.tenancy.LeasesScreen
import com.buildagent.ui.screens.tenancy.TenantsScreen
import com.buildagent.ui.state.AuthState
import com.buildagent.ui.state.LocalAuthState

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val authState = LocalAuthState.current
        val role = (authState.value as? AuthState.Authenticated)?.role ?: "AGENT"
        var selectedIndex by remember { mutableIntStateOf(0) }

        Row(modifier = Modifier.fillMaxSize()) {
            AppDrawer(
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it },
                role = role
            )

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                when (selectedIndex) {
                    0 -> DashboardScreen()
                    1 -> BuildingsScreen()
                    2 -> TenantsScreen()
                    3 -> LeasesScreen()
                    4 -> PaymentLedgerScreen()
                    5 -> MaintenanceHubScreen()
                    6 -> AdminAlertsScreen()
                    7 -> AdminDocumentsScreen()
                    8 -> PaymentMethodsScreen()
                    9 -> AdminLeaseExtensionsScreen()
                    else -> DashboardScreen()
                }
            }
        }
    }
}
