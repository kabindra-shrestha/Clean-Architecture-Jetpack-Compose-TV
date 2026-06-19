package com.kabindra.mobile.iptv.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────
// Enums
// ─────────────────────────────────────────────

enum class ButtonType { Filled, Outlined }
enum class ButtonSize { Small, Medium, Large }

// ─────────────────────────────────────────────
// ButtonComponent — Filled / Outlined
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
    when (type) {
        ButtonType.Filled -> {
            Button(
                modifier = modifier,
                onClick = onClick,
                enabled = enabled,
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    iconSize = 18.dp,
                    iconSpacing = 8.dp,
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
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    iconSize = 18.dp,
                    iconSpacing = 8.dp,
                    textColor = textColor,
                    fontWeight = fontWeight
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// IconButtonComponent — Filled / Outlined, S/M/L
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
    val (buttonSize, iconSize) = resolveIconButtonSize(size)
    
    when (type) {
        ButtonType.Filled -> {
            FilledIconButton(
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
// WideButtonComponent — Mobile version
// ─────────────────────────────────────────────

@Composable
fun WideButtonComponent(
    modifier: Modifier = Modifier,
    text: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconContentDescription: String = "",
    iconTint: Color? = null,
    textColor: Color? = null,
    subtitleColor: Color? = null,
    textFontWeight: FontWeight = FontWeight.SemiBold,
    subtitleFontWeight: FontWeight = FontWeight.Normal,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = ButtonDefaults.shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                ImageHandlerVector(
                    modifier = Modifier.size(24.dp),
                    image = icon,
                    contentDescription = iconContentDescription,
                    tint = iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                TextComponent(
                    text = text,
                    type = TextType.Title,
                    size = TextSize.Medium,
                    fontWeight = textFontWeight,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    TextComponent(
                        text = subtitle,
                        type = TextType.Body,
                        size = TextSize.Small,
                        fontWeight = subtitleFontWeight,
                        color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
    iconSize: Dp,
    iconSpacing: Dp,
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
    size: ButtonSize
): Pair<Dp, Dp> {
    return when (size) {
        ButtonSize.Small -> 32.dp to 18.dp
        ButtonSize.Medium -> 40.dp to 24.dp
        ButtonSize.Large -> 48.dp to 32.dp
    }
}
