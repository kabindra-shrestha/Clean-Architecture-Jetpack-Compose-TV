package com.kabindra.player

import android.net.Uri
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.ui.PlayerView
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
                        throughputKbps = calculateThroughputKbps(sample.transferredBytes, durationMs),
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
        playerView.player = player
        playerView.useController = controllerMode == PlayerControllerMode.Default
        playerView.controllerAutoShow = controllerMode == PlayerControllerMode.Default
        playerView.keepScreenOn = performanceConfig.keepScreenOn
        player.videoScalingMode = performanceConfig.videoScalingMode
        hostState.attach(player, playerView, performanceConfig)
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
                Lifecycle.Event.ON_START -> if (currentPlaylist.autoPlay) player.playWhenReady = true
                Lifecycle.Event.ON_STOP -> player.playWhenReady = false
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
                hostState.uiState = hostState.uiState.copy(errorMessage = error.localizedMessage)
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
            override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) {
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

        val mediaItems = playlist.items.map(PlayerItem::toMediaItem)
        player.setMediaItems(
            mediaItems,
            playlist.startIndex.coerceIn(0, playlist.items.lastIndex),
            0L,
        )
        player.prepare()
        player.playWhenReady = playlist.autoPlay
        updateUiState(player, hostState, playlist)
        if (telemetryConfig.enabled) {
            finishTelemetrySession(currentTelemetryCollector)
            telemetrySessionId = null
            ensureTelemetrySession(player, playlist, currentTelemetryCollector, telemetrySessionId) {
                telemetrySessionId = it
            }
        }
    }

    LaunchedEffect(playlist.startIndex) {
        if (playlist.items.isEmpty()) return@LaunchedEffect
        val targetIndex = playlist.startIndex.coerceIn(0, playlist.items.lastIndex)
        if (player.currentMediaItemIndex != targetIndex) {
            player.seekToDefaultPosition(targetIndex)
        }
        if (playlist.autoPlay) {
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
                currentTelemetryCollector.currentSnapshot()?.let(currentCallbacks.onTelemetrySnapshot ?: {})
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
        uiState.errorMessage,
        controllerInteractionToken,
    ) {
        if (controllerMode != PlayerControllerMode.Custom) return@LaunchedEffect
        if (!currentInteractionConfig.autoHideController) return@LaunchedEffect
        if (!uiState.isControllerVisible) return@LaunchedEffect
        if (uiState.activePanel != PlayerPanel.None) return@LaunchedEffect
        if (uiState.isLoading) return@LaunchedEffect
        if (!uiState.errorMessage.isNullOrBlank()) return@LaunchedEffect

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

        if (uiState.isControllerVisible || uiState.activePanel != PlayerPanel.None) {
            runCatching { primaryControlFocusRequester.requestFocus() }
        } else {
            runCatching { hiddenOverlayFocusRequester.requestFocus() }
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(currentInteractionConfig.showControllerOnTap) {
                if (!currentInteractionConfig.showControllerOnTap) return@pointerInput
                detectTapGestures { registerInteraction() }
            }
            .onPreviewKeyEvent { event ->
                if (!currentInteractionConfig.showControllerOnKeyPress) return@onPreviewKeyEvent false
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                if (!hostState.uiState.isControllerVisible && controllerMode == PlayerControllerMode.Custom) {
                    registerInteraction()
                    true
                } else {
                    false
                }
            }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { playerView },
            update = { view ->
                view.player = player
                view.useController = controllerMode == PlayerControllerMode.Default
                view.controllerAutoShow = controllerMode == PlayerControllerMode.Default
                view.keepScreenOn = performanceConfig.keepScreenOn
            },
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                        registerInteraction()
                        true
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

        AnimatedVisibility(
            visible = controllerMode == PlayerControllerMode.Custom && uiState.isControllerVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CustomControllerOverlay(
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

        AnimatedVisibility(
            visible = uiState.activePanel != PlayerPanel.None,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(
                if (experience == PlayerExperience.AndroidTv) Alignment.CenterEnd
                else Alignment.BottomCenter
            ),
        ) {
            val dismiss = hostState::dismissPanel
            currentPanelContent?.invoke(uiState.activePanel, uiState, hostState, dismiss)
                ?: DefaultPanelContent(
                    panel = uiState.activePanel,
                    uiState = uiState,
                    hostState = hostState,
                    telemetrySnapshot = if (telemetryConfig.enabled) currentTelemetryCollector.currentSnapshot() else null,
                    experience = experience,
                    onDismiss = dismiss,
                )
        }

        uiState.errorMessage?.takeIf { it.isNotBlank() }?.let { errorMessage ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.72f))
                    .padding(24.dp)
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
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
            }
            .then(
                if (interactionConfig.enableFocus) {
                    Modifier.focusable()
                } else {
                    Modifier
                }
            ),
        border = if (isFocused) BorderStroke(2.dp, Color.White) else null,
    ) {
        Text(text = label)
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
            .then(
                if (interactionConfig.enableFocus) {
                    Modifier.focusable()
                } else {
                    Modifier
                }
            )
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
    var scrubbingPositionMs by remember(uiState.positionMs, uiState.durationMs) {
        mutableLongStateOf(uiState.positionMs)
    }
    var isScrubbing by remember { mutableStateOf(false) }
    val duration = uiState.durationMs.coerceAtLeast(1L)
    val currentItem = uiState.currentItem
    val programInfo = currentItem?.programInfo

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black.copy(alpha = 0.70f))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (features.showStreamDetails && currentItem != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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

                    listOfNotNull(programInfo?.startTimeText, programInfo?.endTimeText)
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString(" - ")
                        ?.let { timingText ->
                            Text(
                                text = timingText,
                                color = Color(0xFF9AA0B5),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    callbacks.onBack?.takeIf { features.showBackButton }?.let { onBack ->
                        RailButton("Back", interactionConfig, onUserInteraction, onBack)
                    }
                    if (features.showEpgAction) {
                        RailButton("EPG", interactionConfig, onUserInteraction) {
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
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!features.showStreamDetails) {
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
            } else {
                Spacer(modifier = Modifier)
            }
        }

        if (features.showSeekBar && ((uiState.canSeek && !uiState.isLive) || uiState.hasDvr)) {
            ExpressiveWavySeekBar(
                progressFraction = (if (isScrubbing) scrubbingPositionMs else uiState.positionMs)
                    .toFloat() / duration.toFloat(),
                enabled = uiState.canSeek,
                interactionConfig = interactionConfig,
                stepBackwardFraction = (performanceConfig.seekBackMs.toFloat() / duration.toFloat())
                    .coerceIn(0.01f, 1f),
                stepForwardFraction = (performanceConfig.seekForwardMs.toFloat() / duration.toFloat())
                    .coerceIn(0.01f, 1f),
                onScrubStart = { isScrubbing = true },
                onScrubUpdate = { fraction ->
                    onUserInteraction()
                    scrubbingPositionMs = (duration * fraction).toLong().coerceIn(0L, duration)
                },
                onScrubEnd = { fraction ->
                    onUserInteraction()
                    hostState.seekTo((duration * fraction).toLong().coerceIn(0L, duration))
                    isScrubbing = false
                },
                onStepBackward = onUserInteraction,
                onStepForward = onUserInteraction,
                focusRequester = primaryFocusRequester,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDurationClock(if (isScrubbing) scrubbingPositionMs else uiState.positionMs),
                    color = Color(0xFFCFD8E3),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = if (uiState.hasDvr) {
                        "DVR ${formatDurationClock(uiState.durationMs)}"
                    } else {
                        formatDurationClock(uiState.durationMs)
                    },
                    color = Color(0xFFCFD8E3),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        if (features.showSeekBar && uiState.isLive && !uiState.canSeek && !uiState.hasDvr) {
            Text(
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
                horizontalArrangement = Arrangement.spacedBy(if (experience == PlayerExperience.AndroidTv) 12.dp else 8.dp),
            ) {
                if (features.showPreviousButton && uiState.canGoPrevious) {
                    PlayerTransportIconButton(
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        onClick = hostState::previous,
                    )
                }
                if (features.showRewindButton) {
                    PlayerTransportIconButton(
                        icon = Icons.Default.Replay10,
                        contentDescription = "Rewind",
                        enabled = uiState.canSeek,
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        onClick = hostState::rewind,
                    )
                }
                if (features.showPlayPauseButton) {
                    PlayerTransportIconButton(
                        icon = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play pause",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        focusRequester = if (!features.showSeekBar || !uiState.canSeek) {
                            primaryFocusRequester
                        } else {
                            null
                        },
                        onClick = hostState::togglePlayPause,
                    )
                }
                if (features.showFastForwardButton) {
                    PlayerTransportIconButton(
                        icon = Icons.Default.Forward10,
                        contentDescription = "Fast forward",
                        enabled = uiState.canSeek,
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        onClick = hostState::fastForward,
                    )
                }
                if (features.showNextButton && uiState.canGoNext) {
                    PlayerTransportIconButton(
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                        onClick = hostState::next,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (features.showGoLiveButton && uiState.isLive) {
                    RailButton(
                        label = if (uiState.atLiveEdge) "LIVE" else "GO LIVE",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                    ) {
                        hostState.jumpToLiveEdge()
                    }
                }
                if (features.showShuffleButton) {
                    RailButton(
                        label = if (uiState.shuffleEnabled) "Shuffle On" else "Shuffle Off",
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
                    ) {
                        hostState.setShuffleEnabled(!uiState.shuffleEnabled)
                    }
                }
                if (features.showLoopButton) {
                    RailButton(
                        label = when (uiState.repeatMode) {
                            PlayerRepeatMode.Off -> "Loop Off"
                            PlayerRepeatMode.One -> "Loop One"
                            PlayerRepeatMode.All -> "Loop All"
                        },
                        interactionConfig = interactionConfig,
                        onUserInteraction = onUserInteraction,
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
    onDismiss: () -> Unit,
) {
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
            Text(
                text = panel.title(),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
            )

            when (panel) {
                PlayerPanel.Subtitles -> {
                    SelectionButton(
                        label = "Off",
                        selected = uiState.selectedSubtitleTrackId == null,
                    ) {
                        hostState.disableSubtitles()
                        onDismiss()
                    }
                    uiState.availableSubtitleTracks.forEach { track ->
                        SelectionButton(label = track.label, selected = track.isSelected) {
                            hostState.selectSubtitleTrack(track.id)
                            onDismiss()
                        }
                    }
                }

                PlayerPanel.Audio -> {
                    uiState.availableAudioTracks.forEach { track ->
                        SelectionButton(label = track.label, selected = track.isSelected) {
                            hostState.selectAudioTrack(track.id)
                            onDismiss()
                        }
                    }
                }

                PlayerPanel.Quality -> {
                    uiState.availableVideoTracks.forEach { track ->
                        SelectionButton(label = track.label, selected = track.isSelected) {
                            hostState.selectVideoTrack(track.id)
                            onDismiss()
                        }
                    }
                }

                PlayerPanel.Speed -> {
                    listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                        SelectionButton(
                            label = String.format(Locale.US, "%.2fx", speed).replace(".00", ""),
                            selected = uiState.playbackSpeed == speed,
                        ) {
                            hostState.setPlaybackSpeed(speed)
                            onDismiss()
                        }
                    }
                }

                PlayerPanel.Stats -> {
                    StatsPanel(uiState = uiState, telemetrySnapshot = telemetrySnapshot)
                }

                PlayerPanel.None -> Unit
            }

            FilledTonalButton(onClick = onDismiss) {
                Text(text = "Close")
            }
        }
    }
}

@Composable
private fun SelectionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
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
            Text(
                text = label,
                color = Color(0xFF9FA9B7),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveWavySeekBar(
    progressFraction: Float,
    enabled: Boolean,
    interactionConfig: PlayerInteractionConfig,
    stepBackwardFraction: Float,
    stepForwardFraction: Float,
    onScrubStart: () -> Unit,
    onScrubUpdate: (Float) -> Unit,
    onScrubEnd: (Float) -> Unit,
    onStepBackward: () -> Unit,
    onStepForward: () -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    var dragFraction by remember { mutableFloatStateOf(progressFraction.coerceIn(0f, 1f)) }
    var isDragging by remember { mutableStateOf(false) }
    val visibleProgress = if (isDragging) dragFraction else progressFraction.coerceIn(0f, 1f)
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
            )
            .background(if (isFocused) Color(0x33303B4A) else Color(0x22303B4A))
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (interactionConfig.enableFocus) {
                    Modifier
                        .focusable(enabled)
                        .onPreviewKeyEvent { event ->
                            if (!enabled || event.type != KeyEventType.KeyDown) {
                                return@onPreviewKeyEvent false
                            }

                            when (event.key) {
                                Key.DirectionLeft -> {
                                    val nextFraction =
                                        (visibleProgress - stepBackwardFraction).coerceIn(0f, 1f)
                                    dragFraction = nextFraction
                                    onScrubStart()
                                    onScrubUpdate(nextFraction)
                                    onScrubEnd(nextFraction)
                                    onStepBackward()
                                    true
                                }

                                Key.DirectionRight -> {
                                    val nextFraction =
                                        (visibleProgress + stepForwardFraction).coerceIn(0f, 1f)
                                    dragFraction = nextFraction
                                    onScrubStart()
                                    onScrubUpdate(nextFraction)
                                    onScrubEnd(nextFraction)
                                    onStepForward()
                                    true
                                }

                                else -> false
                            }
                        }
                } else {
                    Modifier
                }
            )
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(enabled, widthPx, interactionConfig.enableTouchGestures) {
                if (!enabled || !interactionConfig.enableTouchGestures) return@pointerInput
                detectTapGestures(
                    onTap = { offset ->
                        val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                        isDragging = true
                        dragFraction = fraction
                        onScrubStart()
                        onScrubUpdate(fraction)
                        onScrubEnd(fraction)
                        isDragging = false
                    }
                )
            }
            .pointerInput(enabled, widthPx, interactionConfig.enableTouchGestures) {
                if (!enabled || !interactionConfig.enableTouchGestures) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        val fraction = (offset.x / widthPx).coerceIn(0f, 1f)
                        isDragging = true
                        dragFraction = fraction
                        onScrubStart()
                        onScrubUpdate(fraction)
                    },
                    onDrag = { change, _ ->
                        val fraction = (change.position.x / widthPx).coerceIn(0f, 1f)
                        dragFraction = fraction
                        onScrubUpdate(fraction)
                    },
                    onDragEnd = {
                        onScrubEnd(dragFraction.coerceIn(0f, 1f))
                        isDragging = false
                    },
                    onDragCancel = {
                        onScrubEnd(dragFraction.coerceIn(0f, 1f))
                        isDragging = false
                    },
                )
            },
    ) {
        LinearWavyProgressIndicator(
            progress = { visibleProgress },
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            color = Color(0xFF7FE8FF),
            trackColor = if (isFocused) Color(0xFF46576B) else Color(0xFF2C3B4C),
        )
    }
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
    val isLive = item?.contentType == PlayerContentType.Live || item?.contentType == PlayerContentType.Dvr
    val hasDvr = item?.contentType == PlayerContentType.Dvr || (isLive && player.isCurrentMediaItemSeekable)
    val canSeek = (item?.isSeekable != false) && (player.isCurrentMediaItemSeekable || !isLive || hasDvr)

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
        atLiveEdge = liveOffset?.let { it <= 5_000L } ?: !isLive,
        liveOffsetMs = liveOffset,
        canSeek = canSeek,
        canGoNext = player.hasNextMediaItem(),
        canGoPrevious = player.hasPreviousMediaItem(),
        repeatMode = player.repeatMode.toPlayerRepeatMode(),
        shuffleEnabled = player.shuffleModeEnabled,
        playbackSpeed = player.playbackParameters.speed,
        availableSubtitleTracks = extraction.subtitleTracks,
        availableAudioTracks = extraction.audioTracks,
        availableVideoTracks = extraction.videoTracks,
        selectedSubtitleTrackId = extraction.subtitleTracks.firstOrNull { it.isSelected }?.id,
        selectedAudioTrackId = extraction.audioTracks.firstOrNull { it.isSelected }?.id,
        selectedVideoTrackId = extraction.videoTracks.firstOrNull { it.isSelected }?.id,
        errorMessage = null,
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
        builder.setSubtitleConfigurations(
            subtitleTracks.mapNotNull { track ->
                val trackUrl = track.url ?: return@mapNotNull null
                MediaItem.SubtitleConfiguration.Builder(Uri.parse(trackUrl))
                    .setMimeType(track.mimeType)
                    .setLanguage(track.language)
                    .setLabel(track.label)
                    .setSelectionFlags(if (track.isDefault) C.SELECTION_FLAG_DEFAULT else 0)
                    .build()
            }
        )
    }

    return builder.build()
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
    val channels = format.channelCount.takeIf { it != Format.NO_VALUE }?.let { " • ${it}ch" }.orEmpty()
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
