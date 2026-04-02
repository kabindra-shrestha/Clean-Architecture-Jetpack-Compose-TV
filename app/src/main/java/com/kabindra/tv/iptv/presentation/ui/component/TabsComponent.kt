package com.kabindra.tv.iptv.presentation.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowDefaults
import network.chaintech.sdpcomposemultiplatform.sdp

private object TabsComponentTokens {
    const val horizontalPadding = 14
    const val verticalPadding = 6
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TabsComponent(
    tabs: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelectedIndexChange: (Int) -> Unit,
) {
    if (tabs.isEmpty()) return

    val clampedSelectedIndex = selectedIndex.coerceIn(0, tabs.lastIndex)

    TabRow(
        selectedTabIndex = clampedSelectedIndex,
        modifier = modifier.focusRestorer(),
        indicator = { tabPositions, doesTabRowHaveFocus ->
            tabPositions.getOrNull(clampedSelectedIndex)?.let { currentTabPosition ->
                TabRowDefaults.PillIndicator(
                    currentTabPosition = currentTabPosition,
                    doesTabRowHaveFocus = doesTabRowHaveFocus
                )
            }
        }
    ) {
        tabs.forEachIndexed { index, title ->
            key(title, index) {
                Tab(
                    selected = clampedSelectedIndex == index,
                    onFocus = { onSelectedIndexChange(index) },
                    onClick = { onSelectedIndexChange(index) }
                ) {
                    TextComponent(
                        text = title,
                        type = TextType.Title,
                        size = TextSize.Medium,
                        modifier = Modifier.padding(
                            horizontal = TabsComponentTokens.horizontalPadding.sdp,
                            vertical = TabsComponentTokens.verticalPadding.sdp
                        )
                    )
                }
            }
        }
    }
}
