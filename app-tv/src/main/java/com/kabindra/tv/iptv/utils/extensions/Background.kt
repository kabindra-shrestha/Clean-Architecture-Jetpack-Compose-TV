package com.kabindra.tv.iptv.utils.extensions

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.tv.material3.MaterialTheme

@Composable
fun Modifier.mainBackground(): Modifier {
    val darkTheme = isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val deepTone = MaterialTheme.colorScheme.surface
    val glowColor = MaterialTheme.colorScheme.primaryContainer.copy(
        alpha = if (darkTheme) 0.96f else 0.42f
    )
    val midTone = MaterialTheme.colorScheme.surfaceVariant.copy(
        alpha = if (darkTheme) 0.88f else 0.58f
    )

    return background(
        brush = Brush.linearGradient(
            colors = listOf(deepTone, backgroundColor),
        )
    ).background(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor,
                midTone,
                backgroundColor
            ),
            center = Offset(180f, 210f),
            radius = if (darkTheme) 960f else 1080f
        )
    )
}