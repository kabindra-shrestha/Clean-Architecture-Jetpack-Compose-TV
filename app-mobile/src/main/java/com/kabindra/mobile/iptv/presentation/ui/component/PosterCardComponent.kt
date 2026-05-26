package com.kabindra.mobile.iptv.presentation.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import network.chaintech.sdpcomposemultiplatform.sdp

private object PosterCardTokens {
    const val width = 112
    const val titleTopPadding = 5
    const val subtitleTopPadding = 2
    const val contentHorizontalPadding = 5
    const val contentBottomPadding = 5
}

@Composable
fun PosterCardComponent(
    title: String,
    posterUrl: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    width: Int = PosterCardTokens.width,
    enabled: Boolean = true,
    onFocused: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = if (enabled) onClick else ({ }),
        modifier = modifier
            .width(width.sdp)
            .alpha(if (enabled) 1f else 0.6f)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocused?.invoke()
                }
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column {
            CardImage(
                config = CardImageConfig(
                    source = CardImageSource.Url,
                    url = posterUrl,
                    contentDescription = title
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
            TextComponent(
                text = title,
                type = TextType.Body,
                size = TextSize.Medium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                modifier = Modifier.padding(
                    top = PosterCardTokens.titleTopPadding.sdp,
                    start = PosterCardTokens.contentHorizontalPadding.sdp,
                    end = PosterCardTokens.contentHorizontalPadding.sdp
                )
            )
            subtitle?.let { value ->
                TextComponent(
                    text = value,
                    type = TextType.Body,
                    size = TextSize.Small,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.padding(
                        top = PosterCardTokens.subtitleTopPadding.sdp,
                        start = PosterCardTokens.contentHorizontalPadding.sdp,
                        end = PosterCardTokens.contentHorizontalPadding.sdp,
                        bottom = PosterCardTokens.contentBottomPadding.sdp
                    )
                )
            }
        }
    }
}
