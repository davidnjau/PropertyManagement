package com.buildagent.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Send Alert", fontSize = 18.sp, fontWeight = FontWeight.Bold)

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
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = targetTypeExpanded,
                        onDismissRequest = { targetTypeExpanded = false }
                    ) {
                        targetTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = { targetType = t; targetTypeExpanded = false }
                            )
                        }
                    }
                }

                Text("Channels", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                channelOptions.forEach { ch ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = ch in selectedChannels,
                            onCheckedChange = { checked ->
                                selectedChannels = if (checked) selectedChannels + ch else selectedChannels - ch
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(ch, fontSize = 14.sp)
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                Button(
                    onClick = {
                        formError = null
                        if (subject.isBlank() || message.isBlank()) {
                            formError = "Subject and message are required."
                            return@Button
                        }
                        if (selectedChannels.isEmpty()) {
                            formError = "Select at least one channel."
                            return@Button
                        }
                        vm.createAlert(
                            CreateAlertRequest(
                                targetType = targetType,
                                channels = selectedChannels.toList(),
                                subject = subject.trim(),
                                message = message.trim()
                            ),
                            onSuccess = {
                                subject = ""
                                message = ""
                            }
                        )
                    },
                    enabled = !sending,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (sending) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Alert", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Right: History
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text("Sent Alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            if (loading) {
                LoadingContent()
            } else if (alerts.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No alerts sent yet.", color = Gray500)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(alert.subject, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                StatusBadge(alert.status, alertStatusBadge)
            }
            Spacer(Modifier.height(6.dp))
            Text("To: ${alert.targetLabel}", fontSize = 13.sp, color = Gray700)
            Text("Recipients: ${alert.recipientCount}  |  Channels: ${alert.channels.joinToString(", ")}", fontSize = 12.sp, color = Gray500)
            Text("Sent: ${alert.sentAt}", fontSize = 12.sp, color = Gray500)
        }
    }
}
