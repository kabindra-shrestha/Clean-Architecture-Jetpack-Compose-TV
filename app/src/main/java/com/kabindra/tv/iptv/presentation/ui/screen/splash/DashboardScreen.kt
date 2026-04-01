package com.kabindra.tv.iptv.presentation.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.MaterialTheme
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazy
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyLayout
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyOrientation
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyPlatform
import com.kabindra.tv.iptv.presentation.ui.component.DashboardTileComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextSize
import com.kabindra.tv.iptv.presentation.ui.component.TextType
import com.kabindra.tv.iptv.presentation.ui.component.TvLazyConfig
import com.kabindra.tv.iptv.presentation.ui.component.rememberBaseLazyState
import com.kabindra.tv.iptv.utils.enums.DashboardMenuType
import kotlinx.coroutines.delay
import network.chaintech.sdpcomposemultiplatform.sdp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private object DashboardScreenTokens {
    const val contentHorizontalPadding = 35
    const val contentTopPadding = 35
    const val menuSpacing = 24
}

@Composable
fun DashboardScreen(
    innerPadding: PaddingValues,
    onNavigateLogin: () -> Unit,
) {
    val menuItems = remember { DashboardMenuType.entries.toList() }
    val lazyState = rememberBaseLazyState()
    var selectedMenu by remember { mutableStateOf(DashboardMenuType.LiveTv) }
    val clockText by rememberDashboardClockText()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .dashboardBackground()
            .padding(innerPadding)
    ) {
        TextComponent(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = DashboardScreenTokens.contentHorizontalPadding.sdp,
                    top = DashboardScreenTokens.contentTopPadding.sdp
                ),
            text = "Welcome, Guest",
            type = TextType.Display,
            size = TextSize.Small,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        TextComponent(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    end = DashboardScreenTokens.contentHorizontalPadding.sdp,
                    top = DashboardScreenTokens.contentTopPadding.sdp
                ),
            text = clockText,
            type = TextType.Headline,
            size = TextSize.Large,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        BaseLazy(
            items = menuItems,
            state = lazyState,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter),
            layout = BaseLazyLayout.List,
            orientation = BaseLazyOrientation.Horizontal,
            platform = BaseLazyPlatform.AndroidTv,
            tvConfig = TvLazyConfig(
                initialFocusedIndex = DashboardMenuType.LiveTv.ordinal,
                autoScrollOnFocus = true,
                focusedItemOffsetFraction = 0.16f
            ),
            contentPadding = PaddingValues(horizontal = DashboardScreenTokens.contentHorizontalPadding.sdp),
            arrangement = Arrangement.spacedBy(DashboardScreenTokens.menuSpacing.sdp),
            userScrollEnabled = true,
            key = { _, item -> item.name },
            contentType = { _, _ -> "dashboard_tile" },
        ) { _, item, itemModifier ->
            DashboardTileComponent(
                modifier = itemModifier,
                title = item.title,
                icon = item.icon,
                selected = selectedMenu == item,
                onFocused = {
                    selectedMenu = item
                },
                onClick = {
                    selectedMenu = item
                }
            )
        }
    }
}

@Composable
private fun Modifier.dashboardBackground(): Modifier {
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

@Composable
private fun rememberDashboardClockText() = produceState(
    initialValue = currentDashboardTime()
) {
    while (true) {
        value = currentDashboardTime()
        delay(1_000)
    }
}

private fun currentDashboardTime(): String {
    return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
}
