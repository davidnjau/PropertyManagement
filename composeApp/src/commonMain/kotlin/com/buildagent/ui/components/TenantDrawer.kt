package com.buildagent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildagent.ui.theme.*

private val tenantNavItems = listOf(
    NavItem("Overview",    "🏠", 0),
    NavItem("Pay Rent",    "💳", 1),
    NavItem("My Lease",    "📄", 2),
    NavItem("Maintenance", "🔧", 3),
    NavItem("Documents",   "📁", 4),
)

@Composable
fun TenantDrawer(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    userName: String,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Sidebar)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
            Text(
                text = "BuildAgent",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userName,
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(8.dp))

        tenantNavItems.forEach { item ->
            val selected = selectedIndex == item.index
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) Brand600 else Color.Transparent)
                    .clickable { onSelect(item.index) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(text = item.icon, fontSize = 16.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = item.label,
                    color = if (selected) Color.White else Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSignOut() }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text("🚪", fontSize = 16.sp)
            Spacer(Modifier.width(10.dp))
            Text("Sign Out", color = Color(0xFF9CA3AF), fontSize = 14.sp)
        }
    }
}
