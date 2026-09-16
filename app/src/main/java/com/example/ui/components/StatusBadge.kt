package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.PowerOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceStatus
import com.example.ui.theme.GoogleAIGreen
import com.example.ui.theme.GoogleAIPurple
import com.example.ui.theme.GoogleAIRed

@Composable
fun StatusBadge(
    status: ServiceStatus,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val (color, icon, label) = when (status) {
        ServiceStatus.NORMAL -> Triple(
            GoogleAIGreen,
            Icons.Outlined.Bolt,
            "Normal / Con Luz"
        )
        ServiceStatus.SCHEDULED_OUTAGE -> Triple(
            GoogleAIRed,
            Icons.Outlined.PowerOff,
            "Corte Programado (PAC)"
        )
        ServiceStatus.IRREGULAR_OUTAGE -> Triple(
            GoogleAIPurple,
            Icons.Outlined.Warning,
            "Corte Irregular (Avería)"
        )
    }

    val needsPulse = status != ServiceStatus.NORMAL
    val alphaAnim = if (needsPulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        ).value
    } else {
        1.0f
    }

    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
            .padding(
                horizontal = if (isLarge) 16.dp else 10.dp,
                vertical = if (isLarge) 8.dp else 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isLarge) 10.dp else 8.dp)
                .graphicsLayer { alpha = alphaAnim }
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(if (isLarge) 18.dp else 14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = color,
            fontSize = if (isLarge) 14.sp else 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

