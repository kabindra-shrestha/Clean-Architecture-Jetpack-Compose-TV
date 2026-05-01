package com.kabindra.tv.iptv.presentation.ui.screen.movie.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.tv.material3.MaterialTheme
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazy
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyLayout
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyOrientation
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyPlatform
import com.kabindra.tv.iptv.presentation.ui.component.ButtonComponent
import com.kabindra.tv.iptv.presentation.ui.component.CardImage
import com.kabindra.tv.iptv.presentation.ui.component.CardImageConfig
import com.kabindra.tv.iptv.presentation.ui.component.CardImageSource
import com.kabindra.tv.iptv.presentation.ui.component.LoadingIndicator
import com.kabindra.tv.iptv.presentation.ui.component.PosterCardComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextSize
import com.kabindra.tv.iptv.presentation.ui.component.TextType
import com.kabindra.tv.iptv.presentation.ui.component.TvLazyConfig
import com.kabindra.tv.iptv.presentation.ui.component.rememberBaseLazyState
import com.kabindra.tv.iptv.utils.extensions.mainBackground
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

private object MovieDetailScreenTokens {
    const val horizontalPadding = 32
    const val verticalPadding = 28
    const val infoColumnWidth = 340
    const val posterWidth = 180
    const val heroSpacing = 22
    const val buttonTopPadding = 18
    const val railTopPadding = 18
    const val railSpacing = 14
    const val railHorizontalPadding = 20
    const val railBottomPadding = 18
    const val railItemBottomPadding = 12
}

@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    movieId: String,
    onBack: () -> Unit,
    onNavigateMoviePlayer: (String) -> Unit,
    onNavigateMovieDetail: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val railState = rememberBaseLazyState()

    BackHandler(onBack = onBack)

    LaunchedEffect(movieId) {
        viewModel.getMovieDetail(movieId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        state.movie?.let { movie ->
            val recommendations = state.recommendedMovies.ifEmpty { movie.alsoWatch }

            CardImage(
                config = CardImageConfig(
                    source = CardImageSource.Url,
                    url = movie.backdropUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(16f / 9f)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(16f / 9f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.76f),
                                Color.Transparent
                            ),
                            start = Offset.Zero,
                            end = Offset(1220f, 0f)
                        )
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.sdp),
                contentPadding = PaddingValues(
                    horizontal = MovieDetailScreenTokens.horizontalPadding.sdp,
                    vertical = MovieDetailScreenTokens.verticalPadding.sdp
                )
            ) {
                // Hero Section
                item(key = "hero") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.width(MovieDetailScreenTokens.infoColumnWidth.sdp),
                            verticalArrangement = Arrangement.spacedBy(MovieDetailScreenTokens.heroSpacing.sdp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.sdp)) {
                                TextComponent(
                                    text = movie.title,
                                    type = TextType.Display,
                                    size = TextSize.Small,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                TextComponent(
                                    text = movie.subtitle,
                                    type = TextType.Body,
                                    size = TextSize.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                                )
                            }

                            TextComponent(
                                text = movie.description,
                                type = TextType.Body,
                                size = TextSize.Large,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                maxLines = 8
                            )

                            ButtonComponent(
                                modifier = Modifier.padding(top = MovieDetailScreenTokens.buttonTopPadding.sdp),
                                text = "Play Movie",
                                icon = Icons.Default.PlayArrow,
                                onClick = { onNavigateMoviePlayer(movie.id) }
                            )
                        }

                        CardImage(
                            config = CardImageConfig(
                                source = CardImageSource.Url,
                                url = movie.posterUrl,
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop
                            ),
                            modifier = Modifier
                                .width(MovieDetailScreenTokens.posterWidth.sdp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(10.sdp))
                        )
                    }
                }

                // Also Watch Section
                if (recommendations.isNotEmpty()) {
                    item(key = "also_watch") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(MovieDetailScreenTokens.railSpacing.sdp)
                        ) {
                            TextComponent(
                                text = "Also Watch",
                                type = TextType.Title,
                                size = TextSize.Large,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            BaseLazy(
                                items = recommendations,
                                state = railState,
                                modifier = Modifier.fillMaxWidth(),
                                layout = BaseLazyLayout.List,
                                orientation = BaseLazyOrientation.Horizontal,
                                platform = BaseLazyPlatform.AndroidTv,
                                tvConfig = TvLazyConfig(
                                    requestInitialFocus = false,
                                    initialFocusedIndex = 0,
                                    autoScrollOnFocus = true,
                                ),
                                contentPadding = PaddingValues(
                                    start = MovieDetailScreenTokens.railHorizontalPadding.sdp,
                                    top = MovieDetailScreenTokens.railTopPadding.sdp,
                                    end = MovieDetailScreenTokens.railHorizontalPadding.sdp,
                                    bottom = MovieDetailScreenTokens.railBottomPadding.sdp
                                ),
                                arrangement = Arrangement.spacedBy(MovieDetailScreenTokens.railSpacing.sdp),
                                userScrollEnabled = true,
                                key = { _, item -> item.id },
                                contentType = { _, _ -> "also_watch" }
                            ) { _, item, itemModifier ->
                                PosterCardComponent(
                                    modifier = itemModifier.padding(bottom = MovieDetailScreenTokens.railItemBottomPadding.sdp),
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    posterUrl = item.posterUrl,
                                    onClick = { onNavigateMovieDetail(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Loading State
        when {
            state.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    isCircular = true,
                    useExpressive = true
                )
            }

            state.errorMessage.isNotBlank() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.sdp)
                ) {
                    TextComponent(
                        text = state.errorMessage,
                        type = TextType.Body,
                        size = TextSize.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ButtonComponent(
                        text = "Back",
                        onClick = onBack
                    )
                }
            }
        }
    }
}