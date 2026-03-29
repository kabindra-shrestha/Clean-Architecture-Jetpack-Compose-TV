package com.kabindra.tv.iptv.presentation.ui.component

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClassicCard
import androidx.tv.material3.CompactCard
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.WideCardContainer
import androidx.tv.material3.WideClassicCard
import network.chaintech.sdpcomposemultiplatform.sdp

// ─────────────────────────────────────────────
// Enums
// ─────────────────────────────────────────────

enum class CardType { Standard, Classic, Compact, WideStandard, WideClassic }

enum class CardImageSource { Url, Resource, Vector }

// ─────────────────────────────────────────────
// CardImageConfig — unified image config
// ─────────────────────────────────────────────

data class CardImageConfig(
    val source: CardImageSource = CardImageSource.Url,
    val url: String = "",
    val resId: Int? = null,
    val vector: ImageVector? = null,
    val contentDescription: String = "",
    val contentScale: ContentScale = ContentScale.Crop,
    val placeholder: ImageVector? = null,
    val tint: Color? = null
)

private object CardComponentTokens {
    const val defaultWidth = 108
    const val imageWidth = 108
    const val compactPadding = 5
    const val compactTopPadding = 2
    const val widePadding = 10
    const val wideTopPadding = 2
}

// ─────────────────────────────────────────────
// CardComponent
//
// Usage:
//   CardComponent(
//       type = CardType.Classic,
//       title = "Breaking Bad",
//       subtitle = "Drama · 2008",
//       imageConfig = CardImageConfig(url = "https://..."),
//       onClick = { }
//   )
//
//   CardComponent(
//       type = CardType.WideClassic,
//       title = "Inception",
//       subtitle = "Sci-Fi · 2010",
//       description = "A thief who steals corporate secrets...",
//       imageConfig = CardImageConfig(url = "https://..."),
//       width = 320.dp,
//       imageWidth = 180.dp
//   )
// ─────────────────────────────────────────────

@Composable
fun CardComponent(
    modifier: Modifier = Modifier,
    type: CardType = CardType.Classic,
    title: String,
    subtitle: String? = null,
    description: String? = null,
    imageConfig: CardImageConfig = CardImageConfig(),
    width: Dp = CardComponentTokens.defaultWidth.sdp,
    imageWidth: Dp = CardComponentTokens.imageWidth.sdp,
    aspectRatio: Float = CardDefaults.HorizontalImageAspectRatio,
    titleColor: Color? = null,
    subtitleColor: Color? = null,
    descriptionColor: Color? = null,
    titleFontWeight: FontWeight = FontWeight.Medium,
    subtitleFontWeight: FontWeight = FontWeight.Normal,
    descriptionFontWeight: FontWeight = FontWeight.Normal,
    containerColor: Color = Color.Transparent,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val cardOnClick = if (enabled) onClick else ({})

    when (type) {
        CardType.Standard -> StandardCard(
            modifier = modifier,
            title = title,
            subtitle = subtitle,
            imageConfig = imageConfig,
            width = width,
            aspectRatio = aspectRatio,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            titleFontWeight = titleFontWeight,
            subtitleFontWeight = subtitleFontWeight,
            containerColor = containerColor,
            enabled = enabled,
            onClick = cardOnClick
        )

        CardType.Classic -> ClassicCard(
            modifier = modifier,
            title = title,
            subtitle = subtitle,
            imageConfig = imageConfig,
            width = width,
            aspectRatio = aspectRatio,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            titleFontWeight = titleFontWeight,
            subtitleFontWeight = subtitleFontWeight,
            enabled = enabled,
            onClick = cardOnClick
        )

        CardType.Compact -> CompactCard(
            modifier = modifier,
            title = title,
            subtitle = subtitle,
            imageConfig = imageConfig,
            width = width,
            aspectRatio = aspectRatio,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            titleFontWeight = titleFontWeight,
            subtitleFontWeight = subtitleFontWeight,
            enabled = enabled,
            onClick = cardOnClick
        )

        CardType.WideStandard -> WideStandardCard(
            modifier = modifier,
            title = title,
            subtitle = subtitle,
            imageConfig = imageConfig,
            imageWidth = imageWidth,
            aspectRatio = aspectRatio,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            titleFontWeight = titleFontWeight,
            subtitleFontWeight = subtitleFontWeight,
            containerColor = containerColor,
            enabled = enabled,
            onClick = cardOnClick
        )

        CardType.WideClassic -> WideClassicCard(
            modifier = modifier,
            title = title,
            subtitle = subtitle,
            description = description,
            imageConfig = imageConfig,
            imageWidth = imageWidth,
            aspectRatio = aspectRatio,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            descriptionColor = descriptionColor,
            titleFontWeight = titleFontWeight,
            subtitleFontWeight = subtitleFontWeight,
            descriptionFontWeight = descriptionFontWeight,
            enabled = enabled,
            onClick = cardOnClick
        )
    }
}

// ─────────────────────────────────────────────
// Internal card implementations
// ─────────────────────────────────────────────

@Composable
private fun StandardCard(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    imageConfig: CardImageConfig,
    width: Dp,
    aspectRatio: Float,
    titleColor: Color?,
    subtitleColor: Color?,
    titleFontWeight: FontWeight,
    subtitleFontWeight: FontWeight,
    containerColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    StandardCardContainer(
        modifier = modifier
            .width(width)
            .alpha(if (enabled) 1f else 0.6f),
        imageCard = { interactionSource ->
            Card(
                onClick = onClick,
                interactionSource = interactionSource,
                colors = CardDefaults.colors(containerColor = containerColor)
            ) {
                CardImage(
                    config = imageConfig,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                )
            }
        },
        title = {
            TextComponent(
                text = title,
                type = TextType.Body,
                size = TextSize.Medium,
                fontWeight = titleFontWeight,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = CardComponentTokens.compactPadding.sdp)
            )
        },
        subtitle = {
            subtitle?.let {
                TextComponent(
                    text = it,
                    type = TextType.Body,
                    size = TextSize.Small,
                    fontWeight = subtitleFontWeight,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}

@Composable
private fun ClassicCard(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    imageConfig: CardImageConfig,
    width: Dp,
    aspectRatio: Float,
    titleColor: Color?,
    subtitleColor: Color?,
    titleFontWeight: FontWeight,
    subtitleFontWeight: FontWeight,
    enabled: Boolean,
    onClick: () -> Unit
) {
    ClassicCard(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .alpha(if (enabled) 1f else 0.6f),
        image = {
            CardImage(
                config = imageConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
            )
        },
        title = {
            TextComponent(
                text = title,
                type = TextType.Body,
                size = TextSize.Medium,
                fontWeight = titleFontWeight,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    top = CardComponentTokens.compactPadding.sdp,
                    start = CardComponentTokens.compactPadding.sdp,
                    end = CardComponentTokens.compactPadding.sdp
                )
            )
        },
        subtitle = {
            subtitle?.let {
                TextComponent(
                    text = it,
                    type = TextType.Body,
                    size = TextSize.Small,
                    fontWeight = subtitleFontWeight,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        top = CardComponentTokens.compactTopPadding.sdp,
                        start = CardComponentTokens.compactPadding.sdp,
                        end = CardComponentTokens.compactPadding.sdp,
                        bottom = CardComponentTokens.compactPadding.sdp
                    )
                )
            }
        }
    )
}

@Composable
private fun CompactCard(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    imageConfig: CardImageConfig,
    width: Dp,
    aspectRatio: Float,
    titleColor: Color?,
    subtitleColor: Color?,
    titleFontWeight: FontWeight,
    subtitleFontWeight: FontWeight,
    enabled: Boolean,
    onClick: () -> Unit
) {
    CompactCard(
        onClick = onClick,
        modifier = modifier
            .width(width)
            .alpha(if (enabled) 1f else 0.6f),
        image = {
            CardImage(
                config = imageConfig,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
            )
        },
        title = {
            TextComponent(
                text = title,
                type = TextType.Body,
                size = TextSize.Medium,
                fontWeight = titleFontWeight,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    top = CardComponentTokens.compactPadding.sdp,
                    start = CardComponentTokens.compactPadding.sdp,
                    end = CardComponentTokens.compactPadding.sdp
                )
            )
        },
        subtitle = {
            subtitle?.let {
                TextComponent(
                    text = it,
                    type = TextType.Body,
                    size = TextSize.Small,
                    fontWeight = subtitleFontWeight,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        top = CardComponentTokens.compactTopPadding.sdp,
                        start = CardComponentTokens.compactPadding.sdp,
                        end = CardComponentTokens.compactPadding.sdp,
                        bottom = CardComponentTokens.compactPadding.sdp
                    )
                )
            }
        }
    )
}

@Composable
private fun WideStandardCard(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    imageConfig: CardImageConfig,
    imageWidth: Dp,
    aspectRatio: Float,
    titleColor: Color?,
    subtitleColor: Color?,
    titleFontWeight: FontWeight,
    subtitleFontWeight: FontWeight,
    containerColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    WideCardContainer(
        modifier = modifier.alpha(if (enabled) 1f else 0.6f),
        imageCard = { interactionSource ->
            Card(
                onClick = onClick,
                interactionSource = interactionSource,
                colors = CardDefaults.colors(containerColor = containerColor)
            ) {
                CardImage(
                    config = imageConfig,
                    modifier = Modifier
                        .width(imageWidth)
                        .aspectRatio(aspectRatio)
                )
            }
        },
        title = {
            TextComponent(
                text = title,
                type = TextType.Title,
                size = TextSize.Medium,
                fontWeight = titleFontWeight,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    start = CardComponentTokens.widePadding.sdp,
                    top = CardComponentTokens.widePadding.sdp
                )
            )
        },
        subtitle = {
            subtitle?.let {
                TextComponent(
                    text = it,
                    type = TextType.Body,
                    size = TextSize.Small,
                    fontWeight = subtitleFontWeight,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        start = CardComponentTokens.widePadding.sdp,
                        top = CardComponentTokens.wideTopPadding.sdp
                    )
                )
            }
        }
    )
}

@Composable
private fun WideClassicCard(
    modifier: Modifier,
    title: String,
    subtitle: String?,
    description: String?,
    imageConfig: CardImageConfig,
    imageWidth: Dp,
    aspectRatio: Float,
    titleColor: Color?,
    subtitleColor: Color?,
    descriptionColor: Color?,
    titleFontWeight: FontWeight,
    subtitleFontWeight: FontWeight,
    descriptionFontWeight: FontWeight,
    enabled: Boolean,
    onClick: () -> Unit
) {
    WideClassicCard(
        onClick = onClick,
        modifier = modifier.alpha(if (enabled) 1f else 0.6f),
        image = {
            CardImage(
                config = imageConfig,
                modifier = Modifier
                    .width(imageWidth)
                    .aspectRatio(aspectRatio)
            )
        },
        title = {
            TextComponent(
                text = title,
                type = TextType.Title,
                size = TextSize.Medium,
                fontWeight = titleFontWeight,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    top = CardComponentTokens.compactPadding.sdp,
                    start = CardComponentTokens.compactPadding.sdp,
                    end = CardComponentTokens.compactPadding.sdp
                )
            )
        },
        subtitle = {
            subtitle?.let {
                TextComponent(
                    text = it,
                    type = TextType.Body,
                    size = TextSize.Small,
                    fontWeight = subtitleFontWeight,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        top = CardComponentTokens.compactTopPadding.sdp,
                        start = CardComponentTokens.compactPadding.sdp,
                        end = CardComponentTokens.compactPadding.sdp,
                        bottom = CardComponentTokens.compactPadding.sdp
                    )
                )
            }
        },
        description = {
            description?.let {
                TextComponent(
                    text = it,
                    type = TextType.Body,
                    size = TextSize.Small,
                    fontWeight = descriptionFontWeight,
                    color = descriptionColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(
                        top = CardComponentTokens.compactTopPadding.sdp,
                        start = CardComponentTokens.compactPadding.sdp,
                        end = CardComponentTokens.compactPadding.sdp,
                        bottom = CardComponentTokens.compactPadding.sdp
                    )
                )
            }
        }
    )
}

// ─────────────────────────────────────────────
// CardImage — routes to the right ImageHandler
// ─────────────────────────────────────────────

@Composable
private fun CardImage(
    config: CardImageConfig,
    modifier: Modifier
) {
    when (config.source) {
        CardImageSource.Url -> ImageHandlerURL(
            modifier = modifier,
            image = config.url,
            contentDescription = config.contentDescription,
            contentScale = config.contentScale,
            placeholder = config.placeholder
                ?: Icons.Default.Image,
            tint = config.tint
        )

        CardImageSource.Resource -> ImageHandlerRes(
            modifier = modifier,
            image = config.resId
                ?: com.kabindra.tv.iptv.R.drawable.splash_icon,
            contentDescription = config.contentDescription
        )

        CardImageSource.Vector -> ImageHandlerVector(
            modifier = modifier,
            image = config.vector,
            contentDescription = config.contentDescription,
            tint = config.tint
        )
    }
}

// Usage Example
// URL image, Classic card
/*CardComponent(
    type = CardType.Classic,
    title = "Breaking Bad",
    subtitle = "Drama · 2008",
    imageConfig = CardImageConfig(
    source = CardImageSource.Url,
        url = "https://example.com/poster.jpg"
    ),
    onClick = { }
)*/

// Drawable resource, Standard card
/*CardComponent(
    type = CardType.Standard,
    title = "Inception",
    imageConfig = CardImageConfig(
        source = CardImageSource.Resource,
        resId = R.drawable.poster_inception
    )
)*/

// Wide classic with description
/*CardComponent(
    type = CardType.WideClassic,
    title = "Planet Earth",
    subtitle = "Documentary · 2006",
    description = "An exploration of exotic creatures and their habitats...",
    imageConfig = CardImageConfig(url = "https://..."),
    imageWidth = 180.dp
)*/
