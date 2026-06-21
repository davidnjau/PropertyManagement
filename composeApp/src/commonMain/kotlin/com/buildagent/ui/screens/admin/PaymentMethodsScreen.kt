package com.buildagent.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.buildagent.shared.models.BankConfig
import com.buildagent.shared.models.UpdateMpesaConfigRequest
import com.buildagent.shared.models.UpdatePaypalConfigRequest
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*

@Composable
fun PaymentMethodsScreen() {
    val vm = koinInject<PaymentMethodsViewModel>()
    val config by vm.config.collectAsState()
    val loading by vm.loading.collectAsState()
    val saving by vm.saving.collectAsState()
    val error by vm.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text("Payment Methods", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Text("Configure how tenants can pay rent", fontSize = 13.sp, color = Gray500)
        }

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp) }

        if (loading || config == null) {
            LoadingContent()
            return@Column
        }

        val cfg = config!!
        val mpesaMethod = cfg.methods.find { it.methodId == "mpesa" }
        val paypalMethod = cfg.methods.find { it.methodId == "paypal" }
        val bankMethod = cfg.methods.find { it.methodId == "bank_transfer" }

        // Method toggles card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text("Payment Channels", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Gray900)
                    Text("Enable or disable methods for tenants", fontSize = 12.sp, color = Gray500)
                    Spacer(Modifier.height(8.dp))

                    val methodRows = listOf(
                        Triple("📱", "M-Pesa", mpesaMethod),
                        Triple("💰", "PayPal", paypalMethod),
                        Triple("🏦", "Bank Transfer", bankMethod),
                    )
                    methodRows.forEach { (icon, label, method) ->
                        if (method != null) {
                            HorizontalDivider(color = Gray100)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = Gray50) {
                                        Text(icon, modifier = Modifier.padding(8.dp), fontSize = 18.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Gray900)
                                        Text(
                                            if (method.enabled) "Enabled" else "Disabled",
                                            fontSize = 12.sp,
                                            color = if (method.enabled) Success600 else Gray500
                                        )
                                    }
                                }
                                Switch(
                                    checked = method.enabled,
                                    onCheckedChange = { vm.toggleMethod(method.methodId, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = Brand600)
                                )
                            }
                        }
                    }
                }
            }
        }

        // M-Pesa config
        if (mpesaMethod?.enabled == true) {
            MpesaConfigSection(initial = cfg.mpesaConfig, saving = saving, onSave = { vm.updateMpesa(it, onSuccess = {}) })
        }

        // PayPal config
        if (paypalMethod?.enabled == true) {
            PaypalConfigSection(initial = cfg.paypalConfig, saving = saving, onSave = { vm.updatePaypal(it, onSuccess = {}) })
        }

        // Banks
        if (bankMethod?.enabled == true && cfg.banks.isNotEmpty()) {
            BanksSection(banks = cfg.banks, onToggle = { id, en -> vm.toggleBank(id, en) })
        }
    }
}

@Composable
private fun SectionCard(title: String, icon: String, accentColor: androidx.compose.ui.graphics.Color = Brand600, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, Gray300),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(accentColor))
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Gray900)
                }
                content()
            }
        }
    }
}

@Composable
private fun MpesaConfigSection(
    initial: com.buildagent.shared.models.MpesaConfig?,
    saving: Boolean,
    onSave: (UpdateMpesaConfigRequest) -> Unit
) {
    var businessNo by remember(initial) { mutableStateOf(initial?.businessNo ?: "") }
    var accountNo by remember(initial) { mutableStateOf(initial?.accountNo ?: "") }
    var instructions by remember(initial) { mutableStateOf(initial?.instructions ?: "") }

    SectionCard("M-Pesa Configuration", "📱") {
        OutlinedTextField(value = businessNo, onValueChange = { businessNo = it },
            label = { Text("Business No *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600))
        OutlinedTextField(value = accountNo, onValueChange = { accountNo = it },
            label = { Text("Account No *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600))
        OutlinedTextField(value = instructions, onValueChange = { instructions = it },
            label = { Text("Instructions (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600))
        Button(
            onClick = {
                if (businessNo.isNotBlank() && accountNo.isNotBlank())
                    onSave(UpdateMpesaConfigRequest(businessNo.trim(), accountNo.trim(), instructions.trim().ifBlank { null }))
            },
            enabled = !saving,
            colors = ButtonDefaults.buttonColors(containerColor = Brand600),
            shape = RoundedCornerShape(8.dp)
        ) { Text(if (saving) "Saving…" else "Save M-Pesa Config") }
    }
}

@Composable
private fun PaypalConfigSection(
    initial: com.buildagent.shared.models.PaypalConfig?,
    saving: Boolean,
    onSave: (UpdatePaypalConfigRequest) -> Unit
) {
    var email by remember(initial) { mutableStateOf(initial?.email ?: "") }
    var instructions by remember(initial) { mutableStateOf(initial?.instructions ?: "") }

    SectionCard("PayPal Configuration", "💰", accentColor = Cyan500) {
        OutlinedTextField(value = email, onValueChange = { email = it },
            label = { Text("PayPal Email *") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600))
        OutlinedTextField(value = instructions, onValueChange = { instructions = it },
            label = { Text("Instructions (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Gray300, focusedBorderColor = Brand600))
        Button(
            onClick = {
                if (email.isNotBlank())
                    onSave(UpdatePaypalConfigRequest(email.trim(), instructions.trim().ifBlank { null }))
            },
            enabled = !saving,
            colors = ButtonDefaults.buttonColors(containerColor = Brand600),
            shape = RoundedCornerShape(8.dp)
        ) { Text(if (saving) "Saving…" else "Save PayPal Config") }
    }
}

@Composable
private fun BanksSection(banks: List<BankConfig>, onToggle: (String, Boolean) -> Unit) {
    SectionCard("Bank Accounts", "🏦", accentColor = Success600) {
        banks.forEachIndexed { i, bank ->
            if (i > 0) HorizontalDivider(color = Gray100)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(bank.bankName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray900)
                    Text(if (bank.enabled) "Active" else "Inactive", fontSize = 12.sp,
                        color = if (bank.enabled) Success600 else Gray500)
                }
                Switch(
                    checked = bank.enabled,
                    onCheckedChange = { onToggle(bank.bankId, it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = White, checkedTrackColor = Brand600)
                )
            }
        }
    }
}
