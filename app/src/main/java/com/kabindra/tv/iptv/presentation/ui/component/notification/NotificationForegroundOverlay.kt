package com.kabindra.tv.iptv.presentation.ui.component.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import com.kabindra.tv.iptv.domain.entity.NotificationPriority
import com.kabindra.tv.iptv.presentation.ui.theme.ColorCritical
import com.kabindra.tv.iptv.presentation.ui.theme.ColorHigh
import com.kabindra.tv.iptv.presentation.ui.theme.ColorNormal
import com.kabindra.tv.iptv.presentation.ui.theme.plum800
import com.kabindra.tv.iptv.presentation.ui.theme.plum900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("HH:mm:ss  dd MMM yyyy", Locale.getDefault())

@Composable
fun ForegroundAlertOverlay(
    message: NotificationMessage,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,   // keep modal — user must press Dismiss
            usePlatformDefaultWidth = false  // let us control the width ourselves
        )
    ) {
        val accentColor = when (message.priorityEnum) {
            NotificationPriority.CRITICAL -> ColorCritical
            NotificationPriority.HIGH -> ColorHigh
            else -> ColorNormal
        }

        Column(
            modifier = Modifier
                .width(640.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(plum800, plum900)
                    )
                )
                .background(
                    color = accentColor.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Priority badge
            PriorityBadge(priority = message.priorityEnum)

            // Title
            Text(
                text = message.title,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(1.dp)
                    .background(accentColor.copy(alpha = 0.4f))
            )

            // Message body
            Text(
                text = message.message,
                color = Color.White,
                fontSize = 18.sp
            )

            // Timestamp
            Text(
                text = dateFormatter.format(Date(message.timestamp)),
                color = Color.White,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(8.dp))

            // Dismiss button — auto-focused for D-pad
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.colors(
                    containerColor = accentColor,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .width(200.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = "Dismiss",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}