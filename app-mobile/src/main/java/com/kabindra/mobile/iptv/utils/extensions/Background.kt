package com.kabindra.mobile.iptv.utils.extensions

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

@Composable
fun Modifier.mainBackground(): Modifier {
    val backgroundColor = MaterialTheme.colorScheme.background
    val deepTone = MaterialTheme.colorScheme.surface

    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                deepTone,
                backgroundColor,
            ),
        )
    )
}
