package com.kabindra.tv.iptv.presentation.ui.screen.movie.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay
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
    const val contentSpacing = 2
    const val railHorizontalPadding = 20
    const val railBottomPadding = 18
    const val railItemBottomPadding = 12
    const val playButtonFocusDelayMillis = 140L
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
    val railState = key(state.movie?.id) { rememberBaseLazyState() }
    val verticalState = rememberLazyListState()
    val playButtonFocusRequester = remember(state.movie?.id) { FocusRequester() }
    var isRecommendationFocused by remember(state.movie?.id) { mutableStateOf(false) }
    var highlightedMovieId by remember(state.movie?.id) { mutableStateOf<String?>(null) }

    BackHandler(onBack = onBack)

    LaunchedEffect(movieId) {
        viewModel.loadMovie(movieId)
    }

    LaunchedEffect(state.movie?.id) {
        if (state.movie == null) return@LaunchedEffect
        delay(MovieDetailScreenTokens.playButtonFocusDelayMillis)
        playButtonFocusRequester.requestFocus()
    }

    LaunchedEffect(isRecommendationFocused, state.movie?.id) {
        if (state.movie == null) return@LaunchedEffect
        delay(40)
        verticalState.animateScrollToItem(if (isRecommendationFocused) 1 else 0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        state.movie?.let { movie ->
            val recommendations = state.recommendedMovies.ifEmpty { movie.alsoWatch }
            val hasRailPreview = isRecommendationFocused || highlightedMovieId != null
            val backdropOffsetX by animateDpAsState(
                targetValue = if (hasRailPreview) (-14).sdp else 0.sdp,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "movie_detail_backdrop_offset",
            )
            val heroOffsetY by animateDpAsState(
                targetValue = if (hasRailPreview) (-16).sdp else 0.sdp,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "movie_detail_hero_offset",
            )
            val heroScale by animateFloatAsState(
                targetValue = if (hasRailPreview) 0.985f else 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "movie_detail_hero_scale",
            )
            val heroAlpha by animateFloatAsState(
                targetValue = if (hasRailPreview) 0.93f else 1f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                label = "movie_detail_hero_alpha",
            )
            val posterOffsetY by animateDpAsState(
                targetValue = if (hasRailPreview) (-10).sdp else 0.sdp,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "movie_detail_poster_offset",
            )
            val railLift by animateDpAsState(
                targetValue = if (hasRailPreview) (-8).sdp else 0.sdp,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                label = "movie_detail_rail_lift",
            )
            val backdropScale by animateFloatAsState(
                targetValue = if (hasRailPreview) 1.04f else 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                label = "movie_detail_backdrop_scale",
            )

            AnimatedContent(
                targetState = movie,
                transitionSpec = {
                    fadeIn(animationSpec = tween(240, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                },
                label = "movie_detail_content",
            ) { displayedMovie ->
                Box(modifier = Modifier.fillMaxSize()) {
                    CardImage(
                        config = CardImageConfig(
                            source = CardImageSource.Url,
                            url = displayedMovie.backdropUrl,
                            contentDescription = displayedMovie.title,
                            contentScale = ContentScale.Crop
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = backdropOffsetX)
                            .graphicsLayer(
                                scaleX = backdropScale,
                                scaleY = backdropScale,
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                        state = verticalState,
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(MovieDetailScreenTokens.contentSpacing.sdp),
                    ) {
                        item(key = "hero") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = MovieDetailScreenTokens.horizontalPadding.sdp,
                                        end = MovieDetailScreenTokens.horizontalPadding.sdp,
                                        top = MovieDetailScreenTokens.verticalPadding.sdp,
                                        bottom = MovieDetailScreenTokens.verticalPadding.sdp
                                    )
                                    .offset(y = heroOffsetY)
                                    .graphicsLayer(
                                        alpha = heroAlpha,
                                        scaleX = heroScale,
                                        scaleY = heroScale,
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.width(MovieDetailScreenTokens.infoColumnWidth.sdp),
                                    verticalArrangement = Arrangement.spacedBy(
                                        MovieDetailScreenTokens.heroSpacing.sdp
                                    )
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.sdp)
                                    ) {
                                        TextComponent(
                                            text = displayedMovie.title,
                                            type = TextType.Display,
                                            size = TextSize.Small,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        TextComponent(
                                            text = displayedMovie.subtitle,
                                            type = TextType.Body,
                                            size = TextSize.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                                        )
                                    }

                                    TextComponent(
                                        text = displayedMovie.description,
                                        type = TextType.Body,
                                        size = TextSize.Large,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                        maxLines = 8
                                    )

                                    ButtonComponent(
                                        modifier = Modifier
                                            .padding(top = MovieDetailScreenTokens.buttonTopPadding.sdp)
                                            .focusRequester(playButtonFocusRequester)
                                            .onFocusChanged {
                                                if (it.hasFocus && isRecommendationFocused) {
                                                    isRecommendationFocused = false
                                                    highlightedMovieId = null
                                                }
                                            },
                                        text = "Play Movie",
                                        icon = Icons.Default.PlayArrow,
                                        onClick = { onNavigateMoviePlayer(displayedMovie.id) }
                                    )
                                }

                                CardImage(
                                    config = CardImageConfig(
                                        source = CardImageSource.Url,
                                        url = displayedMovie.posterUrl,
                                        contentDescription = displayedMovie.title,
                                        contentScale = ContentScale.Crop
                                    ),
                                    modifier = Modifier
                                        .offset(y = posterOffsetY)
                                        .width(MovieDetailScreenTokens.posterWidth.sdp)
                                        .aspectRatio(2f / 3f)
                                        .shadow(
                                            elevation = 8.sdp,
                                            shape = RoundedCornerShape(10.sdp),
                                            clip = false
                                        )
                                        .clip(RoundedCornerShape(10.sdp))
                                )
                            }
                        }

                        if (recommendations.isNotEmpty()) {
                            item(key = "also_watch") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = railLift),
                                    verticalArrangement = Arrangement.spacedBy(
                                        MovieDetailScreenTokens.railSpacing.sdp
                                    )
                                ) {
                                    TextComponent(
                                        text = "Also Watch",
                                        type = TextType.Title,
                                        size = TextSize.Large,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = MovieDetailScreenTokens.horizontalPadding.sdp)
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
                                            focusedItemOffsetFraction = 0.14f,
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
                                            onFocused = {
                                                highlightedMovieId = item.id
                                                isRecommendationFocused = true
                                            },
                                            onClick = { onNavigateMovieDetail(item.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

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
