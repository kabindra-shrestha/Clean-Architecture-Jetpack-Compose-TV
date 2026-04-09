package com.kabindra.tv.iptv.presentation.ui.component.notification

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import com.kabindra.tv.iptv.domain.entity.NotificationPriority
import com.kabindra.tv.iptv.presentation.ui.theme.ColorCritical
import com.kabindra.tv.iptv.presentation.ui.theme.ColorHigh
import com.kabindra.tv.iptv.presentation.ui.theme.ColorLow
import com.kabindra.tv.iptv.presentation.ui.theme.ColorNormal
import com.kabindra.tv.iptv.presentation.ui.theme.ConnectionStateConnected
import com.kabindra.tv.iptv.presentation.ui.theme.ConnectionStateConnecting
import com.kabindra.tv.iptv.presentation.ui.theme.ConnectionStateDisconnected
import com.kabindra.tv.iptv.presentation.ui.theme.ConnectionStateError
import com.kabindra.tv.iptv.presentation.ui.theme.plum700
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// ConnectionStatusBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ConnectionStatusBar(
    state: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (dotColor, label) = when (state) {
        is ConnectionState.Connected -> ConnectionStateConnected to "● Connected"
        is ConnectionState.Connecting -> ConnectionStateConnecting to "◌ Connecting…"
        is ConnectionState.Reconnecting -> ConnectionStateConnecting to "↺ Reconnecting (attempt ${state.attempt})…"
        is ConnectionState.Error -> ConnectionStateError to "✕ Error: ${state.message}"
        is ConnectionState.Disconnected -> ConnectionStateDisconnected to "○ Disconnected"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val isAnimating = state is ConnectionState.Connecting || state is ConnectionState.Reconnecting

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(plum700)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = if (isAnimating) alpha else 1f))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 14.sp
            )
        }
        Text(
            text = "TV Notify  |  Intranet",
            fontSize = 12.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PriorityBadge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PriorityBadge(priority: NotificationPriority) {
    val (color, label) = when (priority) {
        NotificationPriority.CRITICAL -> ColorCritical to "CRITICAL"
        NotificationPriority.HIGH -> ColorHigh to "HIGH"
        NotificationPriority.NORMAL -> ColorNormal to "NORMAL"
        NotificationPriority.LOW -> ColorLow to "LOW"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NotificationCard
// ─────────────────────────────────────────────────────────────────────────────

private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

@Composable
fun NotificationCard(
    message: NotificationMessage,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (message.priorityEnum) {
        NotificationPriority.CRITICAL -> ColorCritical
        NotificationPriority.HIGH -> ColorHigh
        NotificationPriority.NORMAL -> ColorNormal
        NotificationPriority.LOW -> ColorLow
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(plum700)
            .border(
                width = 1.dp,
                color = priorityColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left color accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(priorityColor)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                PriorityBadge(priority = message.priorityEnum)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = message.message,
                fontSize = 14.sp,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = timeFormatter.format(Date(message.timestamp)),
                fontSize = 12.sp
            )
        }
    }
}
