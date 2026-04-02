package com.kabindra.player.model

enum class PlayerStreamType {
    Hls,
    Progressive,
}

data class PlayerMediaItem(
    val id: String,
    val title: String,
    val streamUrl: String,
    val streamType: PlayerStreamType,
    val posterUrl: String? = null,
)

data class PlayerUiConfig(
    val useDefaultController: Boolean = true,
    val keepScreenOn: Boolean = true,
    val playWhenReady: Boolean = true,
)
