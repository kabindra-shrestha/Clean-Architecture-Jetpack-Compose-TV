package com.kabindra.player.player

import androidx.compose.runtime.Composable

@Deprecated(
    message = "Use com.kabindra.player.UnifiedPlayer(...) instead.",
    replaceWith = ReplaceWith("UnifiedPlayer(...)")
)
@Suppress("DEPRECATION")
@Composable
fun Player(
    videoURL: String,
    showStatsForNerds: Boolean,
    playbackRequestToken: Int = 0,
) {
    com.kabindra.player.Player(
        videoURL = videoURL,
        showStatsForNerds = showStatsForNerds,
        playbackRequestToken = playbackRequestToken
    )
}
