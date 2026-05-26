package com.kabindra.mobile.iptv.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

// ─────────────────────────────────────────────
// Enums
// ─────────────────────────────────────────────

enum class ButtonType { Filled, Outlined }
enum class ButtonSize { Small, Medium, Large }

// ─────────────────────────────────────────────
// ButtonComponent — Filled / Outlined
//
// Usage:
//   ButtonComponent(text = "Play")
//   ButtonComponent(text = "Favourite", icon = Icons.Default.Favorite, type = ButtonType.Outlined)
// ─────────────────────────────────────────────

@Composable
fun ButtonComponent(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    type: ButtonType = ButtonType.Filled,
    textColor: Color? = null,
    fontWeight: FontWeight = FontWeight.Medium,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val hasIcon = icon != null

    when (type) {
        ButtonType.Filled -> {
            Button(
                modifier = modifier,
                onClick = onClick,
                enabled = enabled,
                contentPadding = if (hasIcon)
                    ButtonDefaults.ButtonWithIconContentPadding
                else
                    ButtonDefaults.ContentPadding
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    iconSize = ButtonDefaults.IconSize,
                    iconSpacing = ButtonDefaults.IconSpacing,
                    textColor = textColor,
                    fontWeight = fontWeight
                )
            }
        }

        ButtonType.Outlined -> {
            OutlinedButton(
                modifier = modifier,
                onClick = onClick,
                enabled = enabled,
                contentPadding = if (hasIcon)
                    ButtonDefaults.ButtonWithIconContentPadding
                else
                    ButtonDefaults.ContentPadding
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    iconSize = ButtonDefaults.IconSize,
                    iconSpacing = ButtonDefaults.IconSpacing,
                    textColor = textColor,
                    fontWeight = fontWeight
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// IconButtonComponent — Filled / Outlined, S/M/L
//
// Usage:
//   IconButtonComponent(icon = Icons.Default.Favorite)
//   IconButtonComponent(icon = Icons.Default.Settings, type = ButtonType.Outlined, size = ButtonSize.Large)
// ─────────────────────────────────────────────

@Composable
fun IconButtonComponent(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String = "",
    type: ButtonType = ButtonType.Filled,
    size: ButtonSize = ButtonSize.Medium,
    iconTint: Color? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    when (type) {
        ButtonType.Filled -> {
            val (buttonSize, iconSize) = resolveIconButtonSize(
                size,
                outlined = false
            )
            IconButton(
                modifier = modifier.size(buttonSize),
                onClick = onClick,
                enabled = enabled
            ) {
                ImageHandlerVector(
                    modifier = Modifier.size(iconSize),
                    image = icon,
                    contentDescription = contentDescription,
                    tint = iconTint
                )
            }
        }

        ButtonType.Outlined -> {
            val (buttonSize, iconSize) = resolveIconButtonSize(
                size,
                outlined = true
            )
            OutlinedIconButton(
                modifier = modifier.size(buttonSize),
                onClick = onClick,
                enabled = enabled
            ) {
                ImageHandlerVector(
                    modifier = Modifier.size(iconSize),
                    image = icon,
                    contentDescription = contentDescription,
                    tint = iconTint
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Internal helpers
// ─────────────────────────────────────────────

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    iconSize: androidx.compose.ui.unit.Dp,
    iconSpacing: androidx.compose.ui.unit.Dp,
    textColor: Color?,
    fontWeight: FontWeight
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            ImageHandlerVector(
                modifier = Modifier.size(iconSize),
                image = icon,
                tint = textColor
            )
            Spacer(Modifier.size(iconSpacing))
        }
        TextComponent(
            text = text,
            type = TextType.Label,
            size = TextSize.Large,
            fontWeight = fontWeight,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun resolveIconButtonSize(
    size: ButtonSize,
    outlined: Boolean
): Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp> {
    return if (outlined) {
        when (size) {
            ButtonSize.Small -> IconButtonDefaults.smallIconSize to IconButtonDefaults.smallIconSize
            ButtonSize.Medium -> IconButtonDefaults.mediumIconSize to IconButtonDefaults.mediumIconSize
            ButtonSize.Large -> IconButtonDefaults.largeIconSize to IconButtonDefaults.largeIconSize
        }
    } else {
        when (size) {
            ButtonSize.Small -> IconButtonDefaults.smallIconSize to IconButtonDefaults.smallIconSize
            ButtonSize.Medium -> IconButtonDefaults.mediumIconSize to IconButtonDefaults.mediumIconSize
            ButtonSize.Large -> IconButtonDefaults.largeIconSize to IconButtonDefaults.largeIconSize
        }
    }
}
