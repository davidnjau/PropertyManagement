package com.buildagent.ui.state

import androidx.compose.runtime.*

sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val token: String, val agencyId: String, val roles: List<String>, val userName: String) : AuthState()
}

val LocalAuthState = compositionLocalOf<MutableState<AuthState>> {
    error("AuthState not provided")
}
