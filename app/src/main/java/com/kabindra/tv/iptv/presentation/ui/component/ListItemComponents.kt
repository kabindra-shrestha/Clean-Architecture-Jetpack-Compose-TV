package com.kabindra.tv.iptv.presentation.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.DenseListItem
import network.chaintech.sdpcomposemultiplatform.sdp
import androidx.tv.material3.ListItem as TvListItem

enum class ListItemType { Default, Dense }

private object ListItemComponentTokens {
    const val defaultIconSize = 18
    const val denseIconSize = 16
    const val supportingTopPadding = 2
}

@Composable
fun ListItemComponent(
    modifier: Modifier = Modifier,
    type: ListItemType = ListItemType.Default,
    title: String,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    titleColor: Color? = null,
    supportingTextColor: Color? = null,
    iconTint: Color? = null,
    titleFontWeight: FontWeight = FontWeight.Medium,
    supportingTextFontWeight: FontWeight = FontWeight.Normal,
    onClick: () -> Unit = {},
) {
    when (type) {
        ListItemType.Default -> TvListItem(
            modifier = modifier,
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            headlineContent = {
                TextComponent(
                    text = title,
                    type = TextType.Body,
                    size = TextSize.Medium,
                    fontWeight = titleFontWeight,
                    color = titleColor,
                )
            },
            supportingContent = supportingText?.let { text ->
                {
                    TextComponent(
                        text = text,
                        type = TextType.Body,
                        size = TextSize.Small,
                        fontWeight = supportingTextFontWeight,
                        color = supportingTextColor,
                        modifier = Modifier.padding(top = ListItemComponentTokens.supportingTopPadding.sdp)
                    )
                }
            },
            leadingContent = leadingIcon?.let { icon ->
                {
                    ImageHandlerVector(
                        modifier = Modifier.size(ListItemComponentTokens.defaultIconSize.sdp),
                        image = icon,
                        contentDescription = title,
                        tint = iconTint
                    )
                }
            },
        )

        ListItemType.Dense -> DenseListItem(
            modifier = modifier,
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            headlineContent = {
                TextComponent(
                    text = title,
                    type = TextType.Body,
                    size = TextSize.Medium,
                    fontWeight = titleFontWeight,
                    color = titleColor,
                )
            },
            supportingContent = supportingText?.let { text ->
                {
                    TextComponent(
                        text = text,
                        type = TextType.Body,
                        size = TextSize.Small,
                        fontWeight = supportingTextFontWeight,
                        color = supportingTextColor,
                        modifier = Modifier.padding(top = ListItemComponentTokens.supportingTopPadding.sdp)
                    )
                }
            },
            leadingContent = leadingIcon?.let { icon ->
                {
                    ImageHandlerVector(
                        modifier = Modifier.size(ListItemComponentTokens.denseIconSize.sdp),
                        image = icon,
                        contentDescription = title,
                        tint = iconTint
                    )
                }
            },
        )
    }
}
