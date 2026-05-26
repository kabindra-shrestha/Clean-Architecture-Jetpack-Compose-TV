package com.kabindra.mobile.iptv.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import network.chaintech.sdpcomposemultiplatform.sdp

enum class TabsIndicatorStyle {
    Pill,
    Underlined,
}

private object TabsComponentTokens {
    const val pillHorizontalPadding = 10
    const val pillVerticalPadding = 4
    const val underlinedHorizontalPadding = 0
    const val underlinedVerticalPadding = 4
    const val underlinedSeparatorSpacing = 10
}

@Composable
fun TabsComponent(
    tabs: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    indicatorStyle: TabsIndicatorStyle = TabsIndicatorStyle.Pill,
    onSelectedIndexChange: (Int) -> Unit,
) {
    if (tabs.isEmpty()) return

    val clampedSelectedIndex = selectedIndex.coerceIn(0, tabs.lastIndex)

    TabRow(
        selectedTabIndex = clampedSelectedIndex,
        modifier = modifier,
        indicator = { tabPositions ->
            tabPositions.getOrNull(clampedSelectedIndex)?.let { currentTabPosition ->
                when (indicatorStyle) {
                    TabsIndicatorStyle.Pill -> {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(currentTabPosition)
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .zIndex(-1f)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        )
                    }

                    TabsIndicatorStyle.Underlined -> {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(currentTabPosition),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            key(title, index) {
                Tab(
                    selected = clampedSelectedIndex == index,
                    onClick = { onSelectedIndexChange(index) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ) {
                    TextComponent(
                        text = title,
                        type = TextType.Title,
                        size = TextSize.Medium,
                        color = if (clampedSelectedIndex == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.padding(
                            horizontal = when (indicatorStyle) {
                                TabsIndicatorStyle.Pill -> TabsComponentTokens.pillHorizontalPadding.sdp
                                TabsIndicatorStyle.Underlined -> TabsComponentTokens.underlinedHorizontalPadding.sdp
                            },
                            vertical = when (indicatorStyle) {
                                TabsIndicatorStyle.Pill -> TabsComponentTokens.pillVerticalPadding.sdp
                                TabsIndicatorStyle.Underlined -> TabsComponentTokens.underlinedVerticalPadding.sdp
                            }
                        )
                    )
                }
            }
        }
    }
}
