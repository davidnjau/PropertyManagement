package com.buildagent.ui.screens.portfolio

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
import cafe.adriel.voyager.koin.getScreenModel
import com.buildagent.shared.models.Building
import com.buildagent.ui.components.LoadingContent
import com.buildagent.ui.theme.*

@Composable
fun BuildingsScreen() {
    val vm = getScreenModel<PortfolioViewModel>()
    val buildings by vm.buildings.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Portfolio", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("${buildings.size} buildings", fontSize = 13.sp, color = Gray500)
        Spacer(Modifier.height(20.dp))

        when {
            loading -> LoadingContent()
            error != null -> Text("Error: $error", color = Danger600)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(buildings) { building -> BuildingCard(building) }
            }
        }
    }
}

@Composable
fun BuildingCard(building: Building) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = building.name ?: building.address,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${building.suburb}, ${building.state} ${building.postcode}",
                fontSize = 13.sp,
                color = Gray500
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${building.unitCount} units", fontSize = 12.sp, color = Gray700)
                building.client?.let {
                    Text("Owner: ${it.fullName}", fontSize = 12.sp, color = Gray700)
                }
            }
        }
    }
}
