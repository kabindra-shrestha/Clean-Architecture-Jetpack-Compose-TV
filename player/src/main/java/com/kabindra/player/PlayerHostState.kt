package com.kabindra.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.kabindra.player.player.core.PlayerSessionController

@Composable
fun rememberPlayerHostState(vararg keys: Any?): PlayerHostState {
    return remember(*keys) { PlayerHostState() }
}

@Stable
class PlayerHostState internal constructor() : PlayerSessionController {
    var uiState by mutableStateOf(PlayerUiState())
        internal set

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var performanceConfig: PlayerPerformanceConfig = PlayerPerformanceConfig()
    private var subtitleTargets: Map<String, PlayerTrackTarget> = emptyMap()
    private var audioTargets: Map<String, PlayerTrackTarget> = emptyMap()
    private var videoTargets: Map<String, PlayerTrackTarget> = emptyMap()

    internal fun attach(
        player: ExoPlayer,
        playerView: PlayerView,
        performanceConfig: PlayerPerformanceConfig,
    ) {
        this.player = player
        this.playerView = playerView
        this.performanceConfig = performanceConfig
    }

    internal fun detach(player: ExoPlayer) {
        if (this.player !== player) return
        this.player = null
        this.playerView = null
        this.subtitleTargets = emptyMap()
        this.audioTargets = emptyMap()
        this.videoTargets = emptyMap()
    }

    internal fun updateTrackTargets(
        subtitles: Map<String, PlayerTrackTarget>,
        audios: Map<String, PlayerTrackTarget>,
        videos: Map<String, PlayerTrackTarget>,
    ) {
        subtitleTargets = subtitles
        audioTargets = audios
        videoTargets = videos
    }

    internal fun openPanel(panel: PlayerPanel) {
        uiState = uiState.copy(
            activePanel = panel,
            isStatsVisible = panel == PlayerPanel.Stats,
            isControllerVisible = true,
        )
    }

    internal fun dismissPanel() {
        uiState = uiState.copy(
            activePanel = PlayerPanel.None,
            isStatsVisible = false,
        )
    }

    internal fun updateControllerVisibility(isVisible: Boolean) {
        uiState = uiState.copy(
            isControllerVisible = isVisible,
            isStatsVisible = if (!isVisible && uiState.activePanel == PlayerPanel.Stats) {
                false
            } else {
                uiState.isStatsVisible
            }
        )
    }

    override fun play() {
        player?.playWhenReady = true
    }

    override fun pause() {
        player?.playWhenReady = false
    }

    override fun stop() {
        player?.stop()
    }

    override fun release() {
        player?.release()
        player = null
        playerView = null
    }

    override fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs.coerceAtLeast(0L))
    }

    override fun fastForward() {
        val currentPlayer = player ?: return
        seekTo(currentPlayer.currentPosition + performanceConfig.seekForwardMs)
    }

    override fun rewind() {
        val currentPlayer = player ?: return
        seekTo(currentPlayer.currentPosition - performanceConfig.seekBackMs)
    }

    override fun next() {
        val currentPlayer = player ?: return
        if (currentPlayer.hasNextMediaItem()) {
            currentPlayer.seekToNextMediaItem()
            currentPlayer.playWhenReady = true
        }
    }

    override fun previous() {
        val currentPlayer = player ?: return
        if (currentPlayer.hasPreviousMediaItem()) {
            currentPlayer.seekToPreviousMediaItem()
            currentPlayer.playWhenReady = true
        }
    }

    override fun playItem(index: Int) {
        val currentPlayer = player ?: return
        if (index !in 0 until currentPlayer.mediaItemCount) return
        currentPlayer.seekToDefaultPosition(index)
        currentPlayer.playWhenReady = true
    }

    override fun togglePlayPause() {
        val currentPlayer = player ?: return
        currentPlayer.playWhenReady = !currentPlayer.isPlaying
    }

    override fun jumpToLiveEdge() {
        val currentPlayer = player ?: return
        currentPlayer.seekToDefaultPosition()
        currentPlayer.playWhenReady = true
    }

    override fun setRepeatMode(repeatMode: PlayerRepeatMode) {
        player?.repeatMode = repeatMode.toMedia3RepeatMode()
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        player?.shuffleModeEnabled = enabled
    }

    override fun setPlaybackSpeed(speed: Float) {
        player?.playbackParameters = PlaybackParameters(speed.coerceAtLeast(0.25f))
    }

    override fun selectSubtitleTrack(trackId: String) {
        val target = subtitleTargets[trackId] ?: return
        val currentPlayer = player ?: return
        currentPlayer.trackSelectionParameters = currentPlayer.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
            .addOverride(TrackSelectionOverride(target.group, listOf(target.trackIndex)))
            .build()
    }

    override fun disableSubtitles() {
        val currentPlayer = player ?: return
        currentPlayer.trackSelectionParameters = currentPlayer.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .build()
    }

    override fun selectAudioTrack(trackId: String) {
        val target = audioTargets[trackId] ?: return
        val currentPlayer = player ?: return
        currentPlayer.trackSelectionParameters = currentPlayer.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
            .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
            .addOverride(TrackSelectionOverride(target.group, listOf(target.trackIndex)))
            .build()
    }

    override fun selectVideoTrack(trackId: String) {
        val currentPlayer = player ?: return
        val parameters = currentPlayer.trackSelectionParameters.buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false)

        if (trackId != PLAYER_VIDEO_TRACK_AUTO) {
            val target = videoTargets[trackId] ?: return
            parameters.addOverride(TrackSelectionOverride(target.group, listOf(target.trackIndex)))
        }

        currentPlayer.trackSelectionParameters = parameters.build()
    }

    override fun showController() {
        playerView?.showController()
        updateControllerVisibility(true)
    }

    override fun hideController() {
        playerView?.hideController()
        updateControllerVisibility(false)
    }

    override fun showStatsPanel() {
        openPanel(PlayerPanel.Stats)
    }

    override fun hideStatsPanel() {
        if (uiState.activePanel == PlayerPanel.Stats) {
            dismissPanel()
        } else {
            uiState = uiState.copy(isStatsVisible = false)
        }
    }
}

internal data class PlayerTrackTarget(
    val group: TrackGroup,
    val trackIndex: Int,
)

private fun PlayerRepeatMode.toMedia3RepeatMode(): Int {
    return when (this) {
        PlayerRepeatMode.Off -> androidx.media3.common.Player.REPEAT_MODE_OFF
        PlayerRepeatMode.One -> androidx.media3.common.Player.REPEAT_MODE_ONE
        PlayerRepeatMode.All -> androidx.media3.common.Player.REPEAT_MODE_ALL
    }
}
