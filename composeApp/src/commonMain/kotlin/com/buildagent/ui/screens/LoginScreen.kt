package com.buildagent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.ui.screens.main.MainScreen
import com.buildagent.ui.screens.tenant.TenantMainScreen
import com.buildagent.ui.state.AuthState
import com.buildagent.ui.state.LocalAuthState
import com.buildagent.ui.state.TokenStore
import com.buildagent.ui.theme.Brand600
import com.buildagent.ui.theme.Danger600
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authState = LocalAuthState.current
        val client = koinInject<BuildAgentClient>()
        val scope = rememberCoroutineScope()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.width(400.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BuildAgent", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Property Management Platform", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    errorMessage?.let {
                        Text(it, color = Danger600, fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Email and password are required."
                                return@Button
                            }
                            loading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val response = client.signIn(email.trim(), password)
                                    val auth = response.data ?: error(response.message ?: "Sign in failed")
                                    val user = auth.user
                                    TokenStore.token = auth.token
                                    authState.value = AuthState.Authenticated(
                                        token = auth.token,
                                        agencyId = user.agencyId,
                                        roles = user.roles,
                                        userName = user.fullName
                                    )
                                    if ("TENANT" in user.roles && "ADMIN" !in user.roles && "AGENT" !in user.roles) {
                                        navigator.replace(TenantMainScreen())
                                    } else {
                                        navigator.replace(MainScreen())
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Sign in failed. Please try again."
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
