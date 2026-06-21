package com.buildagent.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.buildagent.shared.models.Alert
import com.buildagent.shared.models.CreateAlertRequest
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.components.StatusBadge
import com.buildagent.ui.theme.*

private val alertStatusBadge = mapOf(
    "SENT"    to com.buildagent.ui.components.BadgeConfig("Sent",    Success100, Success600),
    "FAILED"  to com.buildagent.ui.components.BadgeConfig("Failed",  Danger100,  Danger600),
    "PARTIAL" to com.buildagent.ui.components.BadgeConfig("Partial", Warning100, Warning600),
    "PENDING" to com.buildagent.ui.components.BadgeConfig("Pending", Gray100,    Gray700),
)

private val targetTypes = listOf("ALL", "BUILDING", "SPECIFIC")
private val channelOptions = listOf("IN_APP", "EMAIL", "SMS")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAlertsScreen() {
    val vm = koinInject<AdminAlertsViewModel>()
    val alerts by vm.alerts.collectAsState()
    val loading by vm.loading.collectAsState()
    val sending by vm.sending.collectAsState()
    val error by vm.error.collectAsState()

    var targetType by remember { mutableStateOf("ALL") }
    var targetTypeExpanded by remember { mutableStateOf(false) }
    var selectedChannels by remember { mutableStateOf(setOf("IN_APP")) }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        // Left: Compose form
        Card(
            modifier = Modifier.width(360.dp).fillMaxHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            border = BorderStroke(1.dp, Gray300),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier.width(4.dp).fillMaxHeight()
                        .background(Brush.verticalGradient(listOf(Brand600, Cyan500)))
                )
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column {
                        Text("Send Alert", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
                        Text("Notify tenants or all users", fontSize = 12.sp, color = Gray500)
                    }

                    error?.let { Text(it, color = Danger600, fontSize = 13.sp) }
                    formError?.let { Text(it, color = Danger600, fontSize = 13.sp) }

                    ExposedDropdownMenuBox(
                        expanded = targetTypeExpanded,
                        onExpandedChange = { targetTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = targetType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetTypeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                        )
                        ExposedDropdownMenu(expanded = targetTypeExpanded, onDismissRequest = { targetTypeExpanded = false }) {
                            targetTypes.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { targetType = t; targetTypeExpanded = false })
                            }
                        }
                    }

                    Column {
                        Text("Channels", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Gray700)
                        Spacer(Modifier.height(4.dp))
                        channelOptions.forEach { ch ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = ch in selectedChannels,
                                    onCheckedChange = { checked ->
                                        selectedChannels = if (checked) selectedChannels + ch else selectedChannels - ch
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Brand600)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(ch, fontSize = 14.sp, color = Gray700)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600)
                    )

                    Button(
                        onClick = {
                            formError = null
                            if (subject.isBlank() || message.isBlank()) { formError = "Subject and message are required."; return@Button }
                            if (selectedChannels.isEmpty()) { formError = "Select at least one channel."; return@Button }
                            vm.createAlert(
                                CreateAlertRequest(targetType = targetType, channels = selectedChannels.toList(), subject = subject.trim(), message = message.trim()),
                                onSuccess = { subject = ""; message = "" }
                            )
                        },
                        enabled = !sending,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (sending) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = White)
                        else Text("Send Alert", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Right: History
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text("Sent Alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(Modifier.height(12.dp))

            if (loading) {
                LoadingContent()
            } else if (alerts.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔔", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No alerts sent yet.", color = Gray500, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(alerts) { alert -> AlertHistoryCard(alert) }
                }
            }
        }
    }
}

@Composable
fun AlertHistoryCard(alert: Alert) {
    val accentColor = when (alert.status) {
        "SENT" -> Success600
        "FAILED" -> Danger600
        "PARTIAL" -> Warning600
        else -> Gray300
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accentColor))
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(alert.subject, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Gray900, modifier = Modifier.weight(1f))
                    StatusBadge(alert.status, alertStatusBadge)
                }
                Spacer(Modifier.height(6.dp))
                Text("To: ${alert.targetLabel}", fontSize = 13.sp, color = Gray700)
                Text(
                    "${alert.recipientCount} recipients · ${alert.channels.joinToString(", ")}",
                    fontSize = 12.sp, color = Gray500
                )
                Text("Sent: ${alert.sentAt}", fontSize = 12.sp, color = Gray500)
            }
        }
    }
}
