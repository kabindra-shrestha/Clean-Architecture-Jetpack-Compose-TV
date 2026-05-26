package com.kabindra.mobile.iptv.presentation.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import network.chaintech.sdpcomposemultiplatform.sdp

private object DashboardTileTokens {
    const val cornerRadius = 10
    const val tileSize = 118
    const val tilePadding = 12
    const val iconSize = 28
    const val contentSpacing = 10
    const val borderWidth = 2
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
    val shape =
        RoundedCornerShape(DashboardTileTokens.cornerRadius.sdp)
    var isFocused by remember { mutableStateOf(false) }
    val isHighlighted = selected || isFocused

    Card(
        onClick = if (enabled) onClick else ({}),
        modifier = modifier
            .alpha(if (enabled) 1f else 0.6f)
            .size(DashboardTileTokens.tileSize.sdp)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocused?.invoke()
                }
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
            },
            contentColor = if (isHighlighted) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f)
            }
        ),
        border = if (isHighlighted) {
            BorderStroke(
                width = DashboardTileTokens.borderWidth.sdp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
            )
        } else {
            null
        }
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
                tint = if (isHighlighted) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f)
                }
            )

            Spacer(
                modifier = Modifier.size(DashboardTileTokens.contentSpacing.sdp)
            )

            TextComponent(
                text = title,
                type = TextType.Title,
                size = TextSize.Large,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f)
                }
            )
        }
    }
}
