package com.kabindra.mobile.iptv.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import network.chaintech.sdpcomposemultiplatform.sdp

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
    val itemColors = ListItemDefaults.colors(
        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent,
        headlineColor = titleColor
            ?: (if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface),
        supportingColor = supportingTextColor
            ?: (if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant),
        leadingIconColor = iconTint
            ?: (if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    )

    ListItem(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick),
        colors = itemColors,
        headlineContent = {
            TextComponent(
                text = title,
                type = TextType.Body,
                size = if (type == ListItemType.Dense) TextSize.Small else TextSize.Medium,
                fontWeight = titleFontWeight,
                color = Color.Unspecified,
            )
        },
        supportingContent = supportingText?.let { text ->
            {
                TextComponent(
                    text = text,
                    type = TextType.Body,
                    size = TextSize.Small,
                    fontWeight = supportingTextFontWeight,
                    color = Color.Unspecified,
                    modifier = Modifier.padding(top = ListItemComponentTokens.supportingTopPadding.sdp)
                )
            }
        },
        leadingContent = leadingContent(
            title = title,
            icon = leadingIcon,
            leadingImageConfig = leadingImageConfig,
            iconSize = if (type == ListItemType.Dense) ListItemComponentTokens.denseIconSize else ListItemComponentTokens.defaultIconSize,
            imageWidth = if (type == ListItemType.Dense) ListItemComponentTokens.denseImageWidth else ListItemComponentTokens.defaultImageWidth,
            imageHeight = if (type == ListItemType.Dense) ListItemComponentTokens.denseImageHeight else ListItemComponentTokens.defaultImageHeight,
            iconTint = iconTint
        ),
    )
}

private fun leadingContent(
    title: String,
    icon: ImageVector?,
    leadingImageConfig: CardImageConfig?,
    iconSize: Int,
    imageWidth: Int,
    imageHeight: Int,
    iconTint: Color?,
): (@Composable () -> Unit)? {
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
