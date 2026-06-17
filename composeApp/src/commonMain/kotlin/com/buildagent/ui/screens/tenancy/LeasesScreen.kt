package com.buildagent.ui.screens.tenancy

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
import com.buildagent.ui.components.*
import com.buildagent.ui.utils.fmt0dp
import com.buildagent.ui.theme.*

@Composable
fun LeasesScreen() {
    val vm = koinInject<TenancyViewModel>()
    val leases by vm.leases.collectAsState()
    val loading by vm.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Leases", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        if (loading) { LoadingContent() } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(leases) { lease ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lease.tenant?.fullName ?: "—", fontWeight = FontWeight.Medium)
                                Text("A$${lease.rentAmount.fmt0dp()}/mo", fontSize = 13.sp, color = Gray500)
                                Text("${lease.startDate} — ${lease.endDate ?: "Periodic"}", fontSize = 12.sp, color = Gray500)
                            }
                            val displayStatus = (lease.computedStatus ?: lease.status).name
                            StatusBadge(displayStatus, leaseStatusBadge)
                        }
                    }
                }
            }
        }
    }
}
