package com.kabindra.player.player.telemetry.model

data class KpiSummary(
    val startupMs: Long? = null,
    val firstFrameMs: Long? = null,
    val averageBitrateKbps: Int? = null,
    val averageThroughputKbps: Int? = null,
    val bitrateSwitchCount: Int = 0,
    val rebufferCount: Int = 0,
    val rebufferDurationMs: Long = 0L,
    val droppedFrames: Int = 0,
    val liveLatencyMs: Long? = null,
    val fatalErrorCount: Int = 0
)