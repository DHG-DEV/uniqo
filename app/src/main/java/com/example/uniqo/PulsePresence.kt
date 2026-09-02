package com.example.uniqo

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

enum class PulseStatus { ACTIVE, RECENT, IDLE, OFFLINE, LONG_OFFLINE }

private fun pulseStatus(lastActiveAt: Long?): PulseStatus {
    if (lastActiveAt == null) return PulseStatus.OFFLINE
    val age = System.currentTimeMillis() - lastActiveAt
    return when {
        age < TimeUnit.MINUTES.toMillis(5) -> PulseStatus.ACTIVE
        age < TimeUnit.MINUTES.toMillis(20) -> PulseStatus.RECENT
        age < TimeUnit.HOURS.toMillis(1) -> PulseStatus.IDLE
        age < TimeUnit.HOURS.toMillis(24) -> PulseStatus.OFFLINE
        else -> PulseStatus.LONG_OFFLINE
    }
}

private fun lastPulseText(lastActiveAt: Long?): String {
    if (lastActiveAt == null) return "Last pulse unknown"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastActiveAt)
    return when {
        minutes < 1 -> "Pulsing now"
        minutes < 60 -> "Last pulse ${minutes}m ago"
        minutes < 1440 -> "Last pulse ${minutes / 60}h ago"
        else -> "Last pulse ${minutes / 1440}d ago"
    }
}

@Composable
fun PulsePresence(
    lastActiveAt: Long?,
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    val status = pulseStatus(lastActiveAt)
    val infinite = rememberInfiniteTransition(label = "pulse")
    val phase by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                when (status) {
                    PulseStatus.ACTIVE -> 1200
                    PulseStatus.RECENT -> 1800
                    PulseStatus.IDLE -> 2800
                    else -> 1
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulsePhase"
    )

    val alpha = when (status) {
        PulseStatus.ACTIVE -> 1f
        PulseStatus.RECENT -> .7f
        PulseStatus.IDLE -> .4f
        PulseStatus.OFFLINE -> .25f
        PulseStatus.LONG_OFFLINE -> .12f
    }

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.size(12.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val color = PurplePrimary.copy(alpha = alpha)
            drawCircle(color, 3.5.dp.toPx(), center)
            if (status <= PulseStatus.IDLE) {
                val radius = 3.5.dp.toPx() + (size.minDimension / 2) * phase
                drawCircle(color.copy(alpha = (1f - phase) * alpha), radius, center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
            } else {
                drawCircle(color, 5.dp.toPx(), center, style = androidx.compose.ui.graphics.drawscope.Stroke(1.2.dp.toPx()))
            }
        }
        if (showText) {
            Text(
                lastPulseText(lastActiveAt),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = alpha)
            )
        }
    }
}