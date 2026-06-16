package com.buildagent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildagent.ui.theme.*

data class BadgeConfig(val label: String, val bg: Color, val fg: Color)

val leaseStatusBadge = mapOf(
    "ACTIVE"        to BadgeConfig("Active",        Success100, Success600),
    "PERIODIC"      to BadgeConfig("Periodic",      Brand100,   Brand600),
    "EXPIRING_SOON" to BadgeConfig("Expiring Soon", Warning100, Warning600),
    "EXPIRED"       to BadgeConfig("Expired",       Danger100,  Danger600),
    "VACATED"       to BadgeConfig("Vacated",       Gray100,    Gray700),
    "TERMINATED"    to BadgeConfig("Terminated",    Danger100,  Danger600),
)

val paymentStatusBadge = mapOf(
    "RECEIVED" to BadgeConfig("Received", Success100, Success600),
    "PENDING"  to BadgeConfig("Pending",  Warning100, Warning600),
    "OVERDUE"  to BadgeConfig("Overdue",  Danger100,  Danger600),
    "PARTIAL"  to BadgeConfig("Partial",  Brand100,   Brand600),
    "WAIVED"   to BadgeConfig("Waived",   Gray100,    Gray700),
)

val maintenanceStatusBadge = mapOf(
    "REPORTED"    to BadgeConfig("Reported",    Brand100,   Brand600),
    "ASSESSED"    to BadgeConfig("Assessed",    Brand100,   Brand600),
    "ASSIGNED"    to BadgeConfig("Assigned",    Warning100, Warning600),
    "IN_PROGRESS" to BadgeConfig("In Progress", Warning100, Warning600),
    "COMPLETED"   to BadgeConfig("Completed",   Success100, Success600),
    "CLOSED"      to BadgeConfig("Closed",      Gray100,    Gray700),
    "CANCELLED"   to BadgeConfig("Cancelled",   Gray100,    Gray700),
)

val priorityBadge = mapOf(
    "EMERGENCY" to BadgeConfig("Emergency", Danger100,  Danger600),
    "URGENT"    to BadgeConfig("Urgent",    Warning100, Warning600),
    "ROUTINE"   to BadgeConfig("Routine",   Brand100,   Brand600),
    "LOW"       to BadgeConfig("Low",       Gray100,    Gray700),
)

val unitStatusBadge = mapOf(
    "VACANT"            to BadgeConfig("Vacant",            Warning100, Warning600),
    "OCCUPIED"          to BadgeConfig("Occupied",          Success100, Success600),
    "UNDER_MAINTENANCE" to BadgeConfig("Under Maintenance", Danger100,  Danger600),
    "OFF_MARKET"        to BadgeConfig("Off Market",        Gray100,    Gray700),
)

@Composable
fun StatusBadge(status: String, map: Map<String, BadgeConfig>, modifier: Modifier = Modifier) {
    val config = map[status] ?: BadgeConfig(status, Gray100, Gray700)
    Text(
        text = config.label,
        color = config.fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .background(config.bg, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
