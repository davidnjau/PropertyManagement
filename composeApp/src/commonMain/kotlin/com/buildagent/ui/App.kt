package com.buildagent.ui

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.buildagent.ui.screens.LoginScreen
import com.buildagent.ui.state.AuthState
import com.buildagent.ui.state.LocalAuthState
import com.buildagent.ui.theme.BuildAgentTheme

@Composable
fun App() {
    BuildAgentTheme {
        val authState = remember { mutableStateOf<AuthState>(AuthState.Unauthenticated) }
        CompositionLocalProvider(LocalAuthState provides authState) {
            Navigator(LoginScreen())
        }
    }
}
