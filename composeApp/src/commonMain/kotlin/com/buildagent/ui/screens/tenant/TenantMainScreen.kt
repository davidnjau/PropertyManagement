package com.buildagent.ui.screens.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.buildagent.ui.components.TenantDrawer
import com.buildagent.ui.screens.LoginScreen
import com.buildagent.ui.state.AuthState
import com.buildagent.ui.state.LocalAuthState

class TenantMainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authState = LocalAuthState.current
        val userName = (authState.value as? AuthState.Authenticated)?.userName ?: "Tenant"
        var selectedIndex by remember { mutableIntStateOf(0) }

        Row(modifier = Modifier.fillMaxSize()) {
            TenantDrawer(
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it },
                userName = userName,
                onSignOut = {
                    authState.value = AuthState.Unauthenticated
                    navigator.replace(LoginScreen())
                }
            )

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
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
