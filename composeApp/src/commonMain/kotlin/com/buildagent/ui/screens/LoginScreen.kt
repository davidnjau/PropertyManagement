package com.buildagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import com.buildagent.ui.components.AppLogoMark
import com.buildagent.ui.state.TokenStore
import com.buildagent.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private val bgGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0D0D1E), Color(0xFF1A1040), Color(0xFF0D1B3E))
)
private val brandGradient = Brush.horizontalGradient(
    colors = listOf(Brand600, Cyan500)
)
private val accentBarGradient = Brush.horizontalGradient(
    colors = listOf(Brand600, Cyan500)
)

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authState = LocalAuthState.current
        val client = koinInject<BuildAgentClient>()
        val scope = rememberCoroutineScope()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier.fillMaxSize().background(bgGradient),
            contentAlignment = Alignment.Center
        ) {
            // Decorative glow blobs
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .offset(x = (-120).dp, y = (-160).dp)
                    .background(
                        Brush.radialGradient(listOf(Brand600.copy(alpha = 0.15f), Color.Transparent)),
                        RoundedCornerShape(200.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = 150.dp, y = 120.dp)
                    .background(
                        Brush.radialGradient(listOf(Cyan500.copy(alpha = 0.10f), Color.Transparent)),
                        RoundedCornerShape(150.dp)
                    )
            )

            Card(
                modifier = Modifier.width(420.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(24.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column {
                    // Top accent bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentBarGradient)
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 40.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo mark
                        AppLogoMark(size = 56.dp)
                        Spacer(Modifier.height(16.dp))

                        // Brand name with gradient
                        Text(
                            text = "PropVault",
                            style = TextStyle(
                                brush = brandGradient,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Property Management Platform",
                            fontSize = 13.sp,
                            color = Gray500,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(32.dp))

                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("Email address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Gray300,
                                focusedBorderColor = Brand600,
                                focusedLabelColor = Brand600
                            )
                        )
                        Spacer(Modifier.height(12.dp))

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(
                                        if (passwordVisible) "Hide" else "Show",
                                        fontSize = 12.sp,
                                        color = Brand600
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Gray300,
                                focusedBorderColor = Brand600,
                                focusedLabelColor = Brand600
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { navigator.push(ForgotPasswordScreen()) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Forgot password?", fontSize = 13.sp, color = Brand600)
                            }
                        }

                        errorMessage?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, color = Danger600, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                        }

                        Spacer(Modifier.height(8.dp))

                        // Sign in button with gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(
                                    if (!loading) Brush.horizontalGradient(listOf(Brand600, Brand700))
                                    else Brush.horizontalGradient(listOf(Gray300, Gray300)),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
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
                                modifier = Modifier.fillMaxSize(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                if (loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Sign In",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        letterSpacing = 0.5.sp,
                                        color = White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
