package com.kabindra.player.player.telemetry.model

data class PlaybackIssue(
    val timestampMs: Long,
    val category: String,
    val message: String,
    val code: Int? = null
)