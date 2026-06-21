package com.buildagent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buildagent.ui.theme.*

/**
 * Legacy Real Estate Agency logo mark — gradient tile with a 2×2 grid of rounded squares
 * representing a portfolio of managed properties.
 */
@Composable
fun AppLogoMark(size: Dp = 40.dp) {
    val corner = RoundedCornerShape((size.value * 0.28f).dp)
    val innerSize = (size.value * 0.28f).dp
    val gap = (size.value * 0.07f).dp

    Box(
        modifier = Modifier
            .size(size)
            .background(Brush.linearGradient(listOf(Brand600, Cyan500)), corner),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                Box(Modifier.size(innerSize).background(White, RoundedCornerShape(2.dp)))
                Box(Modifier.size(innerSize).background(White.copy(alpha = 0.55f), RoundedCornerShape(2.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                Box(Modifier.size(innerSize).background(White.copy(alpha = 0.55f), RoundedCornerShape(2.dp)))
                Box(Modifier.size(innerSize).background(White, RoundedCornerShape(2.dp)))
            }
        }
    }
}

/** Full lockup: logo mark + "Legacy Real Estate Agency" wordmark side by side. */
@Composable
fun AppLogoLockup(
    logoSize: Dp = 40.dp,
    nameSize: TextUnit = 13.sp,
    subtitleSize: TextUnit = 10.sp,
    subtitle: String = "Property Management"
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppLogoMark(size = logoSize)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = "Legacy Real Estate Agency",
                color = White,
                fontSize = nameSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = SidebarLabel,
                    fontSize = subtitleSize,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
