package com.buildagent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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

val adminNavItems = listOf(
    NavItem("Alerts",            "🔔", 6),
    NavItem("Documents",         "📁", 7),
    NavItem("Payment Methods",   "⚙️", 8),
    NavItem("Lease Extensions",  "📋", 9),
    NavItem("Team Members",      "👤", 10),
)

@Composable
fun AppDrawer(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    roles: List<String> = emptyList(),
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Sidebar)
            .padding(12.dp)
    ) {
        // Scrollable nav content
        Column(modifier = Modifier.weight(1f)) {
            // Logo lockup
            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
                AppLogoLockup(logoSize = 36.dp, nameSize = 17.sp, subtitleSize = 10.sp)
            }

            Spacer(Modifier.height(8.dp))

            navItems.forEach { item ->
                DrawerNavRow(item, selectedIndex, onSelect)
            }

            if ("ADMIN" in roles || "AGENT" in roles) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = SidebarDivider, thickness = 1.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "ADMIN",
                    color = SidebarLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                adminNavItems.forEach { item ->
                    DrawerNavRow(item, selectedIndex, onSelect)
                }
            }
        }

        // Sign out pinned at bottom
        HorizontalDivider(color = SidebarDivider, thickness = 1.dp)
        Spacer(Modifier.height(4.dp))
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
            Text("Sign Out", color = SidebarText, fontSize = 14.sp)
        }
    }
}

@Composable
private fun DrawerNavRow(item: NavItem, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val selected = selectedIndex == item.index
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) SidebarSelected else Color.Transparent)
            .clickable { onSelect(item.index) }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = item.icon, fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = item.label,
            color = if (selected) White else SidebarText,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (selected) {
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brand600)
            )
        }
    }
}
