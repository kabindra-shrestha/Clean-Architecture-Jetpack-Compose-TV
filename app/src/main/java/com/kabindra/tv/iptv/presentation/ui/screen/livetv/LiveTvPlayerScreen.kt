package com.kabindra.tv.iptv.presentation.ui.screen.livetv

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
import com.kabindra.player.component.Media3PlayerComponent
import com.kabindra.player.model.PlayerMediaItem
import com.kabindra.player.model.PlayerStreamType
import com.kabindra.player.model.PlayerUiConfig
import com.kabindra.tv.iptv.domain.entity.media.ChannelCategory
import com.kabindra.tv.iptv.domain.entity.media.LiveChannel
import com.kabindra.tv.iptv.domain.entity.media.MediaStreamType
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
import com.kabindra.tv.iptv.presentation.viewmodel.media.LiveTvViewModel
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

private object LiveTvScreenTokens {
    const val headerHorizontalPadding = 24
    const val headerTopPadding = 24
    const val overlayCategoryWidth = 142
    const val overlayChannelWidth = 252
    const val overlayTopPadding = 36
    const val overlayBottomPadding = 24
    const val overlayHorizontalPadding = 18
    const val overlayInnerSpacing = 10
    const val sectionSpacing = 14
}

@Composable
fun LiveTvPlayerScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    viewModel: LiveTvViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val allChannels = remember(state.categories) { state.categories.flatMap(ChannelCategory::channels) }
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }
        ?: state.categories.firstOrNull()
    val selectedChannel = allChannels.firstOrNull { it.id == state.selectedChannelId }
    val selectedChannelIndex = allChannels.indexOfFirst { it.id == selectedChannel?.id }
        .takeIf { it >= 0 }
        ?: 0

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
            .background(Color.Black)
            .padding(innerPadding)
    ) {
        if (allChannels.isNotEmpty()) {
            Media3PlayerComponent(
                items = allChannels.map(LiveChannel::toPlayerMediaItem),
                selectedIndex = selectedChannelIndex,
                onSelectedIndexChange = { playerIndex ->
                    allChannels.getOrNull(playerIndex)?.let { channel ->
                        viewModel.selectChannel(channel.id, closeOverlay = false)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                uiConfig = PlayerUiConfig()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = LiveTvScreenTokens.headerHorizontalPadding.sdp,
                    top = LiveTvScreenTokens.headerTopPadding.sdp
                ),
            verticalArrangement = Arrangement.spacedBy(4.sdp)
        ) {
            TextComponent(
                text = "LIVE TV",
                type = TextType.Label,
                size = TextSize.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
            TextComponent(
                text = selectedChannel?.title ?: "Loading Channel",
                type = TextType.Headline,
                size = TextSize.Large,
                color = MaterialTheme.colorScheme.onSurface
            )
            selectedChannel?.let { channel ->
                TextComponent(
                    text = channel.currentProgram,
                    type = TextType.Body,
                    size = TextSize.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                )
            }
        }

        when {
            state.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    isCircular = true,
                    useExpressive = true
                )
            }

            !state.errorMessage.isNullOrBlank() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.sdp)
                ) {
                    TextComponent(
                        text = state.errorMessage ?: "Unable to load Live TV.",
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
            LiveTvOverlay(
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
private fun LiveTvOverlay(
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
                    top = LiveTvScreenTokens.overlayTopPadding.sdp,
                    bottom = LiveTvScreenTokens.overlayBottomPadding.sdp,
                    start = 0.sdp
                )
        ) {
            Column(
                modifier = Modifier
                    .width(LiveTvScreenTokens.overlayCategoryWidth.sdp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                    .padding(LiveTvScreenTokens.overlayHorizontalPadding.sdp),
                verticalArrangement = Arrangement.spacedBy(LiveTvScreenTokens.sectionSpacing.sdp)
            ) {
                TextComponent(
                    text = "Categories",
                    type = TextType.Title,
                    size = TextSize.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f)
                )
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
                    .width(LiveTvScreenTokens.overlayChannelWidth.sdp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f))
                    .padding(LiveTvScreenTokens.overlayHorizontalPadding.sdp),
                verticalArrangement = Arrangement.spacedBy(LiveTvScreenTokens.sectionSpacing.sdp)
            ) {
                TextComponent(
                    text = "Channels",
                    type = TextType.Title,
                    size = TextSize.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f)
                )
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

private fun LiveChannel.toPlayerMediaItem(): PlayerMediaItem {
    return PlayerMediaItem(
        id = id,
        title = title,
        streamUrl = streamUrl,
        streamType = streamType.toPlayerStreamType(),
        posterUrl = logoUrl
    )
}

private fun MediaStreamType.toPlayerStreamType(): PlayerStreamType {
    return when (this) {
        MediaStreamType.Hls -> PlayerStreamType.Hls
        MediaStreamType.Progressive -> PlayerStreamType.Progressive
    }
}
