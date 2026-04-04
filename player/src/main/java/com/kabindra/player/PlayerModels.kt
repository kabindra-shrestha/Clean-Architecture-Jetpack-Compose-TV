package com.kabindra.player

import androidx.compose.runtime.Immutable
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import com.kabindra.player.player.telemetry.collector.PlaybackTelemetryCollector
import com.kabindra.player.player.telemetry.model.LivePlaybackSnapshot

enum class PlayerExperience {
    AndroidTv,
    AndroidOtt,
}

enum class PlayerControllerMode {
    Default,
    Custom,
}

enum class PlayerSourceType {
    Hls,
    Progressive,
}

enum class PlayerContentType {
    Live,
    Vod,
    Dvr,
}

enum class PlayerRepeatMode {
    Off,
    One,
    All,
}

enum class PlayerPanel {
    None,
    Quality,
    Subtitles,
    Audio,
    Speed,
    Stats,
}

@Immutable
data class PlayerSubtitleTrack(
    val id: String,
    val label: String,
    val language: String? = null,
    val mimeType: String? = null,
    val url: String? = null,
    val isDefault: Boolean = false,
    val isSelected: Boolean = false,
)

@Immutable
data class PlayerAudioTrack(
    val id: String,
    val label: String,
    val language: String? = null,
    val channels: Int? = null,
    val isSelected: Boolean = false,
)

@Immutable
data class PlayerVideoTrack(
    val id: String,
    val label: String,
    val width: Int? = null,
    val height: Int? = null,
    val bitrateKbps: Int? = null,
    val isAdaptive: Boolean = false,
    val isSelected: Boolean = false,
)

@Immutable
data class PlayerProgramInfo(
    val channelName: String? = null,
    val currentTitle: String? = null,
    val nextTitle: String? = null,
    val startTimeText: String? = null,
    val endTimeText: String? = null,
)

@Immutable
data class PlayerItem(
    val id: String,
    val title: String,
    val streamUrl: String,
    val sourceType: PlayerSourceType = PlayerSourceType.Progressive,
    val contentType: PlayerContentType,
    val posterUrl: String? = null,
    val subtitleTracks: List<PlayerSubtitleTrack> = emptyList(),
    val programInfo: PlayerProgramInfo? = null,
    val isSeekable: Boolean = true,
    val isLiveEdgeDefault: Boolean = false,
)

@Immutable
data class PlayerPlaylist(
    val items: List<PlayerItem> = emptyList(),
    val startIndex: Int = 0,
    val autoPlay: Boolean = true,
    val repeatMode: PlayerRepeatMode = PlayerRepeatMode.Off,
    val loopSingleItem: Boolean = false,
    val shuffleEnabled: Boolean = false,
)

@Immutable
data class PlayerFeatures(
    val showBackButton: Boolean = false,
    val showStreamDetails: Boolean = false,
    val showPlayPauseButton: Boolean = false,
    val showPreviousButton: Boolean = false,
    val showNextButton: Boolean = false,
    val showRewindButton: Boolean = false,
    val showFastForwardButton: Boolean = false,
    val showSeekBar: Boolean = false,
    val showSubtitles: Boolean = false,
    val showQualitySelector: Boolean = false,
    val showAudioSelector: Boolean = false,
    val showEpgAction: Boolean = false,
    val showStatsForNerds: Boolean = false,
    val showPlaybackSpeed: Boolean = false,
    val showShuffleButton: Boolean = false,
    val showLoopButton: Boolean = false,
    val showGoLiveButton: Boolean = false,
)

@Immutable
data class PlayerInteractionConfig(
    val enableFocus: Boolean = true,
    val enableTouchGestures: Boolean = false,
    val autoHideController: Boolean = true,
    val controllerAutoHideMillis: Long = 4_500L,
    val showControllerOnTap: Boolean = true,
    val showControllerOnKeyPress: Boolean = true,
)

fun defaultPlayerInteractionConfig(experience: PlayerExperience): PlayerInteractionConfig {
    return when (experience) {
        PlayerExperience.AndroidTv -> PlayerInteractionConfig(
            enableFocus = true,
            enableTouchGestures = false,
            autoHideController = true,
            controllerAutoHideMillis = 4_500L,
            showControllerOnTap = true,
            showControllerOnKeyPress = true,
        )

        PlayerExperience.AndroidOtt -> PlayerInteractionConfig(
            enableFocus = false,
            enableTouchGestures = true,
            autoHideController = true,
            controllerAutoHideMillis = 4_000L,
            showControllerOnTap = true,
            showControllerOnKeyPress = false,
        )
    }
}

@Immutable
data class PlayerBufferConfig(
    val minBufferMs: Int = 15_000,
    val maxBufferMs: Int = 50_000,
    val bufferForPlaybackMs: Int = 500,
    val bufferForPlaybackAfterRebufferMs: Int = 1_000,
    val backBufferMs: Int = 5_000,
)

@UnstableApi
@Immutable
data class PlayerPerformanceConfig(
    val preferExtensionRenderers: Boolean = true,
    val seekForwardMs: Long = 10_000L,
    val seekBackMs: Long = 10_000L,
    val keepScreenOn: Boolean = true,
    val handleAudioBecomingNoisy: Boolean = true,
    val videoScalingMode: Int = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING,
)

@Immutable
data class PlayerTelemetryConfig(
    val enabled: Boolean = false,
    val collector: PlaybackTelemetryCollector? = null,
    val emitSnapshotsToCallback: Boolean = false,
    val trackHistoryLength: Int = 60,
    val sampleIntervalMs: Long = 1_000L,
)

@Immutable
data class PlayerCallbacks(
    val onBack: (() -> Unit)? = null,
    val onEpgClick: ((PlayerItem?) -> Unit)? = null,
    val onItemChanged: ((PlayerItem, Int) -> Unit)? = null,
    val onPlaybackError: ((PlaybackException) -> Unit)? = null,
    val onTelemetrySnapshot: ((LivePlaybackSnapshot) -> Unit)? = null,
)

@Immutable
data class PlayerUiState(
    val playlist: PlayerPlaylist = PlayerPlaylist(),
    val currentItem: PlayerItem? = null,
    val currentIndex: Int = 0,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val playbackState: Int = androidx.media3.common.Player.STATE_IDLE,
    val isLoading: Boolean = false,
    val isLive: Boolean = false,
    val hasDvr: Boolean = false,
    val atLiveEdge: Boolean = false,
    val liveOffsetMs: Long? = null,
    val canSeek: Boolean = false,
    val canGoNext: Boolean = false,
    val canGoPrevious: Boolean = false,
    val repeatMode: PlayerRepeatMode = PlayerRepeatMode.Off,
    val shuffleEnabled: Boolean = false,
    val playbackSpeed: Float = 1f,
    val availableSubtitleTracks: List<PlayerSubtitleTrack> = emptyList(),
    val availableAudioTracks: List<PlayerAudioTrack> = emptyList(),
    val availableVideoTracks: List<PlayerVideoTrack> = emptyList(),
    val selectedSubtitleTrackId: String? = null,
    val selectedAudioTrackId: String? = null,
    val selectedVideoTrackId: String? = null,
    val activePanel: PlayerPanel = PlayerPanel.None,
    val isControllerVisible: Boolean = true,
    val isStatsVisible: Boolean = false,
    val errorMessage: String? = null,
)

internal const val PLAYER_VIDEO_TRACK_AUTO = "player_video_track_auto"
