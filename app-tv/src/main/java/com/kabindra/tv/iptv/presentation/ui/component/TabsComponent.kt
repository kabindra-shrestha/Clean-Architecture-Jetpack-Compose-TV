package com.kabindra.tv.iptv.presentation.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.tv.material3.Tab
import androidx.tv.material3.TabDefaults
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowDefaults
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

@OptIn(ExperimentalComposeUiApi::class)
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
        modifier = modifier.focusRestorer(),
        separator = {
            if (indicatorStyle == TabsIndicatorStyle.Underlined) {
                Spacer(modifier = Modifier.width(TabsComponentTokens.underlinedSeparatorSpacing.sdp))
            }
        },
        indicator = { tabPositions, doesTabRowHaveFocus ->
            tabPositions.getOrNull(clampedSelectedIndex)?.let { currentTabPosition ->
                when (indicatorStyle) {
                    TabsIndicatorStyle.Pill -> {
                        TabRowDefaults.PillIndicator(
                            currentTabPosition = currentTabPosition,
                            doesTabRowHaveFocus = doesTabRowHaveFocus
                        )
                    }

                    TabsIndicatorStyle.Underlined -> {
                        TabRowDefaults.UnderlinedIndicator(
                            currentTabPosition = currentTabPosition,
                            doesTabRowHaveFocus = doesTabRowHaveFocus
                        )
                    }
                }
            }
        }
    ) {
        tabs.forEachIndexed { index, title ->
            key(title, index) {
                Tab(
                    selected = clampedSelectedIndex == index,
                    onFocus = { onSelectedIndexChange(index) },
                    onClick = { onSelectedIndexChange(index) },
                    colors = when (indicatorStyle) {
                        TabsIndicatorStyle.Pill -> TabDefaults.pillIndicatorTabColors()
                        TabsIndicatorStyle.Underlined -> TabDefaults.underlinedIndicatorTabColors()
                    }
                ) {
                    TextComponent(
                        text = title,
                        type = TextType.Title,
                        size = TextSize.Medium,
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
