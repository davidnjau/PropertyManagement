package com.buildagent.ui.screens.tenant

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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.buildagent.ui.components.TenantDrawer
import com.buildagent.ui.screens.LoginScreen
import com.buildagent.ui.state.AuthState
import com.buildagent.ui.state.LocalAuthState
import com.buildagent.ui.theme.Sidebar

class TenantMainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authState = LocalAuthState.current
        val userName = (authState.value as? AuthState.Authenticated)?.userName ?: "Tenant"
        var selectedIndex by remember { mutableIntStateOf(0) }
        var sidebarVisible by remember { mutableStateOf(true) }

        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = sidebarVisible,
                enter = expandHorizontally(expandFrom = Alignment.Start),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                TenantDrawer(
                    selectedIndex = selectedIndex,
                    onSelect = { selectedIndex = it },
                    userName = userName,
                    onSignOut = {
                        authState.value = AuthState.Unauthenticated
                        navigator.replace(LoginScreen())
                    }
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
                        0 -> TenantOverviewScreen()
                        1 -> TenantPayRentScreen()
                        2 -> TenantLeaseScreen()
                        3 -> TenantMaintenanceScreen()
                        4 -> TenantDocumentsScreen()
                        else -> TenantOverviewScreen()
                    }
                }
            }
        }
    }
}
