package com.kabindra.player.player.telemetry.model

data class NetworkSample(
    val timestampMs: Long,
    val throughputKbps: Int? = null,
    val bufferMs: Long? = null,
    val responseCode: Int? = null,
    val host: String? = null,
    val segmentUri: String? = null,
    val transferBytes: Long? = null,
    val transferDurationMs: Long? = null
)