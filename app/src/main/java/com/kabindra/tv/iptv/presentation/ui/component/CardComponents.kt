package com.kabindra.tv.iptv.presentation.ui.component

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClassicCard
import androidx.tv.material3.CompactCard
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.WideCardContainer
import androidx.tv.material3.WideClassicCard

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
    width: Dp = 180.dp,
    imageWidth: Dp = 180.dp,
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
            onClick = onClick
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
            onClick = onClick
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
            onClick = onClick
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
            onClick = onClick
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
            onClick = onClick
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
        modifier = modifier.width(width),
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
                modifier = Modifier.padding(top = 8.dp)
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
        modifier = modifier.width(width),
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
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
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
                        top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp
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
        modifier = modifier.width(width),
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
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
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
                        top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp
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
        modifier = modifier,
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
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
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
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
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
        modifier = modifier,
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
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
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
                        top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp
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
                        top = 4.dp, start = 8.dp, end = 8.dp, bottom = 8.dp
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