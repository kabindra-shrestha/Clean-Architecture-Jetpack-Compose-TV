package com.kabindra.player

import android.net.Uri
import android.os.SystemClock
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOn
import androidx.compose.material.icons.rounded.RepeatOneOn
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.ShuffleOn
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import com.kabindra.player.player.telemetry.collector.DefaultPlaybackTelemetryCollector
import com.kabindra.player.player.telemetry.collector.NoOpPlaybackTelemetryCollector
import com.kabindra.player.player.telemetry.collector.PlaybackTelemetryCollector
import com.kabindra.player.player.telemetry.model.LivePlaybackSnapshot
import com.kabindra.player.player.telemetry.model.NetworkSample
import com.kabindra.player.player.telemetry.model.PlaybackIssue
import com.kabindra.player.player.telemetry.model.TrackSnapshot
import com.kabindra.player.player.util.ExoPlayerUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.IOException
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton as Media3PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton as Media3SeekBackButton
import androidx.media3.ui.compose.material3.buttons.SeekForwardButton as Media3SeekForwardButton

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun UnifiedPlayer(
    playlist: PlayerPlaylist,
    modifier: Modifier = Modifier,
    hostState: PlayerHostState = rememberPlayerHostState(),
    experience: PlayerExperience = PlayerExperience.AndroidTv,
    controllerMode: PlayerControllerMode = PlayerControllerMode.Default,
    features: PlayerFeatures = PlayerFeatures(),
    interactionConfig: PlayerInteractionConfig = defaultPlayerInteractionConfig(experience),
    bufferConfig: PlayerBufferConfig = PlayerBufferConfig(),
    performanceConfig: PlayerPerformanceConfig = PlayerPerformanceConfig(),
    telemetryConfig: PlayerTelemetryConfig = PlayerTelemetryConfig(),
    callbacks: PlayerCallbacks = PlayerCallbacks(),
    panelContent: (@Composable (PlayerPanel, PlayerUiState, PlayerHostState, () -> Unit) -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentCallbacks by rememberUpdatedState(callbacks)
    val currentFeatures by rememberUpdatedState(features)
    val currentInteractionConfig by rememberUpdatedState(interactionConfig)
    val currentPanelContent by rememberUpdatedState(panelContent)
    val currentPlaylist by rememberUpdatedState(playlist)
    val telemetryCollector = rememberTelemetryCollector(telemetryConfig)
    val currentTelemetryCollector by rememberUpdatedState(telemetryCollector)
    val transferBytes = remember { AtomicLong(0L) }
    val transferSamples = remember { mutableMapOf<Int, TransferSample>() }
    var controllerInteractionToken by remember { mutableIntStateOf(0) }
    val hiddenOverlayFocusRequester = remember { FocusRequester() }
    val primaryControlFocusRequester = remember { FocusRequester() }
    val panelFocusRequester = remember { FocusRequester() }
    val registerInteraction = remember(hostState, controllerMode) {
        {
            controllerInteractionToken += 1
            hostState.showController()
        }
    }
    val telemetryTransferListener = remember(telemetryConfig.enabled) {
        object : TransferListener {
            override fun onTransferInitializing(
                source: DataSource,
                dataSpec: DataSpec,
                isNetwork: Boolean,
            ) = Unit

            override fun onTransferStart(
                source: DataSource,
                dataSpec: DataSpec,
                isNetwork: Boolean,
            ) {
                if (!telemetryConfig.enabled || !isNetwork) return
                transferSamples[System.identityHashCode(dataSpec)] = TransferSample(
                    startedAtMs = System.currentTimeMillis(),
                    dataSpec = dataSpec,
                )
            }

            override fun onBytesTransferred(
                source: DataSource,
                dataSpec: DataSpec,
                isNetwork: Boolean,
                bytesTransferred: Int,
            ) {
                if (!telemetryConfig.enabled || !isNetwork || bytesTransferred <= 0) return
                transferBytes.addAndGet(bytesTransferred.toLong())
                val key = System.identityHashCode(dataSpec)
                val sample = transferSamples[key] ?: return
                transferSamples[key] = sample.copy(
                    transferredBytes = sample.transferredBytes + bytesTransferred,
                )
            }

            override fun onTransferEnd(
                source: DataSource,
                dataSpec: DataSpec,
                isNetwork: Boolean,
            ) {
                if (!telemetryConfig.enabled || !isNetwork) return
                val key = System.identityHashCode(dataSpec)
                val sample = transferSamples.remove(key) ?: return
                val now = System.currentTimeMillis()
                val durationMs = (now - sample.startedAtMs).coerceAtLeast(1L)
                telemetryCollector.appendNetworkSample(
                    NetworkSample(
                        timestampMs = now,
                        throughputKbps = calculateThroughputKbps(
                            sample.transferredBytes,
                            durationMs
                        ),
                        host = dataSpec.uri.host,
                        segmentUri = dataSpec.uri.toString(),
                        transferBytes = sample.transferredBytes,
                        transferDurationMs = durationMs,
                    )
                )
            }
        }
    }

    val player = remember(context, bufferConfig, performanceConfig, telemetryConfig.enabled) {
        ExoPlayerUtils.createExoPlayer(
            context = context,
            bufferConfig = bufferConfig,
            performanceConfig = performanceConfig,
            transferListener = telemetryTransferListener,
        )
    }
    var allowPlayback by remember(player) { mutableStateOf(true) }
    var resumePlaybackWhenStarted by remember(player) { mutableStateOf(playlist.autoPlay) }
    val playerView = remember(context) { PlayerView(context) }
    var telemetrySessionId by remember(player) { mutableStateOf<String?>(null) }
    val playlistSignature = remember(playlist.items) {
        playlist.items.map { item ->
            listOf(
                item.id,
                item.title,
                item.streamUrl,
                item.contentType.name,
                item.posterUrl.orEmpty(),
                item.isSeekable.toString(),
            )
        }
    }

    DisposableEffect(player, playerView, performanceConfig) {
        player.videoScalingMode = performanceConfig.videoScalingMode
        if (controllerMode == PlayerControllerMode.Default) {
            playerView.player = player
            playerView.useController = true
            playerView.controllerAutoShow = true
            playerView.keepScreenOn = performanceConfig.keepScreenOn
        }
        hostState.attach(
            player = player,
            playerView = if (controllerMode == PlayerControllerMode.Default) playerView else null,
            performanceConfig = performanceConfig,
        )
        onDispose {
            hostState.detach(player)
            playerView.player = null
        }
    }

    DisposableEffect(playerView, controllerMode) {
        if (controllerMode == PlayerControllerMode.Default) {
            val listener = PlayerView.ControllerVisibilityListener { visibility ->
                hostState.updateControllerVisibility(visibility == View.VISIBLE)
            }
            playerView.setControllerVisibilityListener(listener)
        } else {
            hostState.updateControllerVisibility(true)
        }
        onDispose {
            playerView.setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?)
        }
    }

    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    allowPlayback = true
                    if (resumePlaybackWhenStarted && currentPlaylist.autoPlay) {
                        player.playWhenReady = true
                    }
                }

                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    resumePlaybackWhenStarted = player.playWhenReady || player.isPlaying
                    allowPlayback = false
                    player.playWhenReady = false
                }

                Lifecycle.Event.ON_DESTROY -> {
                    allowPlayback = false
                    player.playWhenReady = false
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(player, telemetryConfig.enabled) {
        val playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    hostState.clearPlaybackError()
                }
                updateUiState(player, hostState, currentPlaylist)
                if (telemetryConfig.enabled) {
                    currentTelemetryCollector.onPlaybackStateChanged(
                        playbackState = playbackState.toTelemetryPlaybackState(),
                        playbackPositionMs = player.currentPosition,
                        bufferedDurationMs = player.totalBufferedDuration,
                    )
                    currentTelemetryCollector.onLiveLatencyUpdated(
                        player.currentLiveOffset.takeIf { it != C.TIME_UNSET }
                    )
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateUiState(player, hostState, currentPlaylist)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                hostState.clearPlaybackError()
                if (telemetryConfig.enabled) {
                    finishTelemetrySession(currentTelemetryCollector)
                    telemetrySessionId = null
                    ensureTelemetrySession(
                        player,
                        currentPlaylist,
                        currentTelemetryCollector,
                        telemetrySessionId
                    ) {
                        telemetrySessionId = it
                    }
                }
                updateUiState(player, hostState, currentPlaylist)
                currentPlaylist.items.getOrNull(player.currentMediaItemIndex)?.let { item ->
                    currentCallbacks.onItemChanged?.invoke(item, player.currentMediaItemIndex)
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                updateUiState(player, hostState, currentPlaylist)
                if (telemetryConfig.enabled) {
                    selectedTrackSnapshotFromTracks(tracks)?.let(currentTelemetryCollector::appendTrackSnapshot)
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateUiState(player, hostState, currentPlaylist)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateUiState(player, hostState, currentPlaylist)
            }

            override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
                updateUiState(player, hostState, currentPlaylist)
            }

            override fun onPlayerError(error: PlaybackException) {
                hostState.setPlaybackError(error.localizedMessage)
                if (telemetryConfig.enabled) {
                    currentTelemetryCollector.appendIssue(
                        PlaybackIssue(
                            timestampMs = System.currentTimeMillis(),
                            category = "PLAYER_ERROR",
                            message = error.localizedMessage ?: "Unknown playback error",
                            code = error.errorCode,
                        ),
                        isFatal = true,
                    )
                }
                currentCallbacks.onPlaybackError?.invoke(error)
            }
        }

        val analyticsListener = object : AnalyticsListener {
            override fun onRenderedFirstFrame(
                eventTime: AnalyticsListener.EventTime,
                output: Any,
                renderTimeMs: Long
            ) {
                if (telemetryConfig.enabled) {
                    currentTelemetryCollector.onFirstFrameRendered()
                }
            }

            override fun onBandwidthEstimate(
                eventTime: AnalyticsListener.EventTime,
                totalLoadTimeMs: Int,
                totalBytesLoaded: Long,
                bitrateEstimate: Long,
            ) {
                if (!telemetryConfig.enabled) return
                currentTelemetryCollector.appendNetworkSample(
                    NetworkSample(
                        timestampMs = System.currentTimeMillis(),
                        throughputKbps = bitrateEstimate.toKbpsFromBitsPerSecond(),
                        transferBytes = totalBytesLoaded,
                        transferDurationMs = totalLoadTimeMs.toLong(),
                        bufferMs = player.totalBufferedDuration,
                    )
                )
            }

            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData,
            ) {
                if (!telemetryConfig.enabled) return
                currentTelemetryCollector.appendNetworkSample(
                    NetworkSample(
                        timestampMs = System.currentTimeMillis(),
                        throughputKbps = calculateThroughputKbps(
                            bytesLoaded = loadEventInfo.bytesLoaded,
                            loadDurationMs = loadEventInfo.loadDurationMs,
                        ),
                        bufferMs = player.totalBufferedDuration,
                        responseCode = loadEventInfo.responseHeaders["Response-Code"]
                            ?.firstOrNull()
                            ?.toIntOrNull(),
                        host = loadEventInfo.uri.host,
                        segmentUri = loadEventInfo.uri.toString(),
                        transferBytes = loadEventInfo.bytesLoaded,
                        transferDurationMs = loadEventInfo.loadDurationMs,
                    )
                )
            }

            override fun onLoadError(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData,
                error: IOException,
                wasCanceled: Boolean,
            ) {
                if (!telemetryConfig.enabled) return
                currentTelemetryCollector.appendIssue(
                    PlaybackIssue(
                        timestampMs = System.currentTimeMillis(),
                        category = "LOAD_ERROR",
                        message = error.localizedMessage ?: "Network load error",
                    )
                )
            }

            override fun onDroppedVideoFrames(
                eventTime: AnalyticsListener.EventTime,
                droppedFrames: Int,
                elapsedMs: Long,
            ) {
                if (!telemetryConfig.enabled) return
                currentTelemetryCollector.onDroppedFrames(
                    droppedFrames = droppedFrames,
                    elapsedMs = elapsedMs,
                )
            }
        }

        player.addListener(playerListener)
        if (telemetryConfig.enabled) {
            player.addAnalyticsListener(analyticsListener)
        }
        updateUiState(player, hostState, currentPlaylist)

        onDispose {
            player.removeListener(playerListener)
            if (telemetryConfig.enabled) {
                player.removeAnalyticsListener(analyticsListener)
                finishTelemetrySession(currentTelemetryCollector)
                currentTelemetryCollector.reset()
            }
            player.release()
        }
    }

    LaunchedEffect(playlistSignature) {
        if (playlist.items.isEmpty()) {
            player.stop()
            player.clearMediaItems()
            hostState.uiState = PlayerUiState(playlist = playlist)
            return@LaunchedEffect
        }

        hostState.markNextPlaybackErrorPhase(
            if (hostState.uiState.currentItem == null) {
                PlayerPlaybackErrorPhase.Initial
            } else {
                PlayerPlaybackErrorPhase.Switching
            }
        )
        hostState.clearPlaybackError()
        val mediaItems = playlist.items.map(PlayerItem::toMediaItem)
        player.setMediaItems(
            mediaItems,
            playlist.startIndex.coerceIn(0, playlist.items.lastIndex),
            0L,
        )
        player.prepare()
        resumePlaybackWhenStarted = playlist.autoPlay
        player.playWhenReady = playlist.autoPlay && allowPlayback
        updateUiState(player, hostState, playlist)
        if (telemetryConfig.enabled) {
            finishTelemetrySession(currentTelemetryCollector)
            telemetrySessionId = null
            ensureTelemetrySession(
                player,
                playlist,
                currentTelemetryCollector,
                telemetrySessionId
            ) {
                telemetrySessionId = it
            }
        }
    }

    LaunchedEffect(playlist.startIndex) {
        if (playlist.items.isEmpty()) return@LaunchedEffect
        val targetIndex = playlist.startIndex.coerceIn(0, playlist.items.lastIndex)
        if (player.currentMediaItemIndex != targetIndex) {
            hostState.markNextPlaybackErrorPhase(PlayerPlaybackErrorPhase.Switching)
            hostState.clearPlaybackError()
            player.seekToDefaultPosition(targetIndex)
        }
        if (player.mediaItemCount > 0 && player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }
        if (playlist.autoPlay && allowPlayback) {
            player.playWhenReady = true
        }
        updateUiState(player, hostState, playlist)
    }

    LaunchedEffect(playlist.shuffleEnabled, playlist.repeatMode, playlist.loopSingleItem) {
        player.shuffleModeEnabled = playlist.shuffleEnabled
        player.repeatMode = when {
            playlist.loopSingleItem -> Player.REPEAT_MODE_ONE
            else -> playlist.repeatMode.toUnifiedMedia3RepeatMode()
        }
        updateUiState(player, hostState, playlist)
    }

    val uiState = hostState.uiState

    LaunchedEffect(player, telemetryConfig.sampleIntervalMs, playlistSignature) {
        val intervalMs = telemetryConfig.sampleIntervalMs.coerceAtLeast(250L)
        while (isActive) {
            updateUiState(player, hostState, currentPlaylist)
            if (telemetryConfig.enabled && telemetryConfig.emitSnapshotsToCallback) {
                currentTelemetryCollector.currentSnapshot()
                    ?.let(currentCallbacks.onTelemetrySnapshot ?: {})
            }
            delay(intervalMs)
        }
    }

    LaunchedEffect(
        controllerMode,
        currentInteractionConfig.autoHideController,
        currentInteractionConfig.controllerAutoHideMillis,
        uiState.isControllerVisible,
        uiState.activePanel,
        uiState.isLoading,
        uiState.playbackError,
        controllerInteractionToken,
    ) {
        if (controllerMode != PlayerControllerMode.Custom) return@LaunchedEffect
        if (!currentInteractionConfig.autoHideController) return@LaunchedEffect
        if (!uiState.isControllerVisible) return@LaunchedEffect
        if (uiState.activePanel != PlayerPanel.None) return@LaunchedEffect
        if (uiState.isLoading) return@LaunchedEffect
        if (uiState.playbackError != null) return@LaunchedEffect

        delay(currentInteractionConfig.controllerAutoHideMillis.coerceAtLeast(1_500L))
        if (hostState.uiState.activePanel == PlayerPanel.None) {
            hostState.hideController()
        }
    }

    LaunchedEffect(
        controllerMode,
        currentInteractionConfig.enableFocus,
        uiState.isControllerVisible,
        uiState.activePanel,
    ) {
        if (!currentInteractionConfig.enableFocus) return@LaunchedEffect
        if (controllerMode != PlayerControllerMode.Custom) return@LaunchedEffect

        when {
            uiState.activePanel != PlayerPanel.None -> {
                runCatching { panelFocusRequester.requestFocus() }
            }

            uiState.isControllerVisible -> {
                runCatching { primaryControlFocusRequester.requestFocus() }
            }

            else -> {
                runCatching { hiddenOverlayFocusRequester.requestFocus() }
            }
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(currentInteractionConfig.showControllerOnTap) {
                if (!currentInteractionConfig.showControllerOnTap) return@pointerInput
                detectTapGestures { registerInteraction() }
            }
    ) {
        if (controllerMode == PlayerControllerMode.Custom) {
            ContentFrame(
                player = player,
                modifier = Modifier.fillMaxSize(),
                surfaceType = SURFACE_TYPE_TEXTURE_VIEW,
                contentScale = ContentScale.Fit,
                keepContentOnReset = true,
            )
            SubtitleOverlay(
                player = player,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { playerView },
                update = { view ->
                    view.player = player
                    view.useController = true
                    view.controllerAutoShow = true
                    view.keepScreenOn = performanceConfig.keepScreenOn
                },
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            ) {
                PlayerLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        if (controllerMode == PlayerControllerMode.Custom &&
            currentInteractionConfig.enableFocus &&
            !uiState.isControllerVisible &&
            uiState.activePanel == PlayerPanel.None
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(hiddenOverlayFocusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when {
                            currentInteractionConfig.showControllerOnConfirmKey &&
                                    event.key.isConfirmKey() -> {
                                registerInteraction()
                                true
                            }

                            currentInteractionConfig.showControllerOnDirectionalKeys &&
                                    event.key.isDirectionalKey() -> {
                                registerInteraction()
                                true
                            }

                            else -> false
                        }
                    }
            )
        }

        AnimatedVisibility(
            visible = controllerMode == PlayerControllerMode.Default &&
                    features.showStreamDetails &&
                    uiState.isControllerVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            StreamDetailsOverlay(
                uiState = uiState,
                features = features,
                callbacks = callbacks,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }

        AnimatedVisibility(
            visible = controllerMode == PlayerControllerMode.Default && uiState.isControllerVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            DefaultActionRail(
                uiState = uiState,
                features = features,
                interactionConfig = currentInteractionConfig,
                hostState = hostState,
                callbacks = callbacks,
                onUserInteraction = registerInteraction,
                modifier = Modifier
                    .align(
                        if (experience == PlayerExperience.AndroidTv) Alignment.BottomEnd
                        else Alignment.TopEnd
                    )
                    .padding(16.dp)
            )
        }

        val controllerVisible =
            controllerMode == PlayerControllerMode.Custom && uiState.isControllerVisible

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer {
                    alpha = if (controllerVisible) 1f else 0f
                }
                // Block pointer events when invisible so taps still reach the video
                .then(
                    if (!controllerVisible) Modifier.pointerInput(Unit) { /* consume nothing */ }
                    else Modifier
                )
        ) {
            CustomControllerOverlay(
                player = player,
                uiState = uiState,
                features = features,
                interactionConfig = currentInteractionConfig,
                hostState = hostState,
                callbacks = callbacks,
                experience = experience,
                performanceConfig = performanceConfig,
                primaryFocusRequester = primaryControlFocusRequester,
                onUserInteraction = registerInteraction,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        if (uiState.activePanel != PlayerPanel.None) {
            val dismiss = hostState::dismissPanel
            Box(
                modifier = Modifier.align(
                    if (experience == PlayerExperience.AndroidTv) Alignment.CenterEnd
                    else Alignment.BottomCenter
                )
            ) {
                currentPanelContent?.invoke(uiState.activePanel, uiState, hostState, dismiss)
                    ?: DefaultPanelContent(
                        panel = uiState.activePanel,
                        uiState = uiState,
                        hostState = hostState,
                        telemetrySnapshot = if (telemetryConfig.enabled) currentTelemetryCollector.currentSnapshot() else null,
                        experience = experience,
                        initialFocusRequester = panelFocusRequester,
                        onDismiss = dismiss,
                    )
            }
        }

        uiState.playbackError?.let { playbackError ->
            PlaybackErrorOverlay(
                playbackError = playbackError,
                onReplay = {
                    registerInteraction()
                    hostState.replayCurrent()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Deprecated(
    message = "Use UnifiedPlayer(...) with PlayerPlaylist and PlayerFeatures instead.",
    replaceWith = ReplaceWith("UnifiedPlayer()"),
)
@Composable
fun Player(
    videoURL: String,
    showStatsForNerds: Boolean,
    playbackRequestToken: Int = 0,
) {
    val hostState = rememberPlayerHostState(videoURL, playbackRequestToken)
    UnifiedPlayer(
        playlist = PlayerPlaylist(
            items = listOf(
                PlayerItem(
                    id = "legacy-$playbackRequestToken",
                    title = "Player",
                    streamUrl = videoURL,
                    sourceType = if (videoURL.endsWith(".m3u8", ignoreCase = true)) {
                        PlayerSourceType.Hls
                    } else {
                        PlayerSourceType.Progressive
                    },
                    contentType = PlayerContentType.Vod,
                )
            )
        ),
        hostState = hostState,
        controllerMode = PlayerControllerMode.Custom,
        features = PlayerFeatures(
            showStreamDetails = false,
            showPreviousButton = false,
            showNextButton = false,
            showStatsForNerds = showStatsForNerds,
        ),
        telemetryConfig = PlayerTelemetryConfig(enabled = showStatsForNerds),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .aspectRatio(16f / 9f),
    )
}

@Composable
private fun StreamDetailsOverlay(
    uiState: PlayerUiState,
    features: PlayerFeatures,
    callbacks: PlayerCallbacks,
    modifier: Modifier = Modifier,
) {
    val currentItem = uiState.currentItem ?: return
    val programInfo = currentItem.programInfo
    Surface(
        modifier = modifier.widthIn(max = 420.dp),
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            callbacks.onBack?.takeIf { features.showBackButton }?.let { onBack ->
                FilledTonalButton(
                    onClick = onBack,
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp),
                ) {
                    Text(text = "Back")
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = when {
                        uiState.isLive && uiState.atLiveEdge -> Color(0xFFEF5350)
                        uiState.isLive -> Color(0xFFFFB74D)
                        else -> Color(0xFF66BB6A)
                    },
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = when {
                        uiState.isLive && uiState.hasDvr -> "LIVE (DVR)"
                        uiState.isLive -> "LIVE"
                        else -> "VOD"
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                )
            }

            Text(
                text = currentItem.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
            )

            programInfo?.channelName?.takeIf { it.isNotBlank() }?.let { channelName ->
                Text(
                    text = channelName,
                    color = Color(0xFFD8D9E8),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            programInfo?.currentTitle?.takeIf { it.isNotBlank() }?.let { currentTitle ->
                Text(
                    text = currentTitle,
                    color = Color(0xFFB9BDD1),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            val timingText = listOfNotNull(programInfo?.startTimeText, programInfo?.endTimeText)
                .takeIf { it.isNotEmpty() }
                ?.joinToString(" - ")
            timingText?.let {
                Text(
                    text = it,
                    color = Color(0xFF9AA0B5),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun DefaultActionRail(
    uiState: PlayerUiState,
    features: PlayerFeatures,
    interactionConfig: PlayerInteractionConfig,
    hostState: PlayerHostState,
    callbacks: PlayerCallbacks,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (features.showEpgAction) {
            RailButton(
                label = "EPG",
                interactionConfig = interactionConfig,
                onUserInteraction = onUserInteraction,
            ) {
                callbacks.onEpgClick?.invoke(uiState.currentItem)
            }
        }
        if (features.showSubtitles && uiState.availableSubtitleTracks.isNotEmpty()) {
            RailButton("Subtitles", interactionConfig, onUserInteraction) {
                hostState.openPanel(PlayerPanel.Subtitles)
            }
        }
        if (features.showAudioSelector && uiState.availableAudioTracks.isNotEmpty()) {
            RailButton("Audio", interactionConfig, onUserInteraction) {
                hostState.openPanel(PlayerPanel.Audio)
            }
        }
        if (features.showQualitySelector && uiState.availableVideoTracks.isNotEmpty()) {
            RailButton("Quality", interactionConfig, onUserInteraction) {
                hostState.openPanel(PlayerPanel.Quality)
            }
        }
        if (features.showPlaybackSpeed) {
            RailButton("Speed", interactionConfig, onUserInteraction) {
                hostState.openPanel(PlayerPanel.Speed)
            }
        }
        if (features.showStatsForNerds) {
            RailButton("Stats", interactionConfig, onUserInteraction) {
                hostState.openPanel(PlayerPanel.Stats)
            }
        }
    }
}

@Composable
private fun PlayerText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign? = null,
    fontFamily: FontFamily? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = style.fontSize,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style.copy(
            fontFamily = fontFamily ?: style.fontFamily,
            fontSize = fontSize,
        ),
        textAlign = textAlign,
    )
}

@Composable
private fun SubtitleOverlay(
    player: Player,
    modifier: Modifier = Modifier,
) {
    var subtitleView by remember { mutableStateOf<SubtitleView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SubtitleView(context).apply {
                isClickable = false
                isFocusable = false
                setUserDefaultStyle()
                setUserDefaultTextSize()
            }
        },
        update = { view ->
            subtitleView = view
            if (player.isCommandAvailable(Player.COMMAND_GET_TEXT)) {
                view.setCues(player.currentCues.cues)
            }
        },
    )

    DisposableEffect(player, subtitleView) {
        val view = subtitleView ?: return@DisposableEffect onDispose {}
        val listener = object : Player.Listener {
            override fun onCues(cueGroup: CueGroup) {
                view.setCues(cueGroup.cues)
            }
        }

        if (player.isCommandAvailable(Player.COMMAND_GET_TEXT)) {
            view.setCues(player.currentCues.cues)
        }
        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            view.setCues(null)
        }
    }
}

@Composable
private fun playerMediaButtonColors() = IconButtonDefaults.iconButtonColors(
    contentColor = Color.White,
    disabledContentColor = Color(0xFF7B8796),
)

private fun PlayerFeatures.resolveFor(uiState: PlayerUiState): PlayerFeatures {
    return when (uiState.currentItem?.contentType) {
        PlayerContentType.Live -> copy(
            showPreviousButton = showPreviousButton && uiState.canGoPrevious,
            showNextButton = showNextButton && uiState.canGoNext,
            showRewindButton = false,
            showFastForwardButton = false,
            showSeekBar = false,
            showGoLiveButton = false,
        )

        PlayerContentType.Dvr -> copy(
            showPreviousButton = showPreviousButton && uiState.canGoPrevious,
            showNextButton = showNextButton && uiState.canGoNext,
            showRewindButton = showRewindButton,
            showFastForwardButton = showFastForwardButton,
            showSeekBar = showSeekBar,
            showGoLiveButton = showGoLiveButton,
        )

        PlayerContentType.Vod -> copy(
            showPreviousButton = false,
            showNextButton = false,
            showRewindButton = showRewindButton,
            showFastForwardButton = showFastForwardButton,
            showSeekBar = showSeekBar,
            showGoLiveButton = false,
        )

        null -> copy(
            showPreviousButton = false,
            showNextButton = false,
            showRewindButton = false,
            showFastForwardButton = false,
            showSeekBar = false,
            showGoLiveButton = false,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PlayerLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    CircularWavyProgressIndicator(
        modifier = modifier.size(34.dp),
        color = Color.White,
        trackColor = Color.White.copy(alpha = 0.24f),
    )
}

@Composable
private fun PlayerActionIconButton(
    icon: ImageVector,
    contentDescription: String,
    interactionConfig: PlayerInteractionConfig,
    onUserInteraction: () -> Unit,
    selected: Boolean = false,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            onUserInteraction()
            onClick()
        },
        modifier = Modifier
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .graphicsLayer(
                scaleX = if (isFocused) 1.08f else 1f,
                scaleY = if (isFocused) 1.08f else 1f,
            )
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onUserInteraction()
                }
            }
            .background(
                color = when {
                    selected -> Color(0x337FE8FF)
                    isFocused -> Color.White.copy(alpha = 0.16f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(18.dp),
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) Color(0xFF7FE8FF) else Color.White,
        )
    }
}

@Composable
private fun PlaybackErrorOverlay(
    playbackError: PlayerPlaybackErrorState,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.widthIn(max = 360.dp),
        color = Color.Black.copy(alpha = 0.82f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlayerText(
                text = playbackError.phase.title(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            PlayerText(
                text = playbackError.message,
                color = Color(0xFFE4E7F2),
                style = MaterialTheme.typography.bodyMedium,
            )
            FilledTonalButton(
                onClick = onReplay,
                modifier = Modifier.defaultMinSize(minHeight = 40.dp),
            ) {
                PlayerText(text = "Replay")
            }
        }
    }
}

@Composable
private fun RailButton(
    label: String,
    interactionConfig: PlayerInteractionConfig,
    onUserInteraction: () -> Unit,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    FilledTonalButton(
        onClick = {
            onUserInteraction()
            onClick()
        },
        modifier = Modifier
            .defaultMinSize(minHeight = 40.dp)
            .graphicsLayer(
                scaleX = if (isFocused) 1.04f else 1f,
                scaleY = if (isFocused) 1.04f else 1f,
            )
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onUserInteraction()
                }
            },
        border = if (isFocused) BorderStroke(2.dp, Color.White) else null,
    ) {
        PlayerText(text = label)
    }
}

@Composable
private fun PlayerTransportIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    interactionConfig: PlayerInteractionConfig,
    onUserInteraction: () -> Unit,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            if (!enabled) return@IconButton
            onUserInteraction()
            onClick()
        },
        enabled = enabled,
        modifier = Modifier
            .then(
                focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
            )
            .graphicsLayer(
                scaleX = if (isFocused) 1.08f else 1f,
                scaleY = if (isFocused) 1.08f else 1f,
            )
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onUserInteraction()
                }
            }
            .background(
                color = if (isFocused) Color.White.copy(alpha = 0.16f) else Color.Transparent,
                shape = RoundedCornerShape(18.dp),
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) Color.White else Color(0xFF7B8796),
        )
    }
}

@UnstableApi
@Composable
private fun CustomControllerOverlay(
    player: ExoPlayer,
    uiState: PlayerUiState,
    features: PlayerFeatures,
    interactionConfig: PlayerInteractionConfig,
    hostState: PlayerHostState,
    callbacks: PlayerCallbacks,
    experience: PlayerExperience,
    performanceConfig: PlayerPerformanceConfig,
    primaryFocusRequester: FocusRequester,
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentItem = uiState.currentItem
    val programInfo = currentItem?.programInfo
    val effectiveFeatures = features.resolveFor(uiState)
    val mediaButtonColors = playerMediaButtonColors()
    val canShowSeekBar =
        effectiveFeatures.showSeekBar && currentItem != null && currentItem.contentType != PlayerContentType.Live
    val secondaryText = when {
        uiState.isLive -> programInfo?.currentTitle ?: currentItem?.subtitle
        ?: currentItem?.description

        else -> currentItem?.subtitle ?: currentItem?.description
    }
    val tertiaryText = when {
        uiState.isLive -> listOfNotNull(
            programInfo?.nextTitle,
            programInfo?.startTimeText,
            programInfo?.endTimeText
        )
            .takeIf { it.isNotEmpty() }
            ?.joinToString("  •  ")

        else -> currentItem?.description
    }
    val transportSpacing = if (experience == PlayerExperience.AndroidTv) 12.dp else 8.dp

    Column(
        modifier = modifier
            .focusGroup()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black.copy(alpha = 0.70f))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (effectiveFeatures.showStreamDetails && currentItem != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = when {
                                uiState.isLive && uiState.atLiveEdge -> Color(0xFFEF5350)
                                uiState.isLive -> Color(0xFFFFB74D)
                                else -> Color(0xFF66BB6A)
                            },
                            modifier = Modifier.size(12.dp),
                        )
                        PlayerText(
                            text = when {
                                uiState.isLive && uiState.hasDvr -> "LIVE (DVR)"
                                uiState.isLive -> "LIVE"
                                else -> "VOD"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.Monospace,
                        )
                    }

                    PlayerText(
                        text = currentItem.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    programInfo?.channelName?.takeIf { it.isNotBlank() && it != currentItem.title }
                        ?.let { channelName ->
                            PlayerText(
                                text = channelName,
                                color = Color(0xFFD8D9E8),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                    secondaryText?.takeIf { it.isNotBlank() }?.let { text ->
                        PlayerText(
                            text = text,
                            color = Color(0xFFB9BDD1),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    tertiaryText?.takeIf { it.isNotBlank() }?.let { timingText ->
                        PlayerText(
                            text = timingText,
                            color = Color(0xFF9AA0B5),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    callbacks.onBack?.takeIf { effectiveFeatures.showBackButton }?.let { onBack ->
                        PlayerActionIconButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                            onClick = onBack,
                        )
                    }
                    if (features.showEpgAction) {
                        PlayerActionIconButton(
                            icon = Icons.Rounded.LiveTv,
                            contentDescription = "EPG",
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                        ) {
                            callbacks.onEpgClick?.invoke(uiState.currentItem)
                        }
                    }
                    if (effectiveFeatures.showSubtitles && uiState.availableSubtitleTracks.isNotEmpty()) {
                        PlayerActionIconButton(
                            icon = Icons.Rounded.Subtitles,
                            contentDescription = "Subtitles",
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                        ) {
                            hostState.openPanel(PlayerPanel.Subtitles)
                        }
                    }
                    if (effectiveFeatures.showAudioSelector && uiState.availableAudioTracks.isNotEmpty()) {
                        PlayerActionIconButton(
                            icon = Icons.Rounded.GraphicEq,
                            contentDescription = "Audio",
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                        ) {
                            hostState.openPanel(PlayerPanel.Audio)
                        }
                    }
                    if (effectiveFeatures.showQualitySelector && uiState.availableVideoTracks.isNotEmpty()) {
                        PlayerActionIconButton(
                            icon = Icons.Rounded.HighQuality,
                            contentDescription = "Quality",
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                        ) {
                            hostState.openPanel(PlayerPanel.Quality)
                        }
                    }
                    if (effectiveFeatures.showPlaybackSpeed) {
                        PlayerActionIconButton(
                            icon = Icons.Rounded.Speed,
                            contentDescription = "Speed",
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                        ) {
                            hostState.openPanel(PlayerPanel.Speed)
                        }
                    }
                    if (effectiveFeatures.showStatsForNerds) {
                        PlayerActionIconButton(
                            icon = Icons.Rounded.Analytics,
                            contentDescription = "Stats",
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                        ) {
                            hostState.openPanel(PlayerPanel.Stats)
                        }
                    }
                }
            }
        }

        if (canShowSeekBar) {
            TvScrubSlider(
                progressMs = uiState.positionMs,
                durationMs = uiState.durationMs,
                enabled = uiState.canSeek || uiState.hasDvr,
                interactionConfig = interactionConfig,
                primaryFocusRequester = primaryFocusRequester,
                onUserInteraction = onUserInteraction,
                onSeekTo = hostState::seekTo,
            )
        } else if (effectiveFeatures.showSeekBar && uiState.isLive && !uiState.hasDvr) {
            PlayerText(
                text = "Seeking unavailable for this live stream.",
                color = Color(0xFFCFD8E3),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(transportSpacing),
            ) {
                if (effectiveFeatures.showPreviousButton && uiState.canGoPrevious) {
                    PlayerTransportIconButton(
                        icon = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        onClick = hostState::previous,
                    )
                }
                if (effectiveFeatures.showRewindButton && uiState.canSeek) {
                    Media3SeekBackButton(
                        player = player,
                        modifier = Modifier.media3ControlModifier(
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                        ),
                        colors = mediaButtonColors,
                        onClick = {
                            onUserInteraction()
                            hostState.rewind()
                        },
                    )
                }
                if (effectiveFeatures.showPlayPauseButton) {
                    Media3PlayPauseButton(
                        player = player,
                        modifier = Modifier.media3ControlModifier(
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                            focusRequester = if (!canShowSeekBar) {
                                primaryFocusRequester
                            } else {
                                null
                            },
                        ),
                        colors = mediaButtonColors,
                        onClick = {
                            onUserInteraction()
                            hostState.togglePlayPause()
                        }
                    )
                }
                if (effectiveFeatures.showFastForwardButton && uiState.canSeek) {
                    Media3SeekForwardButton(
                        player = player,
                        modifier = Modifier.media3ControlModifier(
                            interactionConfig = interactionConfig,
                            onUserInteraction = onUserInteraction,
                        ),
                        colors = mediaButtonColors,
                        onClick = {
                            onUserInteraction()
                            hostState.fastForward()
                        },
                    )
                }
                if (effectiveFeatures.showNextButton && uiState.canGoNext) {
                    PlayerTransportIconButton(
                        icon = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        focusRequester = if (!canShowSeekBar && !effectiveFeatures.showPlayPauseButton) {
                            primaryFocusRequester
                        } else {
                            null
                        },
                        onClick = hostState::next,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (effectiveFeatures.showGoLiveButton && uiState.hasDvr) {
                    PlayerActionIconButton(
                        icon = if (uiState.atLiveEdge) {
                            Icons.Rounded.RadioButtonChecked
                        } else {
                            Icons.Rounded.LiveTv
                        },
                        contentDescription = if (uiState.atLiveEdge) "Live" else "Go live",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        selected = uiState.atLiveEdge,
                    ) {
                        hostState.jumpToLiveEdge()
                    }
                }
                if (effectiveFeatures.showShuffleButton) {
                    PlayerActionIconButton(
                        icon = if (uiState.shuffleEnabled) {
                            Icons.Rounded.ShuffleOn
                        } else {
                            Icons.Rounded.Shuffle
                        },
                        contentDescription = "Shuffle",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        selected = uiState.shuffleEnabled,
                    ) {
                        hostState.setShuffleEnabled(!uiState.shuffleEnabled)
                    }
                }
                if (effectiveFeatures.showLoopButton) {
                    PlayerActionIconButton(
                        icon = when (uiState.repeatMode) {
                            PlayerRepeatMode.Off -> Icons.Rounded.Repeat
                            PlayerRepeatMode.One -> Icons.Rounded.RepeatOneOn
                            PlayerRepeatMode.All -> Icons.Rounded.RepeatOn
                        },
                        contentDescription = "Loop",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        selected = uiState.repeatMode != PlayerRepeatMode.Off,
                    ) {
                        hostState.setRepeatMode(uiState.repeatMode.nextRepeatMode())
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultPanelContent(
    panel: PlayerPanel,
    uiState: PlayerUiState,
    hostState: PlayerHostState,
    telemetrySnapshot: LivePlaybackSnapshot?,
    experience: PlayerExperience,
    initialFocusRequester: FocusRequester,
    onDismiss: () -> Unit,
) {
    var nextFocusRequester: FocusRequester? = initialFocusRequester

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .widthIn(max = if (experience == PlayerExperience.AndroidTv) 360.dp else 480.dp),
        color = Color(0xFF11131A).copy(alpha = 0.96f),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .heightIn(max = if (experience == PlayerExperience.AndroidTv) 520.dp else 420.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlayerText(
                text = panel.title(),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
            )

            when (panel) {
                PlayerPanel.Subtitles -> {
                    SelectionButton(
                        label = "Off",
                        selected = uiState.selectedSubtitleTrackId == null,
                        focusRequester = nextFocusRequester,
                    ) {
                        hostState.disableSubtitles()
                        onDismiss()
                    }
                    nextFocusRequester = null
                    uiState.availableSubtitleTracks.forEach { track ->
                        SelectionButton(
                            label = track.label,
                            selected = track.isSelected,
                            focusRequester = nextFocusRequester,
                        ) {
                            hostState.selectSubtitleTrack(track.id)
                            onDismiss()
                        }
                        nextFocusRequester = null
                    }
                }

                PlayerPanel.Audio -> {
                    uiState.availableAudioTracks.forEach { track ->
                        SelectionButton(
                            label = track.label,
                            selected = track.isSelected,
                            focusRequester = nextFocusRequester,
                        ) {
                            hostState.selectAudioTrack(track.id)
                            onDismiss()
                        }
                        nextFocusRequester = null
                    }
                }

                PlayerPanel.Quality -> {
                    uiState.availableVideoTracks.forEach { track ->
                        SelectionButton(
                            label = track.label,
                            selected = track.isSelected,
                            focusRequester = nextFocusRequester,
                        ) {
                            hostState.selectVideoTrack(track.id)
                            onDismiss()
                        }
                        nextFocusRequester = null
                    }
                }

                PlayerPanel.Speed -> {
                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                        SelectionButton(
                            label = String.format(Locale.US, "%.2fx", speed).replace(".00", ""),
                            selected = uiState.playbackSpeed == speed,
                            focusRequester = nextFocusRequester,
                        ) {
                            hostState.setPlaybackSpeed(speed)
                            onDismiss()
                        }
                        nextFocusRequester = null
                    }
                }

                PlayerPanel.Stats -> {
                    StatsPanel(uiState = uiState, telemetrySnapshot = telemetrySnapshot)
                }

                PlayerPanel.None -> Unit
            }

            FilledTonalButton(
                onClick = onDismiss,
                modifier = Modifier.then(
                    nextFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
                ),
            ) {
                PlayerText(text = "Close")
            }
        }
    }
}

@Composable
private fun SelectionButton(
    label: String,
    selected: Boolean,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
    ) {
        PlayerText(
            text = if (selected) "$label  Selected" else label,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StatsPanel(
    uiState: PlayerUiState,
    telemetrySnapshot: LivePlaybackSnapshot?,
) {
    val statsLines = buildList {
        add("State" to uiState.playbackState.toPlaybackStateLabel())
        add("Position" to formatDurationClock(uiState.positionMs))
        add("Buffered" to formatDurationClock(uiState.bufferedPositionMs))
        add("Live Offset" to formatDurationClock(uiState.liveOffsetMs))
        add("Current Item" to (uiState.currentItem?.title ?: "N/A"))
        add("Playback Speed" to "${uiState.playbackSpeed}x")
        telemetrySnapshot?.let { snapshot ->
            add("Throughput" to formatKbps(snapshot.throughputKbps))
            add("Bitrate" to formatKbps(snapshot.currentBitrateKbps))
            add("Dropped Frames" to snapshot.droppedFrames.toString())
            add("Rebuffer Count" to snapshot.rebufferCount.toString())
            add("Rebuffer Duration" to formatDurationClock(snapshot.rebufferDurationMs))
            add("Protocol" to snapshot.protocol.name)
            add("Session ID" to snapshot.sessionId)
        }
    }

    statsLines.forEach { (label, value) ->
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            PlayerText(
                text = label,
                color = Color(0xFF9FA9B7),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
            )
            PlayerText(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun TvScrubSlider(
    progressMs: Long,
    durationMs: Long,
    enabled: Boolean,
    interactionConfig: PlayerInteractionConfig,
    primaryFocusRequester: FocusRequester,
    onUserInteraction: () -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeDurationMs = durationMs.coerceAtLeast(1L)
    var previewPositionMs by remember(progressMs, durationMs) {
        mutableLongStateOf(progressMs.coerceIn(0L, safeDurationMs))
    }
    var isScrubbing by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    var lastDirection by remember { mutableIntStateOf(0) }
    var ladderIndex by remember { mutableIntStateOf(0) }
    var lastScrubAtMs by remember { mutableLongStateOf(0L) }
    var scrubInteractionToken by remember { mutableIntStateOf(0) }
    val scrubConfig = interactionConfig.dpadScrubConfig

    LaunchedEffect(progressMs, durationMs) {
        if (!isScrubbing) {
            previewPositionMs = progressMs.coerceIn(0L, safeDurationMs)
        }
    }

    LaunchedEffect(isScrubbing, scrubInteractionToken, scrubConfig.resetAfterIdleMs) {
        if (!isScrubbing) return@LaunchedEffect
        delay(scrubConfig.resetAfterIdleMs.coerceAtLeast(200L))
        isScrubbing = false
        ladderIndex = 0
        lastDirection = 0
    }

    fun applyDpadScrub(direction: Int): Boolean {
        if (!enabled || !interactionConfig.enableDpadScrubbing) return false

        val now = SystemClock.elapsedRealtime()
        val idleElapsedMs = now - lastScrubAtMs
        val shouldReset = lastDirection != direction || idleElapsedMs > scrubConfig.resetAfterIdleMs
        ladderIndex = if (shouldReset) {
            0
        } else {
            (ladderIndex + 1).coerceAtMost(scrubConfig.stepLadderMs.lastIndex)
        }
        if (shouldReset) {
            isScrubbing = false
            previewPositionMs = progressMs.coerceIn(0L, safeDurationMs)
        }
        lastDirection = direction
        lastScrubAtMs = now
        scrubInteractionToken += 1

        val basePositionMs = if (shouldReset) {
            progressMs.coerceIn(0L, safeDurationMs)
        } else if (isScrubbing) {
            previewPositionMs
        } else {
            progressMs.coerceIn(0L, safeDurationMs)
        }
        val stepMs = scrubConfig.stepLadderMs.getOrElse(ladderIndex) {
            scrubConfig.stepLadderMs.lastOrNull() ?: 5_000L
        }
        previewPositionMs = (basePositionMs + (stepMs * direction)).coerceIn(0L, safeDurationMs)
        isScrubbing = true
        onUserInteraction()
        onSeekTo(previewPositionMs)
        return true
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Slider(
            value = previewPositionMs.toFloat() / safeDurationMs.toFloat(),
            onValueChange = { fraction ->
                if (!enabled || !interactionConfig.enableTouchGestures) return@Slider
                isScrubbing = true
                previewPositionMs = (safeDurationMs * fraction.coerceIn(0f, 1f)).toLong()
                scrubInteractionToken += 1
                onUserInteraction()
            },
            onValueChangeFinished = {
                if (!enabled || !interactionConfig.enableTouchGestures) return@Slider
                onSeekTo(previewPositionMs)
                onUserInteraction()
                isScrubbing = false
                ladderIndex = 0
                lastDirection = 0
            },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .then(Modifier.focusRequester(primaryFocusRequester))
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused) {
                        onUserInteraction()
                    }
                }
                .onPreviewKeyEvent { event ->
                    if (!enabled || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft -> applyDpadScrub(direction = -1)
                        Key.DirectionRight -> applyDpadScrub(direction = 1)
                        else -> false
                    }
                }
                .then(
                    if (interactionConfig.enableFocus) {
                        Modifier.focusable(enabled)
                    } else {
                        Modifier
                    }
                ),
            colors = SliderDefaults.colors(
                thumbColor = if (isFocused) Color.White else Color(0xFFE3F2FD),
                activeTrackColor = Color(0xFF7FE8FF),
                inactiveTrackColor = Color(0xFF324455),
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PlayerText(
                text = formatDurationClock(previewPositionMs),
                color = Color(0xFFCFD8E3),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
            PlayerText(
                text = formatDurationClock(durationMs),
                color = Color(0xFFCFD8E3),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

private fun Modifier.media3ControlModifier(
    interactionConfig: PlayerInteractionConfig,
    onUserInteraction: () -> Unit,
    focusRequester: FocusRequester? = null,
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    this
        .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
        .graphicsLayer(
            scaleX = if (isFocused) 1.08f else 1f,
            scaleY = if (isFocused) 1.08f else 1f,
        )
        .onFocusChanged {
            isFocused = it.isFocused
            if (it.isFocused) {
                onUserInteraction()
            }
        }
        .background(
            color = when {
                isFocused -> Color.White.copy(alpha = 0.16f)
                else -> Color.Transparent
            },
            shape = RoundedCornerShape(18.dp),
        )
}

private fun Key.isConfirmKey(): Boolean {
    return this == Key.DirectionCenter || this == Key.Enter || this == Key.NumPadEnter
}

private fun Key.isDirectionalKey(): Boolean {
    return this == Key.DirectionLeft ||
            this == Key.DirectionRight ||
            this == Key.DirectionUp ||
            this == Key.DirectionDown
}

@UnstableApi
private fun updateUiState(
    player: ExoPlayer,
    hostState: PlayerHostState,
    playlist: PlayerPlaylist,
) {
    val item = playlist.items.getOrNull(player.currentMediaItemIndex)
    val extraction = extractTracks(player)
    hostState.updateTrackTargets(
        subtitles = extraction.subtitleTargets,
        audios = extraction.audioTargets,
        videos = extraction.videoTargets,
    )

    val liveOffset = player.currentLiveOffset.takeIf { it != C.TIME_UNSET }
    val isLive =
        item?.contentType == PlayerContentType.Live || item?.contentType == PlayerContentType.Dvr
    val hasDvr = item?.contentType == PlayerContentType.Dvr
    val canSeek = when (item?.contentType) {
        PlayerContentType.Live -> false
        PlayerContentType.Dvr,
        PlayerContentType.Vod -> item.isSeekable

        null -> false
    }
    val hasCircularNavigation = playlist.circularNavigation && playlist.items.size > 1

    hostState.uiState = hostState.uiState.copy(
        playlist = playlist,
        currentItem = item,
        currentIndex = player.currentMediaItemIndex.coerceAtLeast(0),
        durationMs = player.duration.takeIf { it > 0L } ?: 0L,
        positionMs = player.currentPosition.coerceAtLeast(0L),
        bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
        isPlaying = player.isPlaying,
        playbackState = player.playbackState,
        isLoading = player.playbackState == Player.STATE_BUFFERING,
        isLive = isLive,
        hasDvr = hasDvr,
        atLiveEdge = when {
            hasDvr -> liveOffset?.let { it <= 5_000L } ?: false
            isLive -> true
            else -> false
        },
        liveOffsetMs = liveOffset,
        canSeek = canSeek,
        canGoNext = hasCircularNavigation || player.hasNextMediaItem(),
        canGoPrevious = hasCircularNavigation || player.hasPreviousMediaItem(),
        repeatMode = player.repeatMode.toPlayerRepeatMode(),
        shuffleEnabled = player.shuffleModeEnabled,
        playbackSpeed = player.playbackParameters.speed,
        availableSubtitleTracks = extraction.subtitleTracks,
        availableAudioTracks = extraction.audioTracks,
        availableVideoTracks = extraction.videoTracks,
        selectedSubtitleTrackId = extraction.subtitleTracks.firstOrNull { it.isSelected }?.id,
        selectedAudioTrackId = extraction.audioTracks.firstOrNull { it.isSelected }?.id,
        selectedVideoTrackId = extraction.videoTracks.firstOrNull { it.isSelected }?.id,
    )
}

@UnstableApi
private fun extractTracks(player: ExoPlayer): TrackExtractionResult {
    val subtitleTargets = linkedMapOf<String, PlayerTrackTarget>()
    val audioTargets = linkedMapOf<String, PlayerTrackTarget>()
    val videoTargets = linkedMapOf<String, PlayerTrackTarget>()
    val subtitleTracks = mutableListOf<PlayerSubtitleTrack>()
    val audioTracks = mutableListOf<PlayerAudioTrack>()
    val videoTracks = mutableListOf<PlayerVideoTrack>()
    val overrides = player.trackSelectionParameters.overrides.values.toList()

    player.currentTracks.groups.forEachIndexed { groupIndex, group ->
        when (group.type) {
            C.TRACK_TYPE_TEXT -> {
                repeat(group.length) { trackIndex ->
                    if (!group.isTrackSupported(trackIndex)) return@repeat
                    val format = group.getTrackFormat(trackIndex)
                    val id = "subtitle_${groupIndex}_$trackIndex"
                    subtitleTargets[id] = PlayerTrackTarget(group.mediaTrackGroup, trackIndex)
                    subtitleTracks += PlayerSubtitleTrack(
                        id = id,
                        label = buildSubtitleLabel(format, trackIndex),
                        language = format.language,
                        mimeType = format.sampleMimeType,
                        isDefault = format.selectionFlags and C.SELECTION_FLAG_DEFAULT != 0,
                        isSelected = group.isTrackSelected(trackIndex),
                    )
                }
            }

            C.TRACK_TYPE_AUDIO -> {
                repeat(group.length) { trackIndex ->
                    if (!group.isTrackSupported(trackIndex)) return@repeat
                    val format = group.getTrackFormat(trackIndex)
                    val id = "audio_${groupIndex}_$trackIndex"
                    audioTargets[id] = PlayerTrackTarget(group.mediaTrackGroup, trackIndex)
                    audioTracks += PlayerAudioTrack(
                        id = id,
                        label = buildAudioLabel(format, trackIndex),
                        language = format.language,
                        channels = format.channelCount.takeIf { it != Format.NO_VALUE },
                        isSelected = group.isTrackSelected(trackIndex),
                    )
                }
            }

            C.TRACK_TYPE_VIDEO -> {
                val override = overrides.firstOrNull { it.mediaTrackGroup == group.mediaTrackGroup }
                repeat(group.length) { trackIndex ->
                    if (!group.isTrackSupported(trackIndex)) return@repeat
                    val format = group.getTrackFormat(trackIndex)
                    val id = "video_${groupIndex}_$trackIndex"
                    videoTargets[id] = PlayerTrackTarget(group.mediaTrackGroup, trackIndex)
                    videoTracks += PlayerVideoTrack(
                        id = id,
                        label = buildVideoLabel(format, trackIndex),
                        width = format.width.takeIf { it != Format.NO_VALUE },
                        height = format.height.takeIf { it != Format.NO_VALUE },
                        bitrateKbps = format.bitrate.toKbpsFromBitsPerSecond(),
                        isAdaptive = false,
                        isSelected = override?.trackIndices?.contains(trackIndex) == true,
                    )
                }
            }
        }
    }

    val hasVideoOverride = videoTracks.any { it.isSelected }
    val normalizedVideoTracks = buildList {
        add(
            PlayerVideoTrack(
                id = PLAYER_VIDEO_TRACK_AUTO,
                label = "Auto",
                isAdaptive = true,
                isSelected = !hasVideoOverride,
            )
        )
        addAll(
            if (hasVideoOverride) {
                videoTracks
            } else {
                videoTracks.map { it.copy(isSelected = false) }
            }
        )
    }

    return TrackExtractionResult(
        subtitleTracks = subtitleTracks,
        audioTracks = audioTracks,
        videoTracks = normalizedVideoTracks,
        subtitleTargets = subtitleTargets,
        audioTargets = audioTargets,
        videoTargets = videoTargets,
    )
}

private fun PlayerItem.toMediaItem(): MediaItem {
    val builder = MediaItem.Builder()
        .setMediaId(id)
        .setUri(Uri.parse(streamUrl))
        .setMediaMetadata(
            androidx.media3.common.MediaMetadata.Builder()
                .setTitle(title)
                .build()
        )

    when (sourceType) {
        PlayerSourceType.Hls -> builder.setMimeType(MimeTypes.APPLICATION_M3U8)
        PlayerSourceType.Progressive -> Unit
    }

    if (subtitleTracks.isNotEmpty()) {
        val hasDefaultSubtitle = subtitleTracks.any(PlayerSubtitleTrack::isDefault)
        builder.setSubtitleConfigurations(
            subtitleTracks.mapIndexedNotNull { index, track ->
                val trackUrl =
                    track.url?.takeIf(String::isNotBlank) ?: return@mapIndexedNotNull null
                MediaItem.SubtitleConfiguration.Builder(Uri.parse(trackUrl))
                    .setMimeType(track.mimeType ?: inferSubtitleMimeType(trackUrl))
                    .setLanguage(track.language)
                    .setLabel(track.label)
                    .setSelectionFlags(
                        if (track.isDefault || (!hasDefaultSubtitle && index == 0)) {
                            C.SELECTION_FLAG_DEFAULT
                        } else {
                            0
                        }
                    )
                    .build()
            }
        )
    }

    return builder.build()
}

private fun inferSubtitleMimeType(url: String): String {
    val normalizedUrl = url.substringBefore('?').substringBefore('#').lowercase(Locale.ROOT)
    return when {
        normalizedUrl.endsWith(".vtt") -> MimeTypes.TEXT_VTT
        normalizedUrl.endsWith(".ttml") || normalizedUrl.endsWith(".dfxp") -> MimeTypes.APPLICATION_TTML
        normalizedUrl.endsWith(".ssa") || normalizedUrl.endsWith(".ass") -> MimeTypes.TEXT_SSA
        else -> MimeTypes.APPLICATION_SUBRIP
    }
}

@Composable
private fun rememberTelemetryCollector(
    telemetryConfig: PlayerTelemetryConfig,
): PlaybackTelemetryCollector {
    val providedCollector = telemetryConfig.collector
    return remember(telemetryConfig.enabled, providedCollector) {
        when {
            !telemetryConfig.enabled -> NoOpPlaybackTelemetryCollector()
            providedCollector == null -> DefaultPlaybackTelemetryCollector()
            providedCollector is NoOpPlaybackTelemetryCollector -> DefaultPlaybackTelemetryCollector()
            else -> providedCollector
        }
    }
}

private fun PlayerRepeatMode.toUnifiedMedia3RepeatMode(): Int {
    return when (this) {
        PlayerRepeatMode.Off -> Player.REPEAT_MODE_OFF
        PlayerRepeatMode.One -> Player.REPEAT_MODE_ONE
        PlayerRepeatMode.All -> Player.REPEAT_MODE_ALL
    }
}

private fun ensureTelemetrySession(
    player: ExoPlayer,
    playlist: PlayerPlaylist,
    collector: PlaybackTelemetryCollector,
    currentSessionId: String?,
    onSessionIdCreated: (String) -> Unit,
) {
    if (currentSessionId != null) return
    val item = playlist.items.getOrNull(player.currentMediaItemIndex) ?: return
    val sessionId = UUID.randomUUID().toString()
    collector.startSession(
        sessionId = sessionId,
        streamUrl = item.streamUrl,
    )
    onSessionIdCreated(sessionId)
}

private fun finishTelemetrySession(collector: PlaybackTelemetryCollector) {
    collector.endSession()
    collector.reset()
}

@UnstableApi
private fun selectedTrackSnapshotFromTracks(tracks: Tracks): TrackSnapshot? {
    var videoFormat: Format? = null
    var audioFormat: Format? = null

    tracks.groups.forEach { group ->
        repeat(group.length) { index ->
            if (!group.isTrackSelected(index)) return@repeat
            val format = group.getTrackFormat(index)
            when (group.type) {
                C.TRACK_TYPE_VIDEO -> videoFormat = format
                C.TRACK_TYPE_AUDIO -> audioFormat = format
            }
        }
    }

    if (videoFormat == null && audioFormat == null) return null

    return TrackSnapshot(
        timestampMs = System.currentTimeMillis(),
        videoCodec = videoFormat?.codecs,
        videoMimeType = videoFormat?.sampleMimeType,
        width = videoFormat?.width?.takeIf { it != Format.NO_VALUE },
        height = videoFormat?.height?.takeIf { it != Format.NO_VALUE },
        frameRate = videoFormat?.frameRate?.takeIf { it != Format.NO_VALUE.toFloat() },
        videoBitrateKbps = videoFormat?.bitrate.toKbpsFromBitsPerSecond(),
        audioCodec = audioFormat?.codecs,
        audioMimeType = audioFormat?.sampleMimeType,
        audioBitrateKbps = audioFormat?.bitrate.toKbpsFromBitsPerSecond(),
        audioChannels = audioFormat?.channelCount?.takeIf { it != Format.NO_VALUE },
        audioSampleRateHz = audioFormat?.sampleRate?.takeIf { it != Format.NO_VALUE },
        drmType = videoFormat?.drmInitData?.schemeDataCount?.takeIf { it > 0 }?.toString(),
    )
}

private fun buildSubtitleLabel(format: Format, index: Int): String {
    return format.label
        ?: format.language?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        ?: "Subtitle ${index + 1}"
}

private fun buildAudioLabel(format: Format, index: Int): String {
    val base = format.label
        ?: format.language?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        ?: "Audio ${index + 1}"
    val channels =
        format.channelCount.takeIf { it != Format.NO_VALUE }?.let { " • ${it}ch" }.orEmpty()
    return base + channels
}

@UnstableApi
private fun buildVideoLabel(format: Format, index: Int): String {
    val resolution = listOfNotNull(
        format.height.takeIf { it != Format.NO_VALUE }?.let { "${it}p" },
        format.width.takeIf { it != Format.NO_VALUE }?.let { "${it}w" },
    ).firstOrNull()
    val bitrate = format.bitrate.toKbpsFromBitsPerSecond()?.let { " • ${it}kbps" }.orEmpty()
    return resolution?.plus(bitrate) ?: "Quality ${index + 1}$bitrate"
}

private fun Int.toTelemetryPlaybackState(): com.kabindra.player.player.telemetry.model.PlaybackState {
    return when (this) {
        Player.STATE_IDLE -> com.kabindra.player.player.telemetry.model.PlaybackState.IDLE
        Player.STATE_BUFFERING -> com.kabindra.player.player.telemetry.model.PlaybackState.BUFFERING
        Player.STATE_READY -> com.kabindra.player.player.telemetry.model.PlaybackState.READY
        Player.STATE_ENDED -> com.kabindra.player.player.telemetry.model.PlaybackState.ENDED
        else -> com.kabindra.player.player.telemetry.model.PlaybackState.UNKNOWN
    }
}

private fun Int.toPlayerRepeatMode(): PlayerRepeatMode {
    return when (this) {
        Player.REPEAT_MODE_ONE -> PlayerRepeatMode.One
        Player.REPEAT_MODE_ALL -> PlayerRepeatMode.All
        else -> PlayerRepeatMode.Off
    }
}

private fun PlayerRepeatMode.nextRepeatMode(): PlayerRepeatMode {
    return when (this) {
        PlayerRepeatMode.Off -> PlayerRepeatMode.One
        PlayerRepeatMode.One -> PlayerRepeatMode.All
        PlayerRepeatMode.All -> PlayerRepeatMode.Off
    }
}

private fun PlayerPanel.title(): String {
    return when (this) {
        PlayerPanel.None -> ""
        PlayerPanel.Quality -> "Video Quality"
        PlayerPanel.Subtitles -> "Subtitles"
        PlayerPanel.Audio -> "Audio Tracks"
        PlayerPanel.Speed -> "Playback Speed"
        PlayerPanel.Stats -> "Stats for Nerds"
    }
}

private fun PlayerPlaybackErrorPhase.title(): String {
    return when (this) {
        PlayerPlaybackErrorPhase.Initial -> "Playback Error"
        PlayerPlaybackErrorPhase.Switching -> "Channel Switch Failed"
        PlayerPlaybackErrorPhase.Replay -> "Replay Failed"
    }
}

private fun formatDurationClock(valueMs: Long?): String {
    if (valueMs == null || valueMs <= 0L) return "00:00"
    val totalSeconds = valueMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun formatKbps(value: Int?): String {
    return value?.let { "${it} kbps" } ?: "N/A"
}

private fun Int?.toKbpsFromBitsPerSecond(): Int? {
    return this?.takeIf { it > 0 }?.div(1000)
}

private fun Long.toKbpsFromBitsPerSecond(): Int? {
    return takeIf { it > 0 }?.div(1000L)?.toInt()
}

private fun calculateThroughputKbps(bytesLoaded: Long, loadDurationMs: Long): Int? {
    if (bytesLoaded <= 0L || loadDurationMs <= 0L) return null
    return ((bytesLoaded * 8.0) / loadDurationMs.toDouble()).roundToInt().coerceAtLeast(0)
}

private fun Int.toPlaybackStateLabel(): String {
    return when (this) {
        Player.STATE_IDLE -> "IDLE"
        Player.STATE_BUFFERING -> "BUFFERING"
        Player.STATE_READY -> "READY"
        Player.STATE_ENDED -> "ENDED"
        else -> "UNKNOWN"
    }
}

private data class TrackExtractionResult(
    val subtitleTracks: List<PlayerSubtitleTrack>,
    val audioTracks: List<PlayerAudioTrack>,
    val videoTracks: List<PlayerVideoTrack>,
    val subtitleTargets: Map<String, PlayerTrackTarget>,
    val audioTargets: Map<String, PlayerTrackTarget>,
    val videoTargets: Map<String, PlayerTrackTarget>,
)

@UnstableApi
private data class TransferSample(
    val startedAtMs: Long,
    val dataSpec: DataSpec,
    val transferredBytes: Long = 0L,
)
