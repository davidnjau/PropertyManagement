package com.buildagent.ui.screens.tenant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.components.StatusBadge
import com.buildagent.ui.components.leaseStatusBadge
import com.buildagent.ui.components.paymentStatusBadge
import com.buildagent.ui.theme.*

@Composable
fun TenantOverviewScreen() {
    val vm = koinInject<TenantPortalViewModel>()
    val overview by vm.overview.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Overview", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        error?.let { Text("Error: $it", color = Danger600, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }

        if (loading || overview == null) {
            LoadingContent()
            return@Column
        }

        val ov = overview!!

        // Rent due card (dark background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Gray900, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text("Rent Due", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (ov.rentDue != null) "A$${"%.2f".format(ov.rentDue)}" else "Up to date",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                ov.rentDueDate?.let {
                    Spacer(Modifier.height(4.dp))
                    Text("Due: $it", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                }
                ov.leaseStatus?.let {
                    Spacer(Modifier.height(10.dp))
                    StatusBadge(it, leaseStatusBadge)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Lease expiry warning
        ov.leaseEndDate?.let { endDate ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Warning100)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚠️", fontSize = 18.sp)
                    Column {
                        Text("Lease expires $endDate", fontWeight = FontWeight.Medium, color = Warning600, fontSize = 14.sp)
                        Text("Contact your agent or use My Lease to request an extension.", fontSize = 12.sp, color = Warning600)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Recent payments
        if (ov.recentPayments.isNotEmpty()) {
            Text("Recent Payments", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ov.recentPayments) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${payment.periodFrom} – ${payment.periodTo}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(payment.paymentType.name, fontSize = 12.sp, color = Gray500)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("A$${"%.2f".format(payment.amount)}", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                StatusBadge(payment.status.name, paymentStatusBadge)
                            }
                        }
                    }
                }
            }
        }
    }
}
