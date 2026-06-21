package com.buildagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private val bgGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0D0D1E), Color(0xFF1A1040), Color(0xFF0D1B3E))
)
private val brandGradient = Brush.horizontalGradient(
    colors = listOf(Brand600, Cyan500)
)

class ForgotPasswordScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val client = koinInject<BuildAgentClient>()
        val scope = rememberCoroutineScope()

        var email by remember { mutableStateOf("") }
        var otp by remember { mutableStateOf("") }
        var otpSent by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier.fillMaxSize().background(bgGradient),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .offset(x = 120.dp, y = (-100).dp)
                    .background(
                        Brush.radialGradient(listOf(Brand600.copy(alpha = 0.12f), Color.Transparent)),
                        RoundedCornerShape(175.dp)
                    )
            )

            Card(
                modifier = Modifier.width(420.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(24.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Brush.horizontalGradient(listOf(Brand600, Cyan500)))
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 40.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Brand600
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (otpSent) "Verify OTP" else "Forgot Password",
                                style = TextStyle(
                                    brush = brandGradient,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (otpSent)
                                "Enter the 6-digit code sent to your email."
                            else
                                "Enter your email address and we'll send you an OTP to reset your password.",
                            fontSize = 13.sp,
                            color = Gray500,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(28.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("Email address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !otpSent,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Gray300,
                                focusedBorderColor = Brand600,
                                focusedLabelColor = Brand600
                            )
                        )

                        if (otpSent) {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = otp,
                                onValueChange = { otp = it; errorMessage = null },
                                label = { Text("OTP Code") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Gray300,
                                    focusedBorderColor = Brand600,
                                    focusedLabelColor = Brand600
                                )
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        errorMessage?.let {
                            Text(it, color = Danger600, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                        }
                        successMessage?.let {
                            Text(it, color = Success600, fontSize = 13.sp, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(4.dp))
                        }

                        Spacer(Modifier.height(12.dp))

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
                                    errorMessage = null
                                    successMessage = null
                                    loading = true
                                    scope.launch {
                                        try {
                                            if (!otpSent) {
                                                if (email.isBlank()) { errorMessage = "Email is required."; return@launch }
                                                client.forgotPassword(email.trim())
                                                otpSent = true
                                                successMessage = "OTP sent — check your email (or server logs in dev)."
                                            } else {
                                                if (otp.isBlank()) { errorMessage = "Enter the OTP."; return@launch }
                                                client.verifyOtp(email.trim(), otp.trim())
                                                successMessage = "OTP verified successfully."
                                                kotlinx.coroutines.delay(1500)
                                                navigator.pop()
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message ?: "Something went wrong. Please try again."
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
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = White, strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        if (otpSent) "Verify OTP" else "Send OTP",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        letterSpacing = 0.5.sp,
                                        color = White
                                    )
                                }
                            }
                        }

                        if (otpSent) {
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { otpSent = false; otp = ""; errorMessage = null; successMessage = null }) {
                                Text("Resend OTP", color = Brand600, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
