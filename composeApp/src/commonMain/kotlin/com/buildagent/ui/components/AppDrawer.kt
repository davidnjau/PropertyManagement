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

data class NavItem(val label: String, val icon: String, val index: Int)

val navItems = listOf(
    NavItem("Dashboard",   "📊", 0),
    NavItem("Portfolio",   "🏢", 1),
    NavItem("Tenants",     "👥", 2),
    NavItem("Leases",      "📄", 3),
    NavItem("Payments",    "💳", 4),
    NavItem("Maintenance", "🔧", 5),
)

@Composable
fun AppDrawer(selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Sidebar)
            .padding(12.dp)
    ) {
        Text(
            text = "BuildAgent",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        navItems.forEach { item ->
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
    }
}
