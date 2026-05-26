package com.kabindra.mobile.iptv.presentation.ui.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class MobileWindowSizeClass {
    Compact,
    Medium,
    Expanded,
}

enum class MobileNavigationType {
    BottomBar,
    NavigationRail,
}

@Immutable
data class MobileAdaptiveInfo(
    val widthClass: MobileWindowSizeClass,
    val heightClass: MobileWindowSizeClass,
    val maxWidth: Dp,
    val maxHeight: Dp,
) {
    val isLandscape: Boolean = maxWidth > maxHeight
    val navigationType: MobileNavigationType =
        if (widthClass == MobileWindowSizeClass.Compact) {
            MobileNavigationType.BottomBar
        } else {
            MobileNavigationType.NavigationRail
        }

    val horizontalPadding: Dp = when (widthClass) {
        MobileWindowSizeClass.Compact -> 16.dp
        MobileWindowSizeClass.Medium -> 24.dp
        MobileWindowSizeClass.Expanded -> 32.dp
    }

    val verticalPadding: Dp = when (heightClass) {
        MobileWindowSizeClass.Compact -> 12.dp
        MobileWindowSizeClass.Medium -> 20.dp
        MobileWindowSizeClass.Expanded -> 24.dp
    }

    val gridMinCellSize: Dp = when (widthClass) {
        MobileWindowSizeClass.Compact -> 148.dp
        MobileWindowSizeClass.Medium -> 168.dp
        MobileWindowSizeClass.Expanded -> 184.dp
    }

    val dashboardCardMinSize: Dp = when (widthClass) {
        MobileWindowSizeClass.Compact -> 260.dp
        MobileWindowSizeClass.Medium -> 280.dp
        MobileWindowSizeClass.Expanded -> 320.dp
    }
}

@Composable
fun MobileAdaptiveContent(
    modifier: Modifier = Modifier,
    content: @Composable (MobileAdaptiveInfo) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val info = remember(maxWidth, maxHeight) {
            MobileAdaptiveInfo(
                widthClass = maxWidth.toMobileWindowSizeClass(),
                heightClass = maxHeight.toMobileWindowSizeClass(),
                maxWidth = maxWidth,
                maxHeight = maxHeight,
            )
        }
        content(info)
    }
}

fun PaddingValues.plus(
    horizontal: Dp,
    vertical: Dp,
): PaddingValues {
    return PaddingValues(
        start = calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + horizontal,
        top = calculateTopPadding() + vertical,
        end = calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + horizontal,
        bottom = calculateBottomPadding() + vertical,
    )
}

private fun Dp.toMobileWindowSizeClass(): MobileWindowSizeClass {
    return when {
        this < 600.dp -> MobileWindowSizeClass.Compact
        this < 840.dp -> MobileWindowSizeClass.Medium
        else -> MobileWindowSizeClass.Expanded
    }
}
