package com.buildagent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.buildagent.shared.api.BuildAgentClient
import com.buildagent.ui.theme.Brand600
import com.buildagent.ui.theme.Danger600
import com.buildagent.ui.theme.Gray300
import com.buildagent.ui.theme.Gray500
import com.buildagent.ui.theme.Success600
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

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

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.width(400.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Forgot Password", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (otpSent) "Enter the OTP sent to your email." else "Enter your email address and we'll send you an OTP.",
                        fontSize = 13.sp,
                        color = Gray500,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !otpSent,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Gray300,
                            focusedBorderColor = Brand600
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
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Gray300,
                                focusedBorderColor = Brand600
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

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            loading = true
                            scope.launch {
                                try {
                                    if (!otpSent) {
                                        if (email.isBlank()) {
                                            errorMessage = "Email is required."
                                            return@launch
                                        }
                                        client.forgotPassword(email.trim())
                                        otpSent = true
                                        successMessage = "OTP sent. Check your email (or server logs in dev)."
                                    } else {
                                        if (otp.isBlank()) {
                                            errorMessage = "Enter the OTP."
                                            return@launch
                                        }
                                        client.verifyOtp(email.trim(), otp.trim())
                                        successMessage = "OTP verified successfully."
                                        // Navigate back after short delay
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
                            Text(if (otpSent) "Verify OTP" else "Send OTP", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (otpSent) {
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = {
                            otpSent = false
                            otp = ""
                            errorMessage = null
                            successMessage = null
                        }) {
                            Text("Resend OTP", color = Brand600, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
