package com.kabindra.tv.iptv.presentation.ui.screen.livetv.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.tv.material3.MaterialTheme
import com.kabindra.player.PlayerCallbacks
import com.kabindra.player.PlayerContentType
import com.kabindra.player.PlayerControllerMode
import com.kabindra.player.PlayerExperience
import com.kabindra.player.PlayerFeatures
import com.kabindra.player.PlayerItem
import com.kabindra.player.PlayerPlaylist
import com.kabindra.player.PlayerProgramInfo
import com.kabindra.player.PlayerSourceType
import com.kabindra.player.UnifiedPlayer
import com.kabindra.player.defaultPlayerInteractionConfig
import com.kabindra.player.rememberPlayerHostState
import com.kabindra.tv.iptv.domain.entity.ChannelCategory
import com.kabindra.tv.iptv.domain.entity.LiveChannel
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazy
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyLayout
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyOrientation
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyPlatform
import com.kabindra.tv.iptv.presentation.ui.component.ButtonComponent
import com.kabindra.tv.iptv.presentation.ui.component.CardImageConfig
import com.kabindra.tv.iptv.presentation.ui.component.CardImageSource
import com.kabindra.tv.iptv.presentation.ui.component.ListItemComponent
import com.kabindra.tv.iptv.presentation.ui.component.ListItemType
import com.kabindra.tv.iptv.presentation.ui.component.LoadingIndicator
import com.kabindra.tv.iptv.presentation.ui.component.TextComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextSize
import com.kabindra.tv.iptv.presentation.ui.component.TextType
import com.kabindra.tv.iptv.presentation.ui.component.TvLazyConfig
import com.kabindra.tv.iptv.presentation.ui.component.rememberBaseLazyState
import com.kabindra.tv.iptv.utils.extensions.mainBackground
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

private object LiveTVScreenTokens {
    const val headerHorizontalPadding = 24
    const val headerVerticalPadding = 24
    const val overlayCategoryWidth = 120
    const val overlayChannelWidth = 200
    const val overlayHorizontalPadding = 18
    const val overlayInnerSpacing = 10
    const val sectionSpacing = 14
}

@Composable
fun LiveTVPlayerScreen(
    viewModel: LiveTVPlayerViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val allChannels =
        remember(state.categories) { state.categories.flatMap(ChannelCategory::channels) }
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }
        ?: state.categories.firstOrNull()
    val selectedChannel = allChannels.firstOrNull { it.id == state.selectedChannelId }
    val selectedChannelIndex = allChannels.indexOfFirst { it.id == selectedChannel?.id }
        .takeIf { it >= 0 }
        ?: 0
    val playerHostState = rememberPlayerHostState()

    BackHandler {
        if (state.isChannelOverlayVisible) {
            onBack()
        } else {
            viewModel.showChannelOverlay()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        if (allChannels.isNotEmpty()) {
            UnifiedPlayer(
                playlist = PlayerPlaylist(
                    items = allChannels.map(LiveChannel::toPlayerItem),
                    startIndex = selectedChannelIndex,
                    autoPlay = true,
                ),
                hostState = playerHostState,
                experience = PlayerExperience.AndroidTv,
                controllerMode = PlayerControllerMode.Custom,
                features = PlayerFeatures(
                    /*showBackButton = false,
                    showStreamDetails = true,
                    showPlayPauseButton = true,
                    showPreviousButton = true,
                    showNextButton = true,
                    showRewindButton = true,
                    showFastForwardButton = true,
                    showSeekBar = true,
                    showSubtitles = true,
                    showQualitySelector = true,
                    showAudioSelector = true,
                    showEpgAction = false,
                    showStatsForNerds = false,
                    showPlaybackSpeed = false,
                    showShuffleButton = false,
                    showLoopButton = false,
                    showGoLiveButton = true,*/
                    showBackButton = true,
                    showStreamDetails = true,
                    showPlayPauseButton = true,
                    showPreviousButton = true,
                    showNextButton = true,
                    showRewindButton = true,
                    showFastForwardButton = true,
                    showSeekBar = true,
                    showSubtitles = true,
                    showQualitySelector = true,
                    showAudioSelector = true,
                    showEpgAction = true,
                    showStatsForNerds = true,
                    showPlaybackSpeed = true,
                    showShuffleButton = true,
                    showLoopButton = true,
                    showGoLiveButton = true,
                ),
                interactionConfig = defaultPlayerInteractionConfig(PlayerExperience.AndroidTv),
                callbacks = PlayerCallbacks(
                    onItemChanged = { _, playerIndex ->
                        allChannels.getOrNull(playerIndex)?.let { channel ->
                            viewModel.selectChannel(channel.id, closeOverlay = false)
                        }
                    }
                ),
                modifier = Modifier.fillMaxSize(),
            )
        }

        when {
            state.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    isCircular = true,
                    useExpressive = true
                )
            }

            state.errorMessage.isNotBlank() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.sdp)
                ) {
                    TextComponent(
                        text = state.errorMessage,
                        type = TextType.Body,
                        size = TextSize.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ButtonComponent(
                        text = "Retry",
                        onClick = viewModel::loadContent
                    )
                }
            }
        }

        if (state.isChannelOverlayVisible && selectedCategory != null) {
            LiveTVOverlay(
                categories = state.categories,
                selectedCategoryId = selectedCategory.id,
                selectedChannelId = selectedChannel?.id,
                onCategorySelected = viewModel::selectCategory,
                onChannelSelected = { channelId ->
                    viewModel.selectChannel(channelId, closeOverlay = true)
                }
            )
        }
    }
}

@Composable
private fun LiveTVOverlay(
    categories: List<ChannelCategory>,
    selectedCategoryId: String,
    selectedChannelId: String?,
    onCategorySelected: (String) -> Unit,
    onChannelSelected: (String) -> Unit,
) {
    val selectedCategoryIndex = categories.indexOfFirst { it.id == selectedCategoryId }
        .takeIf { it >= 0 }
        ?: 0
    val selectedCategory = categories.getOrNull(selectedCategoryIndex) ?: return
    val selectedChannelIndex = selectedCategory.channels.indexOfFirst { it.id == selectedChannelId }
        .takeIf { it >= 0 }
        ?: 0
    val categoryState = rememberBaseLazyState()
    val channelState = key(selectedCategory.id) { rememberBaseLazyState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    top = 0.sdp,
                    bottom = 0.sdp,
                    start = 0.sdp
                )
        ) {
            Column(
                modifier = Modifier
                    .width(LiveTVScreenTokens.overlayCategoryWidth.sdp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .padding(LiveTVScreenTokens.overlayHorizontalPadding.sdp),
                verticalArrangement = Arrangement.spacedBy(LiveTVScreenTokens.sectionSpacing.sdp)
            ) {
                BaseLazy(
                    items = categories,
                    state = categoryState,
                    modifier = Modifier.fillMaxSize(),
                    layout = BaseLazyLayout.List,
                    orientation = BaseLazyOrientation.Vertical,
                    platform = BaseLazyPlatform.AndroidTv,
                    tvConfig = TvLazyConfig(
                        requestInitialFocus = false,
                        restoreFocus = true,
                        initialFocusedIndex = selectedCategoryIndex,
                        autoScrollOnFocus = true,
                    ),
                    contentPadding = PaddingValues(),
                    arrangement = Arrangement.spacedBy(6.sdp),
                    userScrollEnabled = true,
                    key = { _, item -> item.id },
                    contentType = { _, _ -> "live_tv_category" }
                ) { _, item, itemModifier ->
                    ListItemComponent(
                        modifier = itemModifier.fillMaxWidth(),
                        type = ListItemType.Default,
                        title = item.title,
                        selected = item.id == selectedCategoryId,
                        onClick = { onCategorySelected(item.id) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(LiveTVScreenTokens.overlayChannelWidth.sdp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f))
                    .padding(LiveTVScreenTokens.overlayHorizontalPadding.sdp),
                verticalArrangement = Arrangement.spacedBy(LiveTVScreenTokens.sectionSpacing.sdp)
            ) {
                BaseLazy(
                    items = selectedCategory.channels,
                    state = channelState,
                    modifier = Modifier.fillMaxSize(),
                    layout = BaseLazyLayout.List,
                    orientation = BaseLazyOrientation.Vertical,
                    platform = BaseLazyPlatform.AndroidTv,
                    tvConfig = TvLazyConfig(
                        requestInitialFocus = true,
                        restoreFocus = false,
                        initialFocusedIndex = selectedChannelIndex,
                        autoScrollOnFocus = true,
                    ),
                    contentPadding = PaddingValues(),
                    arrangement = Arrangement.spacedBy(6.sdp),
                    userScrollEnabled = true,
                    key = { _, item -> item.id },
                    contentType = { _, _ -> "live_tv_channel" }
                ) { _, item, itemModifier ->
                    ListItemComponent(
                        modifier = itemModifier.fillMaxWidth(),
                        type = ListItemType.Dense,
                        title = item.title,
                        supportingText = item.currentProgram,
                        leadingImageConfig = CardImageConfig(
                            source = CardImageSource.Url,
                            url = item.logoUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit
                        ),
                        selected = item.id == selectedChannelId,
                        onClick = { onChannelSelected(item.id) }
                    )
                }
            }
        }
    }
}

private fun LiveChannel.toPlayerItem(): PlayerItem {
    return PlayerItem(
        id = id,
        title = title,
        streamUrl = streamUrl,
        sourceType = streamType.toPlayerSourceType(),
        contentType = PlayerContentType.Live,
        posterUrl = logoUrl,
        programInfo = PlayerProgramInfo(
            channelName = title,
            currentTitle = currentProgram,
        ),
        isSeekable = streamType != MediaStreamType.Hls,
    )
}

private fun MediaStreamType.toPlayerSourceType(): PlayerSourceType {
    return when (this) {
        MediaStreamType.Hls -> PlayerSourceType.Hls
        MediaStreamType.Progressive -> PlayerSourceType.Progressive
    }
}
