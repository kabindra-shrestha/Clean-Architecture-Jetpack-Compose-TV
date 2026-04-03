package com.kabindra.player.player.telemetry.collector

import com.kabindra.player.player.telemetry.model.KpiSummary
import com.kabindra.player.player.telemetry.model.LivePlaybackSnapshot
import com.kabindra.player.player.telemetry.model.NetworkSample
import com.kabindra.player.player.telemetry.model.PlaybackIssue
import com.kabindra.player.player.telemetry.model.PlaybackSessionReport
import com.kabindra.player.player.telemetry.model.PlaybackState
import com.kabindra.player.player.telemetry.model.SessionProtocol
import com.kabindra.player.player.telemetry.model.TrackSnapshot
import kotlin.math.max

/**
 * In-memory telemetry collector used by player integrations.
 *
 * It aggregates live playback signals into a structured [PlaybackSessionReport].
 */
class DefaultPlaybackTelemetryCollector(
    private val maxNetworkSamples: Int = DEFAULT_MAX_NETWORK_SAMPLES,
    private val maxTrackSnapshots: Int = DEFAULT_MAX_TRACK_SNAPSHOTS,
    private val maxIssues: Int = DEFAULT_MAX_ISSUES
) : PlaybackTelemetryCollector {

    private var sessionId: String? = null
    private var streamUrl: String? = null
    private var protocol: SessionProtocol = SessionProtocol.UNKNOWN
    private var startedAtMs: Long = 0L

    private var playbackState: PlaybackState = PlaybackState.IDLE
    private var playbackPositionMs: Long = 0L
    private var bufferedDurationMs: Long = 0L

    private var startupMs: Long? = null
    private var firstFrameMs: Long? = null
    private var bufferingStartedAtMs: Long? = null
    private var rebufferCount: Int = 0
    private var rebufferDurationMs: Long = 0L
    private var droppedFrames: Int = 0
    private var fatalErrorCount: Int = 0
    private var liveLatencyMs: Long? = null

    private var bitrateSwitchCount: Int = 0
    private var currentBitrateKbps: Int? = null
    private var lastBitrateForSwitchKbps: Int? = null
    private var bitrateSamplesTotal: Long = 0L
    private var bitrateSampleCount: Int = 0

    private var currentThroughputKbps: Int? = null
    private var throughputSamplesTotal: Long = 0L
    private var throughputSampleCount: Int = 0
    private var latestNetworkActivityBytes: Long? = null

    private val networkSamples = mutableListOf<NetworkSample>()
    private val trackSnapshots = mutableListOf<TrackSnapshot>()
    private val issues = mutableListOf<PlaybackIssue>()

    override fun startSession(sessionId: String, streamUrl: String, startTimestampMs: Long) {
        reset()
        this.sessionId = sessionId
        this.streamUrl = streamUrl
        this.protocol = resolveProtocol(streamUrl)
        this.startedAtMs = startTimestampMs
    }

    override fun onPlaybackStateChanged(
        playbackState: PlaybackState,
        playbackPositionMs: Long,
        bufferedDurationMs: Long,
        timestampMs: Long
    ) {
        if (sessionId == null) return

        this.playbackPositionMs = playbackPositionMs
        this.bufferedDurationMs = max(bufferedDurationMs, 0L)

        if (this.playbackState == PlaybackState.BUFFERING && playbackState != PlaybackState.BUFFERING) {
            bufferingStartedAtMs?.let { started ->
                rebufferDurationMs += (timestampMs - started).coerceAtLeast(0L)
            }
            bufferingStartedAtMs = null
        }

        if (playbackState == PlaybackState.BUFFERING && this.playbackState != PlaybackState.BUFFERING) {
            // The first BUFFERING before startup is treated as startup, not rebuffer.
            if (startupMs != null) {
                rebufferCount += 1
                bufferingStartedAtMs = timestampMs
            } else {
                bufferingStartedAtMs = null
            }
        }

        if (playbackState == PlaybackState.READY && startupMs == null && startedAtMs > 0L) {
            startupMs = (timestampMs - startedAtMs).coerceAtLeast(0L)
        }

        this.playbackState = playbackState
    }

    override fun onFirstFrameRendered(timestampMs: Long) {
        if (sessionId == null || firstFrameMs != null || startedAtMs <= 0L) return
        firstFrameMs = (timestampMs - startedAtMs).coerceAtLeast(0L)
    }

    override fun onDroppedFrames(droppedFrames: Int, elapsedMs: Long?, timestampMs: Long) {
        if (sessionId == null || droppedFrames <= 0) return
        this.droppedFrames += droppedFrames
    }

    override fun onBitrateSwitch(newBitrateKbps: Int, timestampMs: Long) {
        if (sessionId == null || newBitrateKbps <= 0) return

        currentBitrateKbps = newBitrateKbps
        bitrateSamplesTotal += newBitrateKbps.toLong()
        bitrateSampleCount += 1

        val previousBitrate = lastBitrateForSwitchKbps
        if (previousBitrate != null && previousBitrate != newBitrateKbps) {
            bitrateSwitchCount += 1
        }
        lastBitrateForSwitchKbps = newBitrateKbps
    }

    override fun onLiveLatencyUpdated(liveLatencyMs: Long?, timestampMs: Long) {
        if (sessionId == null) return
        this.liveLatencyMs = liveLatencyMs
    }

    override fun appendNetworkSample(sample: NetworkSample) {
        if (sessionId == null) return

        networkSamples.add(sample)
        if (networkSamples.size > maxNetworkSamples) {
            networkSamples.removeAt(0)
        }

        sample.throughputKbps?.takeIf { it > 0 }?.let { throughput ->
            currentThroughputKbps = throughput
            throughputSamplesTotal += throughput.toLong()
            throughputSampleCount += 1
        }
        latestNetworkActivityBytes = sample.transferBytes
    }

    override fun appendTrackSnapshot(snapshot: TrackSnapshot) {
        if (sessionId == null) return

        trackSnapshots.add(snapshot)
        if (trackSnapshots.size > maxTrackSnapshots) {
            trackSnapshots.removeAt(0)
        }

        snapshot.videoBitrateKbps?.takeIf { it > 0 }
            ?.let { onBitrateSwitch(it, snapshot.timestampMs) }
    }

    override fun appendIssue(issue: PlaybackIssue, isFatal: Boolean) {
        if (sessionId == null) return

        issues.add(issue)
        if (issues.size > maxIssues) {
            issues.removeAt(0)
        }

        if (isFatal) {
            fatalErrorCount += 1
        }
    }

    override fun currentSnapshot(): LivePlaybackSnapshot? {
        val sessionId = sessionId ?: return null
        val streamUrl = streamUrl ?: return null
        val latestVideoSnapshot = trackSnapshots
            .asReversed()
            .firstOrNull { it.videoCodec != null || it.videoMimeType != null || it.width != null || it.height != null }
        val latestAudioSnapshot = trackSnapshots
            .asReversed()
            .firstOrNull { it.audioCodec != null || it.audioMimeType != null || it.audioChannels != null || it.audioSampleRateHz != null }

        return LivePlaybackSnapshot(
            sessionId = sessionId,
            streamUrl = streamUrl,
            protocol = protocol,
            playbackState = playbackState,
            playbackPositionMs = playbackPositionMs,
            bufferedDurationMs = bufferedDurationMs,
            throughputKbps = currentThroughputKbps,
            currentBitrateKbps = currentBitrateKbps,
            averageBitrateKbps = averageBitrate(),
            averageThroughputKbps = averageThroughput(),
            networkActivityBytes = latestNetworkActivityBytes,
            liveLatencyMs = liveLatencyMs,
            droppedFrames = droppedFrames,
            bitrateSwitchCount = bitrateSwitchCount,
            startupMs = startupMs,
            firstFrameMs = firstFrameMs,
            rebufferCount = rebufferCount,
            rebufferDurationMs = rebufferDurationMs,
            fatalErrorCount = fatalErrorCount,
            videoCodec = latestVideoSnapshot?.videoCodec,
            videoMimeType = latestVideoSnapshot?.videoMimeType,
            videoWidth = latestVideoSnapshot?.width,
            videoHeight = latestVideoSnapshot?.height,
            audioCodec = latestAudioSnapshot?.audioCodec,
            audioMimeType = latestAudioSnapshot?.audioMimeType,
            audioChannels = latestAudioSnapshot?.audioChannels,
            audioSampleRateHz = latestAudioSnapshot?.audioSampleRateHz,
            drmType = latestVideoSnapshot?.drmType ?: latestAudioSnapshot?.drmType
        )
    }

    override fun endSession(endTimestampMs: Long): PlaybackSessionReport? {
        val sessionId = sessionId ?: return null
        val streamUrl = streamUrl ?: return null
        val startedAtMs = startedAtMs
        if (startedAtMs <= 0L) return null

        if (bufferingStartedAtMs != null) {
            rebufferDurationMs += (endTimestampMs - bufferingStartedAtMs!!).coerceAtLeast(0L)
            bufferingStartedAtMs = null
        }

        val report = PlaybackSessionReport(
            sessionId = sessionId,
            streamUrl = streamUrl,
            protocol = protocol,
            startedAtMs = startedAtMs,
            endedAtMs = endTimestampMs,
            kpiSummary = KpiSummary(
                startupMs = startupMs,
                firstFrameMs = firstFrameMs,
                averageBitrateKbps = averageBitrate(),
                averageThroughputKbps = averageThroughput(),
                bitrateSwitchCount = bitrateSwitchCount,
                rebufferCount = rebufferCount,
                rebufferDurationMs = rebufferDurationMs,
                droppedFrames = droppedFrames,
                liveLatencyMs = liveLatencyMs,
                fatalErrorCount = fatalErrorCount
            ),
            trackSnapshots = trackSnapshots.toList(),
            networkSamples = networkSamples.toList(),
            issues = issues.toList()
        )

        reset()
        return report
    }

    override fun reset() {
        sessionId = null
        streamUrl = null
        protocol = SessionProtocol.UNKNOWN
        startedAtMs = 0L

        playbackState = PlaybackState.IDLE
        playbackPositionMs = 0L
        bufferedDurationMs = 0L

        startupMs = null
        firstFrameMs = null
        bufferingStartedAtMs = null
        rebufferCount = 0
        rebufferDurationMs = 0L
        droppedFrames = 0
        fatalErrorCount = 0
        liveLatencyMs = null

        bitrateSwitchCount = 0
        currentBitrateKbps = null
        lastBitrateForSwitchKbps = null
        bitrateSamplesTotal = 0L
        bitrateSampleCount = 0

        currentThroughputKbps = null
        throughputSamplesTotal = 0L
        throughputSampleCount = 0
        latestNetworkActivityBytes = null

        networkSamples.clear()
        trackSnapshots.clear()
        issues.clear()
    }

    private fun averageBitrate(): Int? {
        if (bitrateSampleCount <= 0) return null
        return (bitrateSamplesTotal / bitrateSampleCount).toInt()
    }

    private fun averageThroughput(): Int? {
        if (throughputSampleCount <= 0) return null
        return (throughputSamplesTotal / throughputSampleCount).toInt()
    }

    private fun resolveProtocol(url: String): SessionProtocol {
        val normalizedUrl = url.trim().lowercase()
        return when {
            normalizedUrl.startsWith("rtsp://") -> SessionProtocol.RTSP
            normalizedUrl.contains(".m3u8") -> SessionProtocol.HLS
            normalizedUrl.contains(".mpd") -> SessionProtocol.DASH
            normalizedUrl.startsWith("http://") || normalizedUrl.startsWith("https://") -> SessionProtocol.PROGRESSIVE
            else -> SessionProtocol.UNKNOWN
        }
    }

    companion object {
        private const val DEFAULT_MAX_NETWORK_SAMPLES = 1_500
        private const val DEFAULT_MAX_TRACK_SNAPSHOTS = 400
        private const val DEFAULT_MAX_ISSUES = 200
    }
}
