package com.kabindra.tv.iptv.presentation.ui.screen.dashboard

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
import com.kabindra.tv.iptv.utils.extensions.mainBackground
import kotlinx.coroutines.delay
import network.chaintech.sdpcomposemultiplatform.sdp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private object DashboardScreenTokens {
    const val contentHorizontalPadding = 20
    const val contentVerticalPadding = 20
    const val menuSpacing = 24
}

@Composable
fun DashboardScreen(
    innerPadding: PaddingValues,
    onNavigateLiveTV: () -> Unit,
    onNavigateMovie: () -> Unit,
) {
    val menuItems = remember { DashboardMenuType.entries.toList() }
    val lazyState = rememberBaseLazyState()
    var selectedMenu by remember { mutableStateOf(DashboardMenuType.LiveTV) }
    val clockText by rememberDashboardClockText()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
            .padding(innerPadding)
    ) {
        TextComponent(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = DashboardScreenTokens.contentHorizontalPadding.sdp,
                    top = DashboardScreenTokens.contentVerticalPadding.sdp
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
                    top = DashboardScreenTokens.contentVerticalPadding.sdp
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
                .align(Alignment.BottomCenter)
                .padding(
                    top = DashboardScreenTokens.contentVerticalPadding.sdp,
                    bottom = DashboardScreenTokens.contentVerticalPadding.sdp
                ),
            layout = BaseLazyLayout.List,
            orientation = BaseLazyOrientation.Horizontal,
            platform = BaseLazyPlatform.AndroidTv,
            tvConfig = TvLazyConfig(
                initialFocusedIndex = DashboardMenuType.LiveTV.ordinal,
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
                    when (item) {
                        DashboardMenuType.LiveTV -> onNavigateLiveTV()
                        DashboardMenuType.Movie -> onNavigateMovie()
                        DashboardMenuType.Profile -> Unit
                    }
                }
            )
        }
    }
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
