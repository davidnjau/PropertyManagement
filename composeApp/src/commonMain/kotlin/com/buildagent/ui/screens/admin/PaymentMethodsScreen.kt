package com.buildagent.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Text("Payment Methods", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp) }

        if (loading || config == null) {
            LoadingContent()
            return@Column
        }

        val cfg = config!!
        val mpesaMethod = cfg.methods.find { it.methodId == "mpesa" }
        val paypalMethod = cfg.methods.find { it.methodId == "paypal" }
        val bankMethod = cfg.methods.find { it.methodId == "bank_transfer" }

        // Section 1: Method toggles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Methods", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                if (mpesaMethod != null) {
                    MethodToggleRow(
                        label = "M-Pesa",
                        enabled = mpesaMethod.enabled,
                        onToggle = { vm.toggleMethod("mpesa", it) }
                    )
                }
                if (paypalMethod != null) {
                    MethodToggleRow(
                        label = "PayPal",
                        enabled = paypalMethod.enabled,
                        onToggle = { vm.toggleMethod("paypal", it) }
                    )
                }
                if (bankMethod != null) {
                    MethodToggleRow(
                        label = "Bank Transfer",
                        enabled = bankMethod.enabled,
                        onToggle = { vm.toggleMethod("bank_transfer", it) }
                    )
                }
            }
        }

        // Section 2: M-Pesa config
        if (mpesaMethod?.enabled == true) {
            MpesaConfigSection(
                initial = cfg.mpesaConfig,
                saving = saving,
                onSave = { req -> vm.updateMpesa(req, onSuccess = {}) }
            )
        }

        // Section 3: PayPal config
        if (paypalMethod?.enabled == true) {
            PaypalConfigSection(
                initial = cfg.paypalConfig,
                saving = saving,
                onSave = { req -> vm.updatePaypal(req, onSuccess = {}) }
            )
        }

        // Section 4: Banks
        if (bankMethod?.enabled == true && cfg.banks.isNotEmpty()) {
            BanksSection(banks = cfg.banks, onToggle = { bankId, enabled -> vm.toggleBank(bankId, enabled) })
        }
    }
}

@Composable
private fun MethodToggleRow(label: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp)
        Switch(checked = enabled, onCheckedChange = onToggle)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("M-Pesa Configuration", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            OutlinedTextField(value = businessNo, onValueChange = { businessNo = it }, label = { Text("Business No *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = accountNo, onValueChange = { accountNo = it }, label = { Text("Account No *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instructions (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
            Button(
                onClick = {
                    if (businessNo.isNotBlank() && accountNo.isNotBlank()) {
                        onSave(UpdateMpesaConfigRequest(businessNo.trim(), accountNo.trim(), instructions.trim().ifBlank { null }))
                    }
                },
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (saving) "Saving…" else "Save M-Pesa Config")
            }
        }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("PayPal Configuration", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("PayPal Email *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instructions (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onSave(UpdatePaypalConfigRequest(email.trim(), instructions.trim().ifBlank { null }))
                    }
                },
                enabled = !saving,
                colors = ButtonDefaults.buttonColors(containerColor = Brand600),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (saving) "Saving…" else "Save PayPal Config")
            }
        }
    }
}

@Composable
private fun BanksSection(banks: List<BankConfig>, onToggle: (String, Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Banks", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            banks.forEach { bank ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(bank.bankName, fontSize = 14.sp)
                    Switch(
                        checked = bank.enabled,
                        onCheckedChange = { onToggle(bank.bankId, it) }
                    )
                }
                Divider(color = Gray100)
            }
        }
    }
}
