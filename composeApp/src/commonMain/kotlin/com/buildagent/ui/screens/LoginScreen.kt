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
import com.buildagent.ui.screens.main.MainScreen
import com.buildagent.ui.state.AuthState
import com.buildagent.ui.state.LocalAuthState
import com.buildagent.ui.theme.Brand600

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authState = LocalAuthState.current
        var token by remember { mutableStateOf("") }
        var agencyId by remember { mutableStateOf("") }

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
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("JWT Token") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = agencyId,
                        onValueChange = { agencyId = it },
                        label = { Text("Agency ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (token.isNotBlank() && agencyId.isNotBlank()) {
                                authState.value = AuthState.Authenticated(
                                    token = token,
                                    agencyId = agencyId,
                                    role = "AGENT",
                                    userName = "Agent"
                                )
                                navigator.replace(MainScreen())
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sign In", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
