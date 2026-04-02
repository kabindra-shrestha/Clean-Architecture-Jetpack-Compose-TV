package com.kabindra.tv.iptv.presentation.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.DenseListItem
import network.chaintech.sdpcomposemultiplatform.sdp
import androidx.tv.material3.ListItem as TvListItem

enum class ListItemType { Default, Dense }

private object ListItemComponentTokens {
    const val defaultIconSize = 18
    const val denseIconSize = 16
    const val defaultImageWidth = 24
    const val defaultImageHeight = 16
    const val denseImageWidth = 22
    const val denseImageHeight = 14
    const val supportingTopPadding = 2
}

@Composable
fun ListItemComponent(
    modifier: Modifier = Modifier,
    type: ListItemType = ListItemType.Default,
    title: String,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    leadingImageConfig: CardImageConfig? = null,
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
            leadingContent = leadingContent(
                title = title,
                icon = leadingIcon,
                leadingImageConfig = leadingImageConfig,
                iconSize = ListItemComponentTokens.defaultIconSize,
                imageWidth = ListItemComponentTokens.defaultImageWidth,
                imageHeight = ListItemComponentTokens.defaultImageHeight,
                iconTint = iconTint
            ),
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
            leadingContent = leadingContent(
                title = title,
                icon = leadingIcon,
                leadingImageConfig = leadingImageConfig,
                iconSize = ListItemComponentTokens.denseIconSize,
                imageWidth = ListItemComponentTokens.denseImageWidth,
                imageHeight = ListItemComponentTokens.denseImageHeight,
                iconTint = iconTint
            ),
        )
    }
}

private fun leadingContent(
    title: String,
    icon: ImageVector?,
    leadingImageConfig: CardImageConfig?,
    iconSize: Int,
    imageWidth: Int,
    imageHeight: Int,
    iconTint: Color?,
): (@Composable BoxScope.() -> Unit)? {
    return when {
        leadingImageConfig != null -> {
            {
                CardImage(
                    config = leadingImageConfig.copy(
                        contentDescription = leadingImageConfig.contentDescription.ifBlank { title },
                        contentScale = if (leadingImageConfig.contentScale == ContentScale.Crop) {
                            ContentScale.Fit
                        } else {
                            leadingImageConfig.contentScale
                        }
                    ),
                    modifier = Modifier.size(
                        width = imageWidth.sdp,
                        height = imageHeight.sdp
                    )
                )
            }
        }

        icon != null -> {
            {
                ImageHandlerVector(
                    modifier = Modifier.size(iconSize.sdp),
                    image = icon,
                    contentDescription = title,
                    tint = iconTint
                )
            }
        }

        else -> null
    }
}
