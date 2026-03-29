package com.kabindra.tv.iptv.presentation.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.tv.material3.MaterialTheme
import network.chaintech.sdpcomposemultiplatform.sdp

private object DashboardTileTokens {
    const val cornerRadius = 10
    const val tileSize = 118
    const val tilePadding = 12
    const val iconSize = 28
    const val contentSpacing = 10
    const val borderWidth = 2
    const val focusedScale = 1.04f
    const val defaultScale = 1f
}

@Composable
fun DashboardTileComponent(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onFocused: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(DashboardTileTokens.cornerRadius.sdp)
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }
    val isHighlighted = selected || isFocused

    val containerColor by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
        },
        animationSpec = tween(durationMillis = 180),
        label = "dashboard_tile_container"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 180),
        label = "dashboard_tile_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f)
        },
        animationSpec = tween(durationMillis = 180),
        label = "dashboard_tile_content"
    )
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) {
            DashboardTileTokens.focusedScale
        } else {
            DashboardTileTokens.defaultScale
        },
        animationSpec = tween(durationMillis = 180),
        label = "dashboard_tile_scale"
    )

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.6f)
            .scale(scale)
            .size(DashboardTileTokens.tileSize.sdp)
            .clip(shape)
            .background(containerColor, shape)
            .border(
                width = DashboardTileTokens.borderWidth.sdp,
                color = borderColor,
                shape = shape
            )
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocused?.invoke()
                }
            }
            .focusable(
                enabled = enabled,
                interactionSource = interactionSource
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DashboardTileTokens.tilePadding.sdp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImageHandlerVector(
                modifier = Modifier.size(DashboardTileTokens.iconSize.sdp),
                image = icon,
                contentDescription = title,
                tint = contentColor
            )

            Spacer(
                modifier = Modifier.size(DashboardTileTokens.contentSpacing.sdp)
            )

            TextComponent(
                text = title,
                type = TextType.Title,
                size = TextSize.Large,
                color = contentColor
            )
        }
    }
}
