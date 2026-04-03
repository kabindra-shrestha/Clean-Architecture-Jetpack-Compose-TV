package com.kabindra.player.player.telemetry.model

data class TrackSnapshot(
    val timestampMs: Long,
    val videoCodec: String? = null,
    val videoMimeType: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val frameRate: Float? = null,
    val videoBitrateKbps: Int? = null,
    val audioCodec: String? = null,
    val audioMimeType: String? = null,
    val audioBitrateKbps: Int? = null,
    val audioChannels: Int? = null,
    val audioSampleRateHz: Int? = null,
    val drmType: String? = null
)