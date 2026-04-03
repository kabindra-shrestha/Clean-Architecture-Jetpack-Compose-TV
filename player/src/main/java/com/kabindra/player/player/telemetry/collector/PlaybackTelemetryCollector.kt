package com.kabindra.player.player.telemetry.collector

import com.kabindra.player.player.telemetry.model.LivePlaybackSnapshot
import com.kabindra.player.player.telemetry.model.NetworkSample
import com.kabindra.player.player.telemetry.model.PlaybackIssue
import com.kabindra.player.player.telemetry.model.PlaybackSessionReport
import com.kabindra.player.player.telemetry.model.PlaybackState
import com.kabindra.player.player.telemetry.model.TrackSnapshot

/**
 * Contract used by player integrations to produce structured telemetry.
 */
interface PlaybackTelemetryCollector {
    fun startSession(
        sessionId: String,
        streamUrl: String,
        startTimestampMs: Long = System.currentTimeMillis()
    )

    fun onPlaybackStateChanged(
        playbackState: PlaybackState,
        playbackPositionMs: Long,
        bufferedDurationMs: Long,
        timestampMs: Long = System.currentTimeMillis()
    )

    fun onFirstFrameRendered(timestampMs: Long = System.currentTimeMillis())

    fun onDroppedFrames(
        droppedFrames: Int,
        elapsedMs: Long? = null,
        timestampMs: Long = System.currentTimeMillis()
    )

    fun onBitrateSwitch(newBitrateKbps: Int, timestampMs: Long = System.currentTimeMillis())

    fun onLiveLatencyUpdated(liveLatencyMs: Long?, timestampMs: Long = System.currentTimeMillis())

    fun appendNetworkSample(sample: NetworkSample)
    fun appendTrackSnapshot(snapshot: TrackSnapshot)
    fun appendIssue(issue: PlaybackIssue, isFatal: Boolean = false)
    fun currentSnapshot(): LivePlaybackSnapshot?
    fun endSession(endTimestampMs: Long = System.currentTimeMillis()): PlaybackSessionReport?
    fun reset()
}
