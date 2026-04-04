package com.kabindra.player.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.kabindra.player.PlayerCallbacks
import com.kabindra.player.PlayerContentType
import com.kabindra.player.PlayerControllerMode
import com.kabindra.player.PlayerItem
import com.kabindra.player.PlayerPlaylist
import com.kabindra.player.PlayerSourceType
import com.kabindra.player.UnifiedPlayer
import com.kabindra.player.model.PlayerMediaItem
import com.kabindra.player.model.PlayerStreamType
import com.kabindra.player.model.PlayerUiConfig
import com.kabindra.player.rememberPlayerHostState

@Deprecated(
    message = "Use UnifiedPlayer(...) with PlayerPlaylist and PlayerFeatures instead.",
    replaceWith = ReplaceWith("UnifiedPlayer()"),
)
@Composable
fun Media3PlayerComponent(
    items: List<PlayerMediaItem>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    uiConfig: PlayerUiConfig = PlayerUiConfig(),
) {
    val hostState = rememberPlayerHostState(items, selectedIndex, uiConfig)

    LaunchedEffect(hostState.uiState.currentIndex, items) {
        val currentIndex = hostState.uiState.currentIndex
        if (currentIndex in items.indices) {
            onSelectedIndexChange(currentIndex)
        }
    }

    UnifiedPlayer(
        playlist = PlayerPlaylist(
            items = items.map(PlayerMediaItem::toUnifiedPlayerItem),
            startIndex = selectedIndex,
            autoPlay = uiConfig.playWhenReady,
        ),
        hostState = hostState,
        controllerMode = if (uiConfig.useDefaultController) {
            PlayerControllerMode.Default
        } else {
            PlayerControllerMode.Custom
        },
        callbacks = PlayerCallbacks(
            onItemChanged = { _, index -> onSelectedIndexChange(index) }
        ),
        modifier = modifier,
    )
}

private fun PlayerMediaItem.toUnifiedPlayerItem(): PlayerItem {
    return PlayerItem(
        id = id,
        title = title,
        streamUrl = streamUrl,
        sourceType = when (streamType) {
            PlayerStreamType.Hls -> PlayerSourceType.Hls
            PlayerStreamType.Progressive -> PlayerSourceType.Progressive
        },
        contentType = when (streamType) {
            PlayerStreamType.Hls -> PlayerContentType.Live
            PlayerStreamType.Progressive -> PlayerContentType.Vod
        },
        posterUrl = posterUrl,
        isSeekable = streamType == PlayerStreamType.Progressive,
    )
}
