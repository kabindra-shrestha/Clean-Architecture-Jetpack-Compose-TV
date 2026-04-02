package com.kabindra.player.component

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.kabindra.player.model.PlayerMediaItem
import com.kabindra.player.model.PlayerStreamType
import com.kabindra.player.model.PlayerUiConfig

@Composable
fun Media3PlayerComponent(
    items: List<PlayerMediaItem>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    uiConfig: PlayerUiConfig = PlayerUiConfig(),
) {
    val context = LocalContext.current
    val currentItems by rememberUpdatedState(items)
    val currentOnSelectedIndexChange by rememberUpdatedState(onSelectedIndexChange)
    val playlistSignature = remember(items) {
        items.map { item ->
            listOf(
                item.id,
                item.title,
                item.streamUrl,
                item.streamType.name,
                item.posterUrl.orEmpty()
            )
        }
    }
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentIndex = exoPlayer.currentMediaItemIndex
                if (currentIndex in currentItems.indices) {
                    currentOnSelectedIndexChange(currentIndex)
                }
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(playlistSignature) {
        if (items.isEmpty()) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            return@LaunchedEffect
        }

        val targetIndex = selectedIndex.coerceIn(0, items.lastIndex)
        exoPlayer.setMediaItems(items.map(PlayerMediaItem::toMedia3Item), targetIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = uiConfig.playWhenReady
    }

    LaunchedEffect(selectedIndex, uiConfig.playWhenReady, playlistSignature) {
        if (items.isEmpty()) return@LaunchedEffect

        val targetIndex = selectedIndex.coerceIn(0, items.lastIndex)
        if (exoPlayer.currentMediaItemIndex != targetIndex) {
            exoPlayer.seekToDefaultPosition(targetIndex)
            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                exoPlayer.prepare()
            }
        }
        exoPlayer.playWhenReady = uiConfig.playWhenReady
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                useController = uiConfig.useDefaultController
                controllerAutoShow = uiConfig.useDefaultController
                keepScreenOn = uiConfig.keepScreenOn
                player = exoPlayer
            }
        },
        update = { playerView ->
            playerView.player = exoPlayer
            playerView.useController = uiConfig.useDefaultController
            playerView.controllerAutoShow = uiConfig.useDefaultController
            playerView.keepScreenOn = uiConfig.keepScreenOn
        }
    )
}

private fun PlayerMediaItem.toMedia3Item(): MediaItem {
    val builder = MediaItem.Builder()
        .setMediaId(id)
        .setUri(Uri.parse(streamUrl))
        .setMediaMetadata(
            androidx.media3.common.MediaMetadata.Builder()
                .setTitle(title)
                .build()
        )

    if (streamType == PlayerStreamType.Hls) {
        builder.setMimeType(MimeTypes.APPLICATION_M3U8)
    }

    return builder.build()
}
