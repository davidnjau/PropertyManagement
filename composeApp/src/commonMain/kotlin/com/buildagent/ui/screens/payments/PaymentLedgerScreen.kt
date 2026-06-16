package com.buildagent.ui.screens.payments

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
import com.buildagent.shared.models.Payment
import com.buildagent.ui.components.*
import com.buildagent.ui.theme.*

@Composable
fun PaymentLedgerScreen() {
    val vm = koinInject<PaymentsViewModel>()
    val payments by vm.payments.collectAsState()
    val overdue by vm.overduePayments.collectAsState()
    val loading by vm.loading.collectAsState()
    var tab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Payment Ledger", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
            Tab(selected = tab == 0, onClick = { tab = 0; vm.loadPayments() }, text = { Text("All Payments") })
            Tab(selected = tab == 1, onClick = { tab = 1; vm.loadOverdue() }, text = { Text("Overdue") })
        }
        Spacer(Modifier.height(16.dp))

        val list = if (tab == 0) payments else overdue
        if (loading) { LoadingContent() } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { payment -> PaymentRow(payment) }
                if (list.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No payments found", color = Gray500)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentRow(payment: Payment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (payment.status.name == "OVERDUE") Danger100 else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(payment.lease?.tenant?.fullName ?: "—", fontWeight = FontWeight.Medium)
                Text(payment.paymentType.name, fontSize = 12.sp, color = Gray500)
                Text("${payment.periodFrom} — ${payment.periodTo}", fontSize = 12.sp, color = Gray500)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("A${"%.2f".format(payment.amount)}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                StatusBadge(payment.status.name, paymentStatusBadge)
            }
        }
    }
}
