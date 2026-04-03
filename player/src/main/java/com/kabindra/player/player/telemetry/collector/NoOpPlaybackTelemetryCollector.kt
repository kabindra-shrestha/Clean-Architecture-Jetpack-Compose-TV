package com.kabindra.player.player.telemetry.collector

import com.kabindra.player.player.telemetry.model.KpiSummary
import com.kabindra.player.player.telemetry.model.LivePlaybackSnapshot
import com.kabindra.player.player.telemetry.model.NetworkSample
import com.kabindra.player.player.telemetry.model.PlaybackIssue
import com.kabindra.player.player.telemetry.model.PlaybackSessionReport
import com.kabindra.player.player.telemetry.model.PlaybackState
import com.kabindra.player.player.telemetry.model.SessionProtocol
import com.kabindra.player.player.telemetry.model.TrackSnapshot

/**
 * Placeholder collector for Milestone 0.
 * Real metric extraction will be implemented in Milestone 1.
 */
class NoOpPlaybackTelemetryCollector : PlaybackTelemetryCollector {
    private var sessionId: String? = null
    private var streamUrl: String? = null
    private var startedAtMs: Long? = null

    override fun startSession(sessionId: String, streamUrl: String, startTimestampMs: Long) {
        this.sessionId = sessionId
        this.streamUrl = streamUrl
        this.startedAtMs = startTimestampMs
    }

    override fun onPlaybackStateChanged(
        playbackState: PlaybackState,
        playbackPositionMs: Long,
        bufferedDurationMs: Long,
        timestampMs: Long
    ) = Unit

    override fun onFirstFrameRendered(timestampMs: Long) = Unit

    override fun onDroppedFrames(droppedFrames: Int, elapsedMs: Long?, timestampMs: Long) = Unit

    override fun onBitrateSwitch(newBitrateKbps: Int, timestampMs: Long) = Unit

    override fun onLiveLatencyUpdated(liveLatencyMs: Long?, timestampMs: Long) = Unit

    override fun appendNetworkSample(sample: NetworkSample) = Unit

    override fun appendTrackSnapshot(snapshot: TrackSnapshot) = Unit

    override fun appendIssue(issue: PlaybackIssue, isFatal: Boolean) = Unit

    override fun currentSnapshot(): LivePlaybackSnapshot? = null

    override fun endSession(endTimestampMs: Long): PlaybackSessionReport? {
        val currentSessionId = sessionId ?: return null
        val currentStreamUrl = streamUrl ?: return null
        val currentStart = startedAtMs ?: return null

        return PlaybackSessionReport(
            sessionId = currentSessionId,
            streamUrl = currentStreamUrl,
            protocol = SessionProtocol.UNKNOWN,
            startedAtMs = currentStart,
            endedAtMs = endTimestampMs,
            kpiSummary = KpiSummary()
        )
    }

    override fun reset() {
        sessionId = null
        streamUrl = null
        startedAtMs = null
    }
}
