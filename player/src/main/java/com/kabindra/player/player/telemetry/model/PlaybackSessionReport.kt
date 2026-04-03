package com.kabindra.player.player.telemetry.model

data class PlaybackSessionReport(
    val sessionId: String,
    val streamUrl: String,
    val protocol: SessionProtocol,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val kpiSummary: KpiSummary,
    val trackSnapshots: List<TrackSnapshot> = emptyList(),
    val networkSamples: List<NetworkSample> = emptyList(),
    val issues: List<PlaybackIssue> = emptyList()
) {
    val durationMs: Long
        get() = (endedAtMs - startedAtMs).coerceAtLeast(0L)
}