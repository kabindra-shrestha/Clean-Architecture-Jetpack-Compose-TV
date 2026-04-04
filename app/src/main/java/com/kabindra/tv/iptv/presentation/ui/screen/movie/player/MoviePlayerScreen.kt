package com.kabindra.tv.iptv.presentation.ui.screen.movie.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import com.kabindra.player.PlayerContentType
import com.kabindra.player.PlayerControllerMode
import com.kabindra.player.PlayerExperience
import com.kabindra.player.PlayerFeatures
import com.kabindra.player.PlayerItem
import com.kabindra.player.PlayerPlaylist
import com.kabindra.player.PlayerSourceType
import com.kabindra.player.UnifiedPlayer
import com.kabindra.player.defaultPlayerInteractionConfig
import com.kabindra.player.rememberPlayerHostState
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.domain.entity.MovieDetail
import com.kabindra.tv.iptv.presentation.ui.component.ButtonComponent
import com.kabindra.tv.iptv.presentation.ui.component.LoadingIndicator
import com.kabindra.tv.iptv.presentation.ui.component.TextComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextSize
import com.kabindra.tv.iptv.presentation.ui.component.TextType
import com.kabindra.tv.iptv.utils.extensions.mainBackground
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

private object MovieScreenTokens {
    const val headerHorizontalPadding = 24
    const val headerVerticalPadding = 24
}

@Composable
fun MoviePlayerScreen(
    viewModel: MoviePlayerViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    movieId: String,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val playerHostState = rememberPlayerHostState(movieId)

    BackHandler(onBack = onBack)

    LaunchedEffect(movieId) {
        viewModel.loadMovie(movieId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        state.movie?.let { movie ->
            UnifiedPlayer(
                playlist = PlayerPlaylist(
                    items = listOf(movie.toPlayerItem()),
                    startIndex = 0,
                    autoPlay = true,
                ),
                hostState = playerHostState,
                experience = PlayerExperience.AndroidTv,
                controllerMode = PlayerControllerMode.Default,
                features = PlayerFeatures(
                    /*showStreamDetails = true,
                    showPreviousButton = false,
                    showNextButton = false,
                    showRewindButton = true,
                    showFastForwardButton = true,
                    showSeekBar = true,
                    showSubtitles = true,
                    showQualitySelector = true,
                    showAudioSelector = true,
                    showEpgAction = false,
                    showStatsForNerds = false,
                    showPlaybackSpeed = true,
                    showShuffleButton = false,
                    showLoopButton = true,
                    showGoLiveButton = false,*/
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
                    showEpgAction = true,
                    showStatsForNerds = true,
                    showPlaybackSpeed = true,
                    showShuffleButton = true,
                    showLoopButton = true,
                    showGoLiveButton = true,
                ),
                interactionConfig = defaultPlayerInteractionConfig(PlayerExperience.AndroidTv),
                modifier = Modifier.fillMaxSize()
            )
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextComponent(
                        text = state.errorMessage,
                        type = TextType.Body,
                        size = TextSize.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ButtonComponent(
                        modifier = Modifier.padding(top = 10.sdp),
                        text = "Back",
                        onClick = onBack
                    )
                }
            }
        }
    }
}

private fun MovieDetail.toPlayerItem(): PlayerItem {
    return PlayerItem(
        id = id,
        title = title,
        streamUrl = streamUrl,
        sourceType = streamType.toPlayerSourceType(),
        contentType = PlayerContentType.Vod,
        posterUrl = posterUrl,
    )
}

private fun MediaStreamType.toPlayerSourceType(): PlayerSourceType {
    return when (this) {
        MediaStreamType.Hls -> PlayerSourceType.Hls
        MediaStreamType.Progressive -> PlayerSourceType.Progressive
    }
}
