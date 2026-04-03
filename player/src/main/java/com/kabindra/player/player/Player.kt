package com.kabindra.player.player

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
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
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING
import com.kabindra.player.R
import com.kabindra.player.player.statsui.StatsForNerdsState
import com.kabindra.player.player.telemetry.collector.DefaultPlaybackTelemetryCollector
import com.kabindra.player.player.telemetry.model.LivePlaybackSnapshot
import com.kabindra.player.player.telemetry.model.NetworkSample
import com.kabindra.player.player.telemetry.model.PlaybackIssue
import com.kabindra.player.player.telemetry.model.PlaybackState
import com.kabindra.player.player.telemetry.model.TrackSnapshot
import com.kabindra.player.player.util.ExoPlayerErrorUtils.errorHandler
import com.kabindra.player.player.util.ExoPlayerUtils.initializeExoPlayer
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.IOException
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

/**
 * Created by Kabindra Shrestha on 8/27/2022.
 */
/**
 * Video player
 *
 * @param videoURL
 */
@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun Player(
    videoURL: String,
    showStatsForNerds: Boolean,
    playbackRequestToken: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        KeepScreenOn()

        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val exoPlayerErrorPrefix = stringResource(id = R.string.exo_player_error_prefix_code)
        val exoPlayerUnknownPrefix = stringResource(id = R.string.exo_player_unknown_exception_code)

        var visibilityError by remember { mutableStateOf(false) }
        var visibilityProgressBar by remember { mutableStateOf(false) }

        var textError by remember { mutableStateOf("") }
        var statsForNerdsState by remember(videoURL, playbackRequestToken) {
            mutableStateOf(
                StatsForNerdsState(
                    isVisible = false,
                    streamUrl = videoURL
                )
            )
        }
        val connectionSpeedSeries =
            remember(videoURL, playbackRequestToken) { mutableStateListOf<Float>() }
        val bufferHealthSeries =
            remember(videoURL, playbackRequestToken) { mutableStateListOf<Float>() }
        val networkActivitySeries =
            remember(videoURL, playbackRequestToken) { mutableStateListOf<Float>() }
        val droppedFramesSeries =
            remember(videoURL, playbackRequestToken) { mutableStateListOf<Float>() }
        val rollingTransferSamples =
            remember(videoURL, playbackRequestToken) { mutableListOf<WindowedTransferSample>() }
        var statsSeriesVersion by remember(videoURL, playbackRequestToken) { mutableIntStateOf(0) }
        val networkTransferredBytes = remember(videoURL, playbackRequestToken) { AtomicLong(0L) }
        var previousNetworkTotalBytes by remember(
            videoURL,
            playbackRequestToken
        ) { mutableLongStateOf(0L) }
        var previousNetworkSampleTimestampMs by remember(
            videoURL,
            playbackRequestToken
        ) { mutableLongStateOf(0L) }
        var controllerState by remember(videoURL, playbackRequestToken) {
            mutableStateOf(
                PlayerControllerState()
            )
        }
        var isScrubbing by remember(videoURL, playbackRequestToken) { mutableStateOf(false) }
        var scrubbingPositionMs by remember(
            videoURL,
            playbackRequestToken
        ) { mutableLongStateOf(0L) }

        val telemetryCollector = remember { DefaultPlaybackTelemetryCollector() }
        val transferSamples = remember { mutableMapOf<Int, TransferSample>() }

        val finishTelemetrySession: (String) -> Unit = { reason ->
            val report = telemetryCollector.endSession(System.currentTimeMillis())
            if (report != null) {
                println(
                    "telemetry_session_end reason=$reason sessionId=${report.sessionId} protocol=${report.protocol} startupMs=${report.kpiSummary.startupMs} firstFrameMs=${report.kpiSummary.firstFrameMs} rebufferCount=${report.kpiSummary.rebufferCount} rebufferDurationMs=${report.kpiSummary.rebufferDurationMs} droppedFrames=${report.kpiSummary.droppedFrames} bitrateSwitches=${report.kpiSummary.bitrateSwitchCount} avgBitrateKbps=${report.kpiSummary.averageBitrateKbps} avgThroughputKbps=${report.kpiSummary.averageThroughputKbps} fatalErrors=${report.kpiSummary.fatalErrorCount} networkSamples=${report.networkSamples.size} trackSnapshots=${report.trackSnapshots.size}"
                )
            }
        }

        val telemetryTransferListener = remember {
            object : TransferListener {
                override fun onTransferInitializing(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean
                ) = Unit

                override fun onTransferStart(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean
                ) {
                    if (!isNetwork) return
                    val transferKey = System.identityHashCode(dataSpec)
                    transferSamples[transferKey] =
                        TransferSample(
                            dataSpec = dataSpec,
                            startedAtMs = System.currentTimeMillis(),
                            transferredBytes = 0L
                        )
                }

                override fun onBytesTransferred(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean,
                    bytesTransferred: Int
                ) {
                    if (!isNetwork || bytesTransferred <= 0) return
                    networkTransferredBytes.addAndGet(bytesTransferred.toLong())

                    val transferKey = System.identityHashCode(dataSpec)
                    val currentSample = transferSamples[transferKey] ?: return
                    transferSamples[transferKey] = currentSample.copy(
                        transferredBytes = currentSample.transferredBytes + bytesTransferred
                    )
                }

                override fun onTransferEnd(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean
                ) {
                    if (!isNetwork) return

                    val transferKey = System.identityHashCode(dataSpec)
                    val sample = transferSamples.remove(transferKey) ?: return
                    val timestampMs = System.currentTimeMillis()
                    val durationMs = (timestampMs - sample.startedAtMs).coerceAtLeast(1L)

                    telemetryCollector.appendNetworkSample(
                        NetworkSample(
                            timestampMs = timestampMs,
                            throughputKbps = calculateThroughputKbps(
                                sample.transferredBytes,
                                durationMs
                            ),
                            responseCode = null,
                            host = dataSpec.uri.host,
                            segmentUri = dataSpec.uri.toString(),
                            transferBytes = sample.transferredBytes,
                            transferDurationMs = durationMs
                        )
                    )
                }
            }
        }

        var playWhenReadyInstance by remember { mutableStateOf(true) }

        val playerInstance = remember {
            initializeExoPlayer(context, telemetryTransferListener).apply {
                val exoPlayer = this

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        var stateString: String = ""
                        val timestampMs = System.currentTimeMillis()

                        when (playbackState) {
                            ExoPlayer.STATE_IDLE -> {
                                stateString = "ExoPlayer.STATE_IDLE      -"

                                visibilityProgressBar = false
                            }

                            ExoPlayer.STATE_BUFFERING -> {
                                stateString = "ExoPlayer.STATE_BUFFERING -"

                                visibilityProgressBar = false
                            }

                            ExoPlayer.STATE_READY -> {
                                stateString = "ExoPlayer.STATE_READY     -"

                                visibilityProgressBar = false
                            }

                            ExoPlayer.STATE_ENDED -> {
                                stateString = "ExoPlayer.STATE_ENDED     -"

                                visibilityProgressBar = false
                            }

                            else -> {
                                stateString = "UNKNOWN_STATE             -"

                                visibilityProgressBar = false
                            }
                        }

                        telemetryCollector.onPlaybackStateChanged(
                            playbackState = mapPlaybackState(playbackState),
                            playbackPositionMs = exoPlayer.currentPosition,
                            bufferedDurationMs = exoPlayer.totalBufferedDuration,
                            timestampMs = timestampMs
                        )

                        val currentLiveOffset =
                            exoPlayer.currentLiveOffset.takeIf { it != C.TIME_UNSET }
                        telemetryCollector.onLiveLatencyUpdated(
                            liveLatencyMs = currentLiveOffset,
                            timestampMs = timestampMs
                        )

                        println("onPlaybackStateChanged to $stateString")
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        super.onTracksChanged(tracks)

                        selectedTrackSnapshotFromTracks(
                            tracks = tracks,
                            timestampMs = System.currentTimeMillis()
                        )?.let { snapshot ->
                            telemetryCollector.appendTrackSnapshot(snapshot)
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        super.onRenderedFirstFrame()

                        telemetryCollector.onFirstFrameRendered(System.currentTimeMillis())
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)

                        visibilityProgressBar = false

                        telemetryCollector.appendIssue(
                            issue = PlaybackIssue(
                                timestampMs = System.currentTimeMillis(),
                                category = "PLAYER_ERROR",
                                message = error.message ?: "Unknown playback error",
                                code = error.errorCode
                            ),
                            isFatal = true
                        )

                        finishTelemetrySession("player_error")
                        playWhenReadyInstance = false
                        exoPlayer.playWhenReady = false
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()

                        try {
                            textError = exoPlayerErrorPrefix + errorHandler(error)

                            visibilityError = true
                        } catch (e: Exception) {
                            textError = exoPlayerUnknownPrefix + errorHandler(error)

                            visibilityError = true
                        }
                    }
                })

                addAnalyticsListener(object : AnalyticsListener {
                    override fun onBandwidthEstimate(
                        eventTime: AnalyticsListener.EventTime,
                        totalLoadTimeMs: Int,
                        totalBytesLoaded: Long,
                        bitrateEstimate: Long
                    ) {
                        val timestampMs = System.currentTimeMillis()
                        val throughputKbps = bitrateEstimate.toKbpsFromBitsPerSecond()
                        telemetryCollector.appendNetworkSample(
                            NetworkSample(
                                timestampMs = timestampMs,
                                throughputKbps = throughputKbps,
                                bufferMs = exoPlayer.totalBufferedDuration,
                                transferBytes = totalBytesLoaded,
                                transferDurationMs = totalLoadTimeMs.toLong()
                            )
                        )
                    }

                    override fun onLoadCompleted(
                        eventTime: AnalyticsListener.EventTime,
                        loadEventInfo: LoadEventInfo,
                        mediaLoadData: MediaLoadData
                    ) {
                        telemetryCollector.appendNetworkSample(
                            NetworkSample(
                                timestampMs = System.currentTimeMillis(),
                                throughputKbps = calculateThroughputKbps(
                                    loadEventInfo.bytesLoaded,
                                    loadEventInfo.loadDurationMs
                                ),
                                bufferMs = exoPlayer.totalBufferedDuration,
                                responseCode = extractResponseCode(loadEventInfo.responseHeaders),
                                host = loadEventInfo.uri.host,
                                segmentUri = loadEventInfo.uri.toString(),
                                transferBytes = loadEventInfo.bytesLoaded,
                                transferDurationMs = loadEventInfo.loadDurationMs
                            )
                        )
                    }

                    override fun onLoadError(
                        eventTime: AnalyticsListener.EventTime,
                        loadEventInfo: LoadEventInfo,
                        mediaLoadData: MediaLoadData,
                        error: IOException,
                        wasCanceled: Boolean
                    ) {
                        telemetryCollector.appendIssue(
                            issue = PlaybackIssue(
                                timestampMs = System.currentTimeMillis(),
                                category = "LOAD_ERROR",
                                message = error.message ?: "Unknown load error",
                                code = null
                            ),
                            isFatal = false
                        )
                    }

                    override fun onDownstreamFormatChanged(
                        eventTime: AnalyticsListener.EventTime,
                        mediaLoadData: MediaLoadData
                    ) {
                        val format = mediaLoadData.trackFormat ?: return
                        val timestampMs = System.currentTimeMillis()

                        when (mediaLoadData.trackType) {
                            C.TRACK_TYPE_VIDEO -> {
                                format.bitrate.toKbpsFromBitsPerSecond()?.let { newBitrateKbps ->
                                    telemetryCollector.onBitrateSwitch(
                                        newBitrateKbps = newBitrateKbps,
                                        timestampMs = timestampMs
                                    )
                                }
                                telemetryCollector.appendTrackSnapshot(
                                    trackSnapshotFromMediaLoadData(
                                        format,
                                        mediaLoadData.trackType,
                                        timestampMs
                                    )
                                )
                            }

                            C.TRACK_TYPE_AUDIO -> {
                                telemetryCollector.appendTrackSnapshot(
                                    trackSnapshotFromMediaLoadData(
                                        format,
                                        mediaLoadData.trackType,
                                        timestampMs
                                    )
                                )
                            }
                        }
                    }

                    override fun onDroppedVideoFrames(
                        eventTime: AnalyticsListener.EventTime,
                        droppedFrames: Int,
                        elapsedMs: Long
                    ) {
                        telemetryCollector.onDroppedFrames(
                            droppedFrames = droppedFrames,
                            elapsedMs = elapsedMs,
                            timestampMs = System.currentTimeMillis()
                        )
                    }
                })
            }
        }

        val playerViewInstance = remember {
            val playerView = PlayerView(context).apply {
                Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.Black)

                setBackgroundColor(AndroidColor.BLACK)

                hideController()
                resizeMode = RESIZE_MODE_FILL
                setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING)
                useController = false

                player = playerInstance as Player
                layoutParams = FrameLayout.LayoutParams(
                    MATCH_PARENT, MATCH_PARENT
                )
            }

            playerView
        }

        DisposableEffect(lifecycle, playerViewInstance, playerInstance) {
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        playerViewInstance.onResume()
                        playerInstance.playWhenReady = playWhenReadyInstance
                    }

                    Lifecycle.Event.ON_STOP -> {
                        playWhenReadyInstance = playerInstance.playWhenReady
                        playerViewInstance.onPause()
                        playerInstance.playWhenReady = false
                    }

                    else -> Unit
                }
            }
            lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycle.removeObserver(lifecycleObserver)
            }
        }

        DisposableEffect(playerInstance) {
            onDispose {
                finishTelemetrySession("dispose")
                playWhenReadyInstance = playerInstance.playWhenReady
                playerInstance.release()
            }
        }

        LaunchedEffect(videoURL, playbackRequestToken, showStatsForNerds) {
            if (!showStatsForNerds) {
                statsForNerdsState = statsForNerdsState.copy(isVisible = false)
                rollingTransferSamples.clear()
                previousNetworkTotalBytes = networkTransferredBytes.get()
                previousNetworkSampleTimestampMs = System.currentTimeMillis()
                return@LaunchedEffect
            }

            while (isActive) {
                val currentPlayer = playerInstance
                val nowMs = System.currentTimeMillis()
                telemetryCollector.onPlaybackStateChanged(
                    playbackState = mapPlaybackState(currentPlayer.playbackState),
                    playbackPositionMs = currentPlayer.currentPosition,
                    bufferedDurationMs = currentPlayer.totalBufferedDuration,
                    timestampMs = nowMs
                )
                telemetryCollector.onLiveLatencyUpdated(
                    liveLatencyMs = currentPlayer.currentLiveOffset.takeIf { it != C.TIME_UNSET },
                    timestampMs = nowMs
                )

                val elapsedMs = if (previousNetworkSampleTimestampMs > 0L) {
                    (nowMs - previousNetworkSampleTimestampMs).coerceAtLeast(1L)
                } else {
                    STATS_UPDATE_INTERVAL_PLAYING_MS
                }
                previousNetworkSampleTimestampMs = nowMs

                val currentNetworkTotalBytes = networkTransferredBytes.get()
                val networkBytesDelta =
                    (currentNetworkTotalBytes - previousNetworkTotalBytes).coerceAtLeast(0L)
                previousNetworkTotalBytes = currentNetworkTotalBytes

                val isPlaybackActive =
                    currentPlayer.playbackState != ExoPlayer.STATE_IDLE &&
                            currentPlayer.playbackState != ExoPlayer.STATE_ENDED
                if (isPlaybackActive) {
                    rollingTransferSamples.add(
                        WindowedTransferSample(
                            bytesDelta = networkBytesDelta,
                            durationMs = elapsedMs
                        )
                    )
                    var rollingDurationMs = rollingTransferSamples.sumOf { it.durationMs }
                    while (rollingTransferSamples.size > 1 && rollingDurationMs > STATS_NETWORK_RATE_WINDOW_MS) {
                        rollingDurationMs -= rollingTransferSamples.removeAt(0).durationMs
                    }
                } else {
                    rollingTransferSamples.clear()
                }

                val rollingBytes = rollingTransferSamples.sumOf { it.bytesDelta }
                val rollingDurationMs =
                    rollingTransferSamples.sumOf { it.durationMs }.coerceAtLeast(1L)
                val liveNetworkBytesPerSecond = if (isPlaybackActive && rollingBytes > 0L) {
                    (rollingBytes * 1000L) / rollingDurationMs
                } else {
                    0L
                }
                val liveConnectionSpeedKbps = if (isPlaybackActive && rollingBytes > 0L) {
                    ((rollingBytes * 8.0) / rollingDurationMs.toDouble()).roundToInt()
                        .coerceAtLeast(0)
                } else {
                    0
                }
                val lowLatencySignals = currentPlayer.toLowLatencySignals()

                val latestState = telemetryCollector.currentSnapshot()
                    .toStatsForNerdsState(
                        streamUrlFallback = videoURL,
                        isVisible = showStatsForNerds
                    )
                    .copy(
                        isLiveStream = lowLatencySignals.isLive,
                        liveLatencyMs = lowLatencySignals.liveOffsetMs,
                        liveTargetOffsetMs = lowLatencySignals.targetOffsetMs,
                        liveOffsetDeltaMs = lowLatencySignals.offsetDeltaMs,
                        lowLatencyStatus = lowLatencySignals.status,
                        lowLatencyConfidence = lowLatencySignals.confidence,
                        lowLatencyNote = lowLatencySignals.note,
                        throughputKbps = liveConnectionSpeedKbps,
                        networkActivityBytes = liveNetworkBytesPerSecond
                    )
                statsForNerdsState = latestState

                appendSeriesValue(
                    connectionSpeedSeries,
                    latestState.throughputKbps?.toFloat()
                )
                appendSeriesValue(
                    bufferHealthSeries,
                    latestState.bufferedDurationMs.div(1000f)
                )
                appendSeriesValue(
                    networkActivitySeries,
                    latestState.networkActivityBytes?.div(1024f)
                )
                appendSeriesValue(
                    droppedFramesSeries,
                    latestState.droppedFrames.toFloat()
                )
                statsSeriesVersion += 1

                delay(
                    if (isPlaybackActive) {
                        STATS_UPDATE_INTERVAL_PLAYING_MS
                    } else {
                        STATS_UPDATE_INTERVAL_IDLE_MS
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            AndroidView(
                factory = { playerViewInstance },
                modifier = Modifier.fillMaxSize()
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = visibilityError, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black)
                ) {
                    Text(
                        text = textError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                            .align(Alignment.Center),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 3
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = visibilityProgressBar, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.Black.copy(0.2f))
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        PlayerControllerOverlay(
            state = controllerState,
            sliderPositionMs = if (isScrubbing) scrubbingPositionMs else controllerState.positionMs,
            onPlayPauseToggle = {
                val currentPlayer = playerInstance
                currentPlayer.playWhenReady = !currentPlayer.isPlaying
            },
            onRewind = {
                val currentPlayer = playerInstance
                if (!controllerState.canSeek) return@PlayerControllerOverlay
                val seekTo = (currentPlayer.currentPosition - CONTROLLER_SEEK_INTERVAL_MS)
                    .coerceAtLeast(0L)
                currentPlayer.seekTo(seekTo)
            },
            onForward = {
                val currentPlayer = playerInstance
                if (!controllerState.canSeek) return@PlayerControllerOverlay
                val seekTo = (currentPlayer.currentPosition + CONTROLLER_SEEK_INTERVAL_MS)
                    .coerceAtMost(controllerState.durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE)
                currentPlayer.seekTo(seekTo)
            },
            onGoLive = {
                val currentPlayer = playerInstance
                currentPlayer.seekToDefaultPosition()
                currentPlayer.playWhenReady = true
            },
            onScrubStart = {
                isScrubbing = true
            },
            onScrubUpdate = { next ->
                scrubbingPositionMs = next
            },
            onScrubEnd = { seekTo ->
                val currentPlayer = playerInstance
                currentPlayer.seekTo(seekTo)
                isScrubbing = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        AnimatedVisibility(
            visible = showStatsForNerds,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            StatsForNerdsPanel(
                state = statsForNerdsState,
                connectionSpeedSeries = connectionSpeedSeries,
                bufferHealthSeries = bufferHealthSeries,
                networkActivitySeries = networkActivitySeries,
                droppedFramesSeries = droppedFramesSeries,
                seriesVersion = statsSeriesVersion
            )
        }

        LaunchedEffect(videoURL, playbackRequestToken) {
            while (isActive) {
                val currentPlayer = playerInstance
                val latestControllerState = currentPlayer.toControllerState()
                controllerState = latestControllerState
                if (!isScrubbing) {
                    scrubbingPositionMs = latestControllerState.positionMs
                }

                delay(
                    if (currentPlayer.isPlaying || currentPlayer.playbackState == ExoPlayer.STATE_BUFFERING) {
                        STATS_UPDATE_INTERVAL_PLAYING_MS
                    } else {
                        STATS_UPDATE_INTERVAL_IDLE_MS
                    }
                )
            }
        }

        if (videoURL.isNotEmpty()) {
            LaunchedEffect(videoURL, playbackRequestToken) {
                finishTelemetrySession("switch_url")
                statsForNerdsState = StatsForNerdsState(
                    isVisible = showStatsForNerds,
                    streamUrl = videoURL
                )
                telemetryCollector.startSession(
                    sessionId = UUID.randomUUID().toString(),
                    streamUrl = videoURL,
                    startTimestampMs = System.currentTimeMillis()
                )

                playWhenReadyInstance = true
                visibilityError = false
                textError = ""
                playerInstance.playWhenReady = playWhenReadyInstance

                val mediaItem = MediaItem.Builder().setUri(videoURL).build()
                playerInstance.setMediaItem(mediaItem, true)
                playerInstance.prepare()
            }
        }
    }
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current

    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

@Composable
private fun PlayerControllerOverlay(
    state: PlayerControllerState,
    sliderPositionMs: Long,
    onPlayPauseToggle: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onGoLive: () -> Unit,
    onScrubStart: () -> Unit,
    onScrubUpdate: (Long) -> Unit,
    onScrubEnd: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.62f))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = when {
                        state.isLive && state.atLiveEdge -> Color(0xFFEF5350)
                        state.isLive -> Color(0xFFFFB74D)
                        else -> Color(0xFF66BB6A)
                    },
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        state.isLive && state.hasDvr -> "LIVE (DVR)"
                        state.isLive -> "LIVE"
                        else -> "VOD"
                    },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            val statusText = when {
                state.isLive && state.hasDvr && state.liveOffsetMs != null ->
                    "Latency ${formatDurationClock(state.liveOffsetMs)}"

                state.isLive -> "No DVR window"
                else -> formatDurationClock(state.durationMs)
            }
            Text(
                text = statusText,
                color = Color(0xFFD6DEE8),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (state.canSeek) {
            val duration = state.durationMs.coerceAtLeast(1L)
            val sliderValue = (sliderPositionMs.toFloat() / duration.toFloat())
                .coerceIn(0f, 1f)
            ExpressiveWavySeekBar(
                progressFraction = sliderValue,
                enabled = state.canSeek,
                onScrubStart = onScrubStart,
                onScrubUpdate = { fraction ->
                    val seekTo = (fraction * duration.toFloat()).toLong()
                        .coerceIn(0L, duration)
                    onScrubUpdate(seekTo)
                },
                onScrubEnd = { fraction ->
                    val seekTo = (fraction * duration.toFloat()).toLong()
                        .coerceIn(0L, duration)
                    onScrubEnd(seekTo)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDurationClock(sliderPositionMs),
                    color = Color(0xFFCFD8E3),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (state.isLive && state.hasDvr) {
                        "DVR Window ${formatDurationClock(state.durationMs)}"
                    } else {
                        formatDurationClock(state.durationMs)
                    },
                    color = Color(0xFFCFD8E3),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Text(
                text = "Seeking unavailable for this stream window.",
                color = Color(0xFFCFD8E3),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRewind,
                    enabled = state.canSeek
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = if (state.canSeek) Color.White else Color(0xFF7B8796)
                    )
                }
                IconButton(onClick = onPlayPauseToggle) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play pause",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = onForward,
                    enabled = state.canSeek
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward 10s",
                        tint = if (state.canSeek) Color.White else Color(0xFF7B8796)
                    )
                }
            }

            if (state.isLive) {
                TextButton(onClick = onGoLive) {
                    Text(
                        text = if (state.atLiveEdge) "LIVE" else "GO LIVE",
                        color = if (state.atLiveEdge) Color(0xFFEF5350) else Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveWavySeekBar(
    progressFraction: Float,
    enabled: Boolean,
    onScrubStart: () -> Unit,
    onScrubUpdate: (Float) -> Unit,
    onScrubEnd: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    var dragFraction by remember { mutableFloatStateOf(progressFraction.coerceIn(0f, 1f)) }
    var isDragging by remember { mutableStateOf(false) }

    val safeProgress = progressFraction.coerceIn(0f, 1f)

    LaunchedEffect(safeProgress, isDragging) {
        if (!isDragging) {
            dragFraction = safeProgress
        }
    }

    val visibleProgress = if (isDragging) dragFraction else safeProgress

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x22303B4A))
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(enabled, widthPx) {
                if (!enabled) return@pointerInput
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
            .pointerInput(enabled, widthPx) {
                if (!enabled) return@pointerInput
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
                        val finalFraction = dragFraction.coerceIn(0f, 1f)
                        onScrubEnd(finalFraction)
                        isDragging = false
                    },
                    onDragCancel = {
                        val finalFraction = dragFraction.coerceIn(0f, 1f)
                        onScrubEnd(finalFraction)
                        isDragging = false
                    }
                )
            }
    ) {
        LinearWavyProgressIndicator(
            progress = { visibleProgress },
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            color = Color(0xFF7FE8FF),
            trackColor = Color(0xFF2C3B4C)
        )
    }
}

@Composable
private fun StatsForNerdsPanel(
    state: StatsForNerdsState,
    connectionSpeedSeries: List<Float>,
    bufferHealthSeries: List<Float>,
    networkActivitySeries: List<Float>,
    droppedFramesSeries: List<Float>,
    seriesVersion: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 360.dp)
            .background(color = Color.Black.copy(alpha = 0.78f))
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val connectionHealth =
            connectionSpeedHealth(state.throughputKbps, state.currentBitrateKbps)
        val bufferHealth = bufferHealth(state.bufferedDurationMs)
        val networkHealth =
            networkHealth(
                state.networkActivityBytes,
                state.playerState == PlaybackState.BUFFERING.name
            )
        val droppedFrameHealth = droppedFrameHealth(state.droppedFrames)
        val lowLatencyHealth = lowLatencyHealth(state.lowLatencyStatus)

        Text(
            text = "Stats for Nerds",
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Low Latency ${state.lowLatencyStatus} (${state.lowLatencyConfidence})",
            color = lowLatencyHealth.color,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = state.lowLatencyNote,
            color = Color(0xFF9FA9B7),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))

        StatsTrendChart(
            label = "Connection Speed",
            value = formatKbps(state.throughputKbps),
            points = connectionSpeedSeries,
            seriesVersion = seriesVersion,
            note = "Rolling 1s throughput from transferred bytes.",
            unitSuffix = "kbps",
            status = connectionHealth.label,
            statusColor = connectionHealth.color,
            threshold = connectionHealth.threshold
        )
        StatsTrendChart(
            label = "Buffer Size",
            value = formatSeconds(state.bufferedDurationMs),
            points = bufferHealthSeries,
            seriesVersion = seriesVersion,
            note = "Playable media already buffered ahead of current position.",
            unitSuffix = "s",
            status = bufferHealth.label,
            statusColor = bufferHealth.color,
            threshold = bufferHealth.threshold
        )
        StatsTrendChart(
            label = "Network Activity",
            value = formatKiloBytesPerSecond(state.networkActivityBytes),
            points = networkActivitySeries,
            seriesVersion = seriesVersion,
            note = "Rolling 1s ingress traffic rate from transferred bytes.",
            unitSuffix = "KB/s",
            status = networkHealth.label,
            statusColor = networkHealth.color,
            threshold = networkHealth.threshold
        )
        StatsTrendChart(
            label = "Dropped Frames",
            value = state.droppedFrames.toString(),
            points = droppedFramesSeries,
            seriesVersion = seriesVersion,
            note = "Cumulative dropped render frames since this session started.",
            unitSuffix = "frames",
            decimals = 0,
            status = droppedFrameHealth.label,
            statusColor = droppedFrameHealth.color,
            threshold = droppedFrameHealth.threshold
        )
        Spacer(modifier = Modifier.height(8.dp))

        StatsSectionHeader("Dynamic Values")
        StatsLine("State", state.playerState)
        StatsLine("Position", formatMilliseconds(state.playbackPositionMs))
        StatsLine("Buffer Size", formatMilliseconds(state.bufferedDurationMs))
        StatsLine("Live Latency", formatMilliseconds(state.liveLatencyMs))
        StatsLine("Live Target Offset", formatMilliseconds(state.liveTargetOffsetMs))
        StatsLine("Live Offset Delta", formatSignedMilliseconds(state.liveOffsetDeltaMs))
        StatsLine("Low Latency", state.lowLatencyStatus)
        StatsLine("Current Bitrate", formatKbps(state.currentBitrateKbps))
        StatsLine("Connection Speed", formatKbps(state.throughputKbps))
        StatsLine("Network Activity", formatKiloBytesPerSecond(state.networkActivityBytes))
        StatsLine("Avg Throughput", formatKbps(state.averageThroughputKbps))
        StatsLine("Dropped Frames", state.droppedFrames.toString())
        StatsLine("Rebuffer Count", state.rebufferCount.toString())
        StatsLine("Rebuffer Duration", formatMilliseconds(state.rebufferDurationMs))
        StatsLine("Bitrate Switches", state.bitrateSwitchCount.toString())
        StatsSectionNote(
            "Updates every 250 ms during active playback. Speed/activity use rolling 1s bytes; chart line is smoothed for readability."
        )

        Spacer(modifier = Modifier.height(8.dp))
        StatsSectionHeader("Mostly Static Values")
        StatsLine("Session", state.sessionId.ifBlank { "N/A" })
        StatsLine("Protocol", state.protocol.name)
        StatsLine("URL", state.streamUrl.ifBlank { "N/A" })
        StatsLine("Startup", formatMilliseconds(state.startupMs))
        StatsLine("First Frame", formatMilliseconds(state.firstFrameMs))
        StatsLine("Avg Bitrate", formatKbps(state.averageBitrateKbps))
        StatsLine("Resolution", formatResolution(state.videoWidth, state.videoHeight))
        StatsLine("Video Codec", state.videoCodec ?: "N/A")
        StatsLine("Video Mime", state.videoMimeType ?: "N/A")
        StatsLine("Audio Codec", state.audioCodec ?: "N/A")
        StatsLine("Audio Mime", state.audioMimeType ?: "N/A")
        StatsLine("Audio Channels", state.audioChannels?.toString() ?: "N/A")
        StatsLine("Audio Sample Rate", state.audioSampleRateHz?.let { "$it Hz" } ?: "N/A")
        StatsLine("DRM", state.drmType ?: "None")
        StatsLine("Fatal Errors", state.fatalErrorCount.toString())
        StatsSectionNote(
            "Set once per stream or track selection. Resolution/codec can still change on adaptive streams."
        )
    }
}

@Composable
private fun StatsLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            modifier = Modifier.width(120.dp),
            color = Color(0xFFB8BDC6),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            maxLines = 2
        )
    }
}

@Composable
private fun StatsSectionHeader(text: String) {
    Text(
        text = text,
        color = Color(0xFFE3E7ED),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun StatsSectionNote(text: String) {
    Text(
        text = text,
        color = Color(0xFF8F9AAA),
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
    )
}

@Composable
private fun StatsTrendChart(
    label: String,
    value: String,
    points: List<Float>,
    seriesVersion: Int,
    note: String,
    unitSuffix: String,
    status: String,
    statusColor: Color,
    threshold: String,
    decimals: Int = 1
) {
    val chartValues = remember(seriesVersion, points.size, points.lastOrNull()) {
        points.map { it.coerceAtLeast(0f) }
    }
    val smoothedValues = remember(seriesVersion, chartValues.size, chartValues.lastOrNull()) {
        smoothSeries(chartValues, STATS_CHART_SMOOTHING_WINDOW)
    }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(seriesVersion, smoothedValues.size, smoothedValues.lastOrNull()) {
        modelProducer.runTransaction {
            lineSeries {
                series(y = smoothedValues.ifEmpty { listOf(0f) }.map { it.toDouble() })
            }
        }
    }
    val minValue = chartValues.minOrNull()
    val maxValue = chartValues.maxOrNull()
    val currentValue = chartValues.lastOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: $value",
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = status,
                color = statusColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = threshold,
            color = Color(0xFF9FA9B7),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = note,
            color = Color(0xFF8F9AAA),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.Black.copy(alpha = 0.2f))
        ) {
            CartesianChartHost(
                chart = rememberCartesianChart(rememberLineCartesianLayer()),
                modelProducer = modelProducer,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = "min ${
                formatTrendValue(
                    minValue,
                    unitSuffix,
                    decimals
                )
            } | now ${
                formatTrendValue(
                    currentValue,
                    unitSuffix,
                    decimals
                )
            } | max ${formatTrendValue(maxValue, unitSuffix, decimals)}",
            color = Color(0xFFB8BDC6),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@UnstableApi
private data class TransferSample(
    val dataSpec: DataSpec,
    val startedAtMs: Long,
    val transferredBytes: Long
)

private data class WindowedTransferSample(
    val bytesDelta: Long,
    val durationMs: Long
)

private data class PlayerControllerState(
    val isLive: Boolean = false,
    val hasDvr: Boolean = false,
    val isSeekable: Boolean = false,
    val isPlaying: Boolean = false,
    val playbackState: Int = ExoPlayer.STATE_IDLE,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val liveOffsetMs: Long? = null,
    val atLiveEdge: Boolean = false
) {
    val canSeek: Boolean
        get() = isSeekable && durationMs > 0L
}

private data class LowLatencySignals(
    val isLive: Boolean,
    val liveOffsetMs: Long?,
    val targetOffsetMs: Long?,
    val offsetDeltaMs: Long?,
    val status: String,
    val confidence: String,
    val note: String
)

private const val STATS_UPDATE_INTERVAL_PLAYING_MS = 250L
private const val STATS_UPDATE_INTERVAL_IDLE_MS = 750L
private const val STATS_SERIES_MAX_POINTS = 180
private const val STATS_NETWORK_RATE_WINDOW_MS = 1_000L
private const val STATS_CHART_SMOOTHING_WINDOW = 4
private const val CONTROLLER_SEEK_INTERVAL_MS = 10_000L
private const val LIVE_EDGE_THRESHOLD_MS = 3_000L
private const val LOW_LATENCY_STRICT_THRESHOLD_MS = 3_000L
private const val LOW_LATENCY_POSSIBLE_THRESHOLD_MS = 5_000L

@UnstableApi
private fun ExoPlayer.toControllerState(): PlayerControllerState {
    val durationMs = duration.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
    val positionMs = currentPosition.coerceAtLeast(0L)
    val bufferedMs = bufferedPosition.takeIf { it != C.TIME_UNSET && it >= 0L } ?: positionMs
    val liveOffsetMs = currentLiveOffset.takeIf { it != C.TIME_UNSET && it >= 0L }
    val isLive = isCurrentMediaItemLive
    val isSeekable = isCurrentMediaItemSeekable
    val hasDvr = isLive && isSeekable && durationMs > 0L
    val atLiveEdge = if (isLive) {
        !hasDvr || (liveOffsetMs?.let { it <= LIVE_EDGE_THRESHOLD_MS } ?: false)
    } else {
        false
    }

    return PlayerControllerState(
        isLive = isLive,
        hasDvr = hasDvr,
        isSeekable = isSeekable,
        isPlaying = isPlaying,
        playbackState = playbackState,
        positionMs = positionMs,
        durationMs = durationMs,
        bufferedPositionMs = bufferedMs,
        liveOffsetMs = liveOffsetMs,
        atLiveEdge = atLiveEdge
    )
}

@UnstableApi
private fun ExoPlayer.toLowLatencySignals(): LowLatencySignals {
    val isLive = isCurrentMediaItemLive
    if (!isLive) {
        return LowLatencySignals(
            isLive = false,
            liveOffsetMs = null,
            targetOffsetMs = null,
            offsetDeltaMs = null,
            status = "N/A",
            confidence = "N/A",
            note = "Not a live stream (VOD/progressive playback)."
        )
    }

    val currentOffsetMs = currentLiveOffset.takeIf { it != C.TIME_UNSET && it >= 0L }
    val targetOffsetMs = resolveLiveTargetOffsetMs()
    val offsetDeltaMs =
        if (currentOffsetMs != null && targetOffsetMs != null) currentOffsetMs - targetOffsetMs else null

    if (currentOffsetMs == null) {
        return LowLatencySignals(
            isLive = true,
            liveOffsetMs = null,
            targetOffsetMs = targetOffsetMs,
            offsetDeltaMs = null,
            status = "UNKNOWN",
            confidence = if (targetOffsetMs != null) "High" else "Medium",
            note = "Waiting for live offset from player."
        )
    }

    if (targetOffsetMs != null) {
        val toleranceMs = ((targetOffsetMs * 0.6f).roundToInt()).coerceAtLeast(1_000)
        val status = when {
            currentOffsetMs <= targetOffsetMs + toleranceMs -> "YES"
            currentOffsetMs <= targetOffsetMs + (toleranceMs * 2L) -> "POSSIBLE"
            else -> "NO"
        }
        return LowLatencySignals(
            isLive = true,
            liveOffsetMs = currentOffsetMs,
            targetOffsetMs = targetOffsetMs,
            offsetDeltaMs = offsetDeltaMs,
            status = status,
            confidence = "High",
            note = "Manifest target offset available. Classified against target + tolerance."
        )
    }

    val status = when {
        currentOffsetMs <= LOW_LATENCY_STRICT_THRESHOLD_MS -> "YES"
        currentOffsetMs <= LOW_LATENCY_POSSIBLE_THRESHOLD_MS -> "POSSIBLE"
        else -> "NO"
    }
    return LowLatencySignals(
        isLive = true,
        liveOffsetMs = currentOffsetMs,
        targetOffsetMs = null,
        offsetDeltaMs = null,
        status = status,
        confidence = "Medium",
        note = "No manifest target offset. Classified by observed live offset threshold."
    )
}

@UnstableApi
private fun ExoPlayer.resolveLiveTargetOffsetMs(): Long? {
    val mediaItemTarget = currentMediaItem?.liveConfiguration?.targetOffsetMs
        ?.takeIf { it != C.TIME_UNSET && it > 0L }

    val timelineTarget = runCatching {
        if (currentTimeline.isEmpty) {
            null
        } else {
            val window = Timeline.Window()
            currentTimeline.getWindow(currentMediaItemIndex.coerceAtLeast(0), window)
            window.liveConfiguration?.targetOffsetMs
                ?.takeIf { it != C.TIME_UNSET && it > 0L }
        }
    }.getOrNull()

    return timelineTarget ?: mediaItemTarget
}

private fun mapPlaybackState(playbackState: Int): PlaybackState {
    return when (playbackState) {
        ExoPlayer.STATE_IDLE -> PlaybackState.IDLE
        ExoPlayer.STATE_BUFFERING -> PlaybackState.BUFFERING
        ExoPlayer.STATE_READY -> PlaybackState.READY
        ExoPlayer.STATE_ENDED -> PlaybackState.ENDED
        else -> PlaybackState.UNKNOWN
    }
}

@UnstableApi
private fun selectedTrackSnapshotFromTracks(
    tracks: Tracks,
    timestampMs: Long
): TrackSnapshot? {
    var videoFormat: Format? = null
    var audioFormat: Format? = null

    tracks.groups.forEach { group ->
        for (trackIndex in 0 until group.length) {
            if (!group.isTrackSelected(trackIndex)) continue
            when (group.type) {
                C.TRACK_TYPE_VIDEO -> if (videoFormat == null) {
                    videoFormat = group.getTrackFormat(trackIndex)
                }

                C.TRACK_TYPE_AUDIO -> if (audioFormat == null) {
                    audioFormat = group.getTrackFormat(trackIndex)
                }
            }
        }
    }

    if (videoFormat == null && audioFormat == null) return null

    return TrackSnapshot(
        timestampMs = timestampMs,
        videoCodec = videoFormat?.codecs,
        videoMimeType = videoFormat?.sampleMimeType,
        width = videoFormat?.width.toValidFormatIntOrNull(),
        height = videoFormat?.height.toValidFormatIntOrNull(),
        frameRate = videoFormat?.frameRate?.takeIf { it > 0f },
        videoBitrateKbps = videoFormat?.bitrate.toKbpsFromBitsPerSecond(),
        audioCodec = audioFormat?.codecs,
        audioMimeType = audioFormat?.sampleMimeType,
        audioBitrateKbps = audioFormat?.bitrate.toKbpsFromBitsPerSecond(),
        audioChannels = audioFormat?.channelCount.toValidFormatIntOrNull(),
        audioSampleRateHz = audioFormat?.sampleRate.toValidFormatIntOrNull(),
        drmType = videoFormat?.drmInitData?.schemeType ?: audioFormat?.drmInitData?.schemeType
    )
}

@UnstableApi
private fun trackSnapshotFromMediaLoadData(
    format: Format,
    trackType: Int,
    timestampMs: Long
): TrackSnapshot {
    return when (trackType) {
        C.TRACK_TYPE_VIDEO -> TrackSnapshot(
            timestampMs = timestampMs,
            videoCodec = format.codecs,
            videoMimeType = format.sampleMimeType,
            width = format.width.toValidFormatIntOrNull(),
            height = format.height.toValidFormatIntOrNull(),
            frameRate = format.frameRate.takeIf { it > 0f },
            videoBitrateKbps = format.bitrate.toKbpsFromBitsPerSecond(),
            drmType = format.drmInitData?.schemeType
        )

        C.TRACK_TYPE_AUDIO -> TrackSnapshot(
            timestampMs = timestampMs,
            audioCodec = format.codecs,
            audioMimeType = format.sampleMimeType,
            audioBitrateKbps = format.bitrate.toKbpsFromBitsPerSecond(),
            audioChannels = format.channelCount.toValidFormatIntOrNull(),
            audioSampleRateHz = format.sampleRate.toValidFormatIntOrNull(),
            drmType = format.drmInitData?.schemeType
        )

        else -> TrackSnapshot(timestampMs = timestampMs)
    }
}

private fun Int?.toValidFormatIntOrNull(): Int? {
    val value = this ?: return null
    return value.takeIf { it != Format.NO_VALUE && it > 0 }
}

private fun Int?.toKbpsFromBitsPerSecond(): Int? {
    val value = this ?: return null
    if (value == Format.NO_VALUE || value <= 0) return null
    return (value / 1000f).toInt().takeIf { it > 0 }
}

private fun Long.toKbpsFromBitsPerSecond(): Int? {
    if (this <= 0L) return null
    return (this / 1000L).toInt().takeIf { it > 0 }
}

private fun calculateThroughputKbps(bytesLoaded: Long, loadDurationMs: Long): Int? {
    if (bytesLoaded <= 0L || loadDurationMs <= 0L) return null
    val kbps = ((bytesLoaded * 8.0) / loadDurationMs.toDouble()).toInt()
    return kbps.takeIf { it > 0 }
}

private fun extractResponseCode(responseHeaders: Map<String, List<String>>): Int? {
    val possibleKeys = listOf(":status", "status", "Status", "response-code", "Response-Code")
    possibleKeys.forEach { key ->
        val matchingEntry =
            responseHeaders.entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
        val code = matchingEntry?.value?.firstOrNull()?.trim()?.toIntOrNull()
        if (code != null) return code
    }
    return null
}

private fun LivePlaybackSnapshot?.toStatsForNerdsState(
    streamUrlFallback: String,
    isVisible: Boolean
): StatsForNerdsState {
    if (this == null) {
        return StatsForNerdsState(
            isVisible = isVisible,
            streamUrl = streamUrlFallback
        )
    }

    return StatsForNerdsState(
        isVisible = isVisible,
        sessionId = sessionId,
        streamUrl = streamUrl,
        protocol = protocol,
        playerState = playbackState.name,
        playbackPositionMs = playbackPositionMs,
        bufferedDurationMs = bufferedDurationMs,
        currentBitrateKbps = currentBitrateKbps,
        averageBitrateKbps = averageBitrateKbps,
        throughputKbps = throughputKbps,
        averageThroughputKbps = averageThroughputKbps,
        networkActivityBytes = networkActivityBytes,
        liveLatencyMs = liveLatencyMs,
        droppedFrames = droppedFrames,
        startupMs = startupMs,
        firstFrameMs = firstFrameMs,
        rebufferCount = rebufferCount,
        rebufferDurationMs = rebufferDurationMs,
        bitrateSwitchCount = bitrateSwitchCount,
        fatalErrorCount = fatalErrorCount,
        videoCodec = videoCodec,
        videoMimeType = videoMimeType,
        videoWidth = videoWidth,
        videoHeight = videoHeight,
        audioCodec = audioCodec,
        audioMimeType = audioMimeType,
        audioChannels = audioChannels,
        audioSampleRateHz = audioSampleRateHz,
        drmType = drmType
    )
}

private fun formatKbps(value: Int?): String {
    if (value == null) return "N/A"
    if (value <= 0) return "0 kbps"
    return "$value kbps"
}

private fun formatSeconds(valueMs: Long): String {
    if (valueMs <= 0L) return "N/A"
    val seconds = valueMs / 1000f
    return String.format(Locale.US, "%.1f s", seconds)
}

private fun formatKiloBytesPerSecond(valueBytes: Long?): String {
    if (valueBytes == null) return "N/A"
    if (valueBytes <= 0L) return "0.0 KB/s"
    val kb = valueBytes / 1024f
    return String.format(Locale.US, "%.1f KB/s", kb)
}

private fun formatMilliseconds(value: Long?): String {
    if (value == null || value < 0L) return "N/A"
    return "$value ms"
}

private fun formatSignedMilliseconds(value: Long?): String {
    if (value == null) return "N/A"
    val sign = if (value >= 0L) "+" else ""
    return "$sign$value ms"
}

private fun formatDurationClock(valueMs: Long): String {
    if (valueMs <= 0L) return "00:00"
    val totalSeconds = valueMs / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun formatResolution(width: Int?, height: Int?): String {
    if (width == null || height == null) return "N/A"
    return "${width}x$height"
}

private data class MetricHealthState(
    val label: String,
    val color: Color,
    val threshold: String
)

private fun connectionSpeedHealth(
    throughputKbps: Int?,
    currentBitrateKbps: Int?
): MetricHealthState {
    val targetBitrate = currentBitrateKbps?.takeIf { it > 0 }
        ?: return MetricHealthState(
            label = "NO TARGET",
            color = Color(0xFF90A4AE),
            threshold = "Target: >= 1.3x of current bitrate once bitrate is known."
        )

    val throughput = throughputKbps ?: 0
    val ratio = throughput.toFloat() / targetBitrate.toFloat()
    return when {
        ratio >= 1.3f -> MetricHealthState(
            label = "HEALTHY",
            color = Color(0xFF66BB6A),
            threshold = "Target: >= 1.3x current bitrate."
        )

        ratio >= 1.0f -> MetricHealthState(
            label = "MARGINAL",
            color = Color(0xFFFFCA28),
            threshold = "Target: >= 1.3x current bitrate."
        )

        else -> MetricHealthState(
            label = "LOW",
            color = Color(0xFFEF5350),
            threshold = "Target: >= 1.3x current bitrate."
        )
    }
}

private fun lowLatencyHealth(status: String): MetricHealthState {
    return when (status.uppercase()) {
        "YES" -> MetricHealthState(
            label = "HEALTHY",
            color = Color(0xFF66BB6A),
            threshold = "Target: keep live offset <= 3.0 s, or close to manifest target."
        )

        "POSSIBLE" -> MetricHealthState(
            label = "MARGINAL",
            color = Color(0xFFFFCA28),
            threshold = "Target: keep live offset <= 3.0 s, or close to manifest target."
        )

        "NO" -> MetricHealthState(
            label = "LOW",
            color = Color(0xFFEF5350),
            threshold = "Target: keep live offset <= 3.0 s, or close to manifest target."
        )

        else -> MetricHealthState(
            label = "N/A",
            color = Color(0xFF90A4AE),
            threshold = "Low-latency check applies to live streams only."
        )
    }
}

private fun bufferHealth(bufferedDurationMs: Long): MetricHealthState {
    return when {
        bufferedDurationMs >= 10_000L -> MetricHealthState(
            label = "HEALTHY",
            color = Color(0xFF66BB6A),
            threshold = "Target: >= 5.0 s buffered. Warning below 2.0 s."
        )

        bufferedDurationMs >= 2_000L -> MetricHealthState(
            label = "MARGINAL",
            color = Color(0xFFFFCA28),
            threshold = "Target: >= 5.0 s buffered. Warning below 2.0 s."
        )

        else -> MetricHealthState(
            label = "LOW",
            color = Color(0xFFEF5350),
            threshold = "Target: >= 5.0 s buffered. Warning below 2.0 s."
        )
    }
}

private fun networkHealth(
    networkActivityBytesPerSecond: Long?,
    isBuffering: Boolean
): MetricHealthState {
    val bytesPerSecond = networkActivityBytesPerSecond ?: 0L
    return when {
        bytesPerSecond > 0L -> MetricHealthState(
            label = "ACTIVE",
            color = Color(0xFF26C6DA),
            threshold = "Expected > 0 KB/s while loading data."
        )

        isBuffering -> MetricHealthState(
            label = "STALLED",
            color = Color(0xFFEF5350),
            threshold = "Expected > 0 KB/s while buffering."
        )

        else -> MetricHealthState(
            label = "IDLE",
            color = Color(0xFF90A4AE),
            threshold = "Can be 0 KB/s when player is already well buffered."
        )
    }
}

private fun droppedFrameHealth(droppedFrames: Int): MetricHealthState {
    return when {
        droppedFrames <= 5 -> MetricHealthState(
            label = "HEALTHY",
            color = Color(0xFF66BB6A),
            threshold = "Target: keep total dropped frames <= 30."
        )

        droppedFrames <= 30 -> MetricHealthState(
            label = "MARGINAL",
            color = Color(0xFFFFCA28),
            threshold = "Target: keep total dropped frames <= 30."
        )

        else -> MetricHealthState(
            label = "LOW",
            color = Color(0xFFEF5350),
            threshold = "Target: keep total dropped frames <= 30."
        )
    }
}

private fun smoothSeries(values: List<Float>, windowSize: Int): List<Float> {
    if (values.isEmpty() || windowSize <= 1) return values
    return values.indices.map { index ->
        val start = (index - windowSize + 1).coerceAtLeast(0)
        val window = values.subList(start, index + 1)
        window.average().toFloat()
    }
}

private fun formatTrendValue(value: Float?, unitSuffix: String, decimals: Int): String {
    val safeValue = value ?: return "N/A"
    return if (decimals <= 0) {
        "${safeValue.roundToInt()} $unitSuffix"
    } else {
        "${String.format(Locale.US, "%.${decimals}f", safeValue)} $unitSuffix"
    }
}

private fun appendSeriesValue(series: MutableList<Float>, value: Float?) {
    val next = value ?: 0f
    series.add(next.coerceAtLeast(0f))
    while (series.size > STATS_SERIES_MAX_POINTS) {
        series.removeAt(0)
    }
}
