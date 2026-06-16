package com.buildagent.ui.screens.tenancy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*

@Composable
fun TenantsScreen() {
    val vm = koinInject<TenancyViewModel>()
    val tenants by vm.tenants.collectAsState()
    val loading by vm.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Tenants", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("${tenants.size} tenants", fontSize = 13.sp, color = Gray500)
        Spacer(Modifier.height(20.dp))

        if (loading) { LoadingContent() } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tenants) { tenant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(tenant.fullName, fontWeight = FontWeight.Medium)
                                Text(tenant.email, fontSize = 13.sp, color = Gray500)
                                tenant.phone?.let { Text(it, fontSize = 12.sp, color = Gray500) }
                            }
                        }
                    }
                }
            }
        }
    }
}
