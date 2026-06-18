package com.buildagent.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.buildagent.ui.components.AppDrawer
import com.buildagent.ui.screens.admin.AdminAlertsScreen
import com.buildagent.ui.screens.admin.AdminDocumentsScreen
import com.buildagent.ui.screens.admin.AdminLeaseExtensionsScreen
import com.buildagent.ui.screens.admin.AdminUsersScreen
import com.buildagent.ui.screens.admin.PaymentMethodsScreen
import com.buildagent.ui.screens.dashboard.DashboardScreen
import com.buildagent.ui.screens.maintenance.MaintenanceHubScreen
import com.buildagent.ui.screens.payments.PaymentLedgerScreen
import com.buildagent.ui.screens.portfolio.BuildingsScreen
import com.buildagent.ui.screens.tenancy.LeasesScreen
import com.buildagent.ui.screens.tenancy.TenantsScreen
import com.buildagent.ui.state.AuthState
import com.buildagent.ui.state.LocalAuthState
import com.buildagent.ui.theme.Sidebar

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val authState = LocalAuthState.current
        val roles = (authState.value as? AuthState.Authenticated)?.roles ?: listOf("AGENT")
        var selectedIndex by remember { mutableIntStateOf(0) }
        var sidebarVisible by remember { mutableStateOf(true) }

        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = sidebarVisible,
                enter = expandHorizontally(expandFrom = Alignment.Start),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                AppDrawer(
                    selectedIndex = selectedIndex,
                    onSelect = { selectedIndex = it },
                    roles = roles
                )
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Sidebar)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { sidebarVisible = !sidebarVisible }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☰", fontSize = 20.sp, color = Color.White)
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                        10 -> AdminUsersScreen()
                        else -> DashboardScreen()
                    }
                }
            }
        }
    }
}
