package com.kabindra.mobile.iptv.presentation.ui.screen.movie.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileAdaptiveContent
import com.kabindra.mobile.iptv.presentation.ui.adaptive.plus
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileErrorState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileLoadingState
import com.kabindra.mobile.iptv.utils.extensions.mainBackground
import com.kabindra.player.PlayerCallbacks
import com.kabindra.player.PlayerContentType
import com.kabindra.player.PlayerControllerMode
import com.kabindra.player.PlayerExperience
import com.kabindra.player.PlayerFeatures
import com.kabindra.player.PlayerItem
import com.kabindra.player.PlayerPlaylist
import com.kabindra.player.PlayerSourceType
import com.kabindra.player.PlayerSubtitleTrack
import com.kabindra.player.UnifiedPlayer
import com.kabindra.player.defaultPlayerInteractionConfig
import com.kabindra.player.rememberPlayerHostState
import com.kabindra.tv.iptv.domain.entity.MediaPlaybackType
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.domain.entity.VODDetail
import com.kabindra.tv.iptv.presentation.ui.screen.movie.player.MoviePlayerViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MoviePlayerScreen(
    viewModel: MoviePlayerViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    movieId: String,
    onBack: () -> Unit,
    onImmersiveChanged: (Boolean) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val playerHostState = rememberPlayerHostState(movieId)
    val interactionConfig = remember { defaultPlayerInteractionConfig(PlayerExperience.AndroidOtt) }
    var isMinimized by remember { mutableStateOf(false) }
    val playerAlpha by animateFloatAsState(
        targetValue = if (state.movie != null) 1f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "movie_player_alpha",
    )

    LaunchedEffect(movieId) {
        viewModel.getMovieDetail(movieId)
    }

    LaunchedEffect(isMinimized) {
        onImmersiveChanged(!isMinimized)
    }

    BackHandler {
        when {
            isMinimized -> isMinimized = false
            playerHostState.consumeBackPress() -> Unit
            else -> onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isMinimized) Modifier.mainBackground() else Modifier.background(Color.Black))
    ) {
        state.movie?.let { movie ->
            if (isMinimized) {
                MobileAdaptiveContent {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding.plus(
                                    horizontal = it.horizontalPadding,
                                    vertical = it.verticalPadding,
                                )
                            ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "Now playing",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        MoviePlayerSurface(
                            movie = movie,
                            playerHostState = playerHostState,
                            interactionConfig = interactionConfig,
                            alpha = playerAlpha,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            onBack = onBack,
                            onMinimizeToggle = { isMinimized = false },
                            isMinimized = true,
                        )
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = movie.description.ifBlank { movie.subtitle },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Button(onClick = { isMinimized = false }) {
                            Icon(Icons.Default.OpenInFull, contentDescription = null)
                            Text("Return to fullscreen", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            } else {
                MoviePlayerSurface(
                    movie = movie,
                    playerHostState = playerHostState,
                    interactionConfig = interactionConfig,
                    alpha = playerAlpha,
                    modifier = Modifier.fillMaxSize(),
                    onBack = onBack,
                    onMinimizeToggle = { isMinimized = true },
                    isMinimized = false,
                )
            }
        }

        when {
            state.isLoading -> {
                MobileLoadingState(message = "Loading player...")
            }

            state.errorMessage.isNotBlank() -> {
                MobileErrorState(
                    message = state.errorMessage,
                    actionLabel = "Back",
                    onActionClick = onBack,
                )
            }
        }
    }
}

@Composable
private fun MoviePlayerSurface(
    movie: VODDetail,
    playerHostState: com.kabindra.player.PlayerHostState,
    interactionConfig: com.kabindra.player.PlayerInteractionConfig,
    alpha: Float,
    onBack: () -> Unit,
    onMinimizeToggle: () -> Unit,
    isMinimized: Boolean,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        shape = if (isMinimized) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            UnifiedPlayer(
                playlist = PlayerPlaylist(
                    items = listOf(movie.toPlayerItem()),
                    startIndex = 0,
                    autoPlay = true,
                ),
                hostState = playerHostState,
                experience = PlayerExperience.AndroidOtt,
                controllerMode = PlayerControllerMode.Custom,
                features = PlayerFeatures(
                    showBackButton = true,
                    showStreamDetails = true,
                    showPlayPauseButton = true,
                    showPreviousButton = true,
                    showNextButton = true,
                    showRewindButton = true,
                    showFastForwardButton = true,
                    showSeekBar = true,
                    showSubtitles = true,
                    showQualitySelector = true,
                    showAudioSelector = true,
                    showEpgAction = false,
                    showStatsForNerds = true,
                    showPlaybackSpeed = true,
                    showShuffleButton = false,
                    showLoopButton = true,
                    showGoLiveButton = false,
                ),
                interactionConfig = interactionConfig,
                callbacks = PlayerCallbacks(onBack = onBack),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = alpha),
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                FilledTonalIconButton(onClick = onMinimizeToggle) {
                    Icon(
                        imageVector = if (isMinimized) Icons.Default.Fullscreen else Icons.Default.PictureInPictureAlt,
                        contentDescription = if (isMinimized) "Fullscreen" else "Minimize player",
                    )
                }
            }

            if (!isMinimized) {
                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                ) {
                    Icon(Icons.Default.CloseFullscreen, contentDescription = "Close fullscreen")
                }
            }
        }
    }
}

private fun VODDetail.toPlayerItem(): PlayerItem {
    return PlayerItem(
        id = id,
        title = title,
        streamUrl = streamUrl,
        sourceType = streamType.toPlayerSourceType(),
        contentType = playbackType.toPlayerContentType(),
        posterUrl = posterUrl,
        subtitle = subtitle,
        description = description,
        subtitleTracks = subtitleUrls.mapIndexed { index, url ->
            PlayerSubtitleTrack(
                id = "movie_subtitle_$index",
                label = "Subtitle ${index + 1}",
                url = url,
                isDefault = index == 0,
            )
        },
    )
}

private fun MediaStreamType.toPlayerSourceType(): PlayerSourceType {
    return when (this) {
        MediaStreamType.Hls -> PlayerSourceType.Hls
        MediaStreamType.Progressive -> PlayerSourceType.Progressive
    }
}

private fun MediaPlaybackType.toPlayerContentType(): PlayerContentType {
    return when (this) {
        MediaPlaybackType.Live -> PlayerContentType.Live
        MediaPlaybackType.Dvr -> PlayerContentType.Dvr
        MediaPlaybackType.Movie -> PlayerContentType.Vod
    }
}
