package com.kabindra.mobile.iptv.presentation.ui.screen.livetv.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileAdaptiveContent
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileWindowSizeClass
import com.kabindra.mobile.iptv.presentation.ui.adaptive.plus
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileCategoryChips
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileEmptyState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileErrorState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileLoadingState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileScreenHeader
import com.kabindra.mobile.iptv.utils.extensions.mainBackground
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
import com.kabindra.tv.iptv.domain.entity.MediaPlaybackType
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.presentation.ui.screen.livetv.player.LiveTVPlayerViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LiveTVPlayerScreen(
    viewModel: LiveTVPlayerViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onImmersiveChanged: (Boolean) -> Unit = {},
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
    val interactionConfig = remember { defaultPlayerInteractionConfig(PlayerExperience.AndroidOtt) }
    val playerFeatures = remember {
        PlayerFeatures(
            showBackButton = true,
            showStreamDetails = true,
            showPlayPauseButton = true,
            showPreviousButton = true,
            showNextButton = true,
            showRewindButton = false,
            showFastForwardButton = false,
            showSeekBar = false,
            showSubtitles = true,
            showQualitySelector = true,
            showAudioSelector = true,
            showEpgAction = false,
            showStatsForNerds = true,
            showPlaybackSpeed = false,
            showShuffleButton = false,
            showLoopButton = false,
            showGoLiveButton = true,
        )
    }
    val playerItems = remember(allChannels) { allChannels.map(LiveChannel::toPlayerItem) }
    val playerPlaylist = remember(playerItems, selectedChannelIndex) {
        PlayerPlaylist(
            items = playerItems,
            startIndex = selectedChannelIndex,
            autoPlay = true,
            circularNavigation = true,
        )
    }
    var isFullscreen by remember { mutableStateOf(false) }
    var isMinimized by remember { mutableStateOf(false) }

    LaunchedEffect(isFullscreen) {
        onImmersiveChanged(isFullscreen)
    }

    BackHandler {
        when {
            isFullscreen -> isFullscreen = false
            isMinimized -> isMinimized = false
            playerHostState.consumeBackPress() -> Unit
            else -> onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        when {
            state.isLoading -> MobileLoadingState(message = "Loading live channels...")
            state.errorMessage.isNotBlank() -> MobileErrorState(
                message = state.errorMessage,
                onActionClick = viewModel::observeLiveTVContent,
            )

            state.isEmpty || allChannels.isEmpty() -> {
                MobileEmptyState(message = "Live TV data is being prepared.")
            }

            isFullscreen -> {
                LivePlayerSurface(
                    playlist = playerPlaylist,
                    selectedChannel = selectedChannel,
                    playerHostState = playerHostState,
                    features = playerFeatures,
                    modifier = Modifier.fillMaxSize(),
                    onBack = { isFullscreen = false },
                    onFullscreenToggle = { isFullscreen = false },
                    onMinimizeToggle = {
                        isFullscreen = false
                        isMinimized = true
                    },
                    isFullscreen = true,
                    isMinimized = false,
                    callbacks = PlayerCallbacks(
                        onBack = { isFullscreen = false },
                        onItemChanged = { _, playerIndex ->
                            allChannels.getOrNull(playerIndex)?.let { channel ->
                                viewModel.selectChannel(channel.id, closeOverlay = false)
                            }
                        },
                    ),
                )
            }

            else -> {
                MobileAdaptiveContent {
                    val useSideBySide =
                        it.widthClass != MobileWindowSizeClass.Compact || it.isLandscape
                    val contentPadding = innerPadding.plus(
                        horizontal = it.horizontalPadding,
                        vertical = it.verticalPadding,
                    )

                    if (useSideBySide) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding),
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                MobileScreenHeader(
                                    title = "Live TV",
                                    subtitle = selectedChannel?.title
                                        ?: "Choose a channel to start watching.",
                                )
                                LivePlayerSurface(
                                    playlist = playerPlaylist,
                                    selectedChannel = selectedChannel,
                                    playerHostState = playerHostState,
                                    features = playerFeatures,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isMinimized) Modifier.height(112.dp)
                                            else Modifier.aspectRatio(16f / 9f)
                                        ),
                                    onBack = onBack,
                                    onFullscreenToggle = {
                                        isMinimized = false
                                        isFullscreen = true
                                    },
                                    onMinimizeToggle = { isMinimized = !isMinimized },
                                    isFullscreen = false,
                                    isMinimized = isMinimized,
                                    callbacks = PlayerCallbacks(
                                        onBack = onBack,
                                        onItemChanged = { _, playerIndex ->
                                            allChannels.getOrNull(playerIndex)?.let { channel ->
                                                viewModel.selectChannel(
                                                    channel.id,
                                                    closeOverlay = false
                                                )
                                            }
                                        },
                                    ),
                                )
                            }

                            LiveChannelBrowser(
                                categories = state.categories,
                                selectedCategory = selectedCategory,
                                selectedChannel = selectedChannel,
                                onCategorySelected = { category -> viewModel.selectCategory(category.id) },
                                onChannelSelected = { channel ->
                                    isMinimized = false
                                    viewModel.selectChannel(channel.id, closeOverlay = true)
                                },
                                modifier = Modifier
                                    .widthIn(min = 300.dp, max = 420.dp)
                                    .fillMaxHeight(),
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = contentPadding,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            item(key = "header") {
                                MobileScreenHeader(
                                    title = "Live TV",
                                    subtitle = selectedChannel?.title
                                        ?: "Choose a channel to start watching.",
                                )
                            }
                            item(key = "player") {
                                LivePlayerSurface(
                                    playlist = playerPlaylist,
                                    selectedChannel = selectedChannel,
                                    playerHostState = playerHostState,
                                    features = playerFeatures,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isMinimized) Modifier.height(104.dp)
                                            else Modifier.aspectRatio(16f / 9f)
                                        ),
                                    onBack = onBack,
                                    onFullscreenToggle = {
                                        isMinimized = false
                                        isFullscreen = true
                                    },
                                    onMinimizeToggle = { isMinimized = !isMinimized },
                                    isFullscreen = false,
                                    isMinimized = isMinimized,
                                    callbacks = PlayerCallbacks(
                                        onBack = onBack,
                                        onItemChanged = { _, playerIndex ->
                                            allChannels.getOrNull(playerIndex)?.let { channel ->
                                                viewModel.selectChannel(
                                                    channel.id,
                                                    closeOverlay = false
                                                )
                                            }
                                        },
                                    ),
                                )
                            }
                            item(key = "categories") {
                                if (state.categories.isNotEmpty()) {
                                    MobileCategoryChips(
                                        items = state.categories,
                                        selectedItem = selectedCategory,
                                        label = { category -> category.title },
                                        key = { category -> category.id },
                                        onSelected = { category -> viewModel.selectCategory(category.id) },
                                    )
                                }
                            }
                            items(
                                items = selectedCategory?.channels.orEmpty(),
                                key = { channel -> channel.id },
                            ) { channel ->
                                LiveChannelRow(
                                    channel = channel,
                                    selected = channel.id == selectedChannel?.id,
                                    onClick = {
                                        isMinimized = false
                                        viewModel.selectChannel(channel.id, closeOverlay = true)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LivePlayerSurface(
    playlist: PlayerPlaylist,
    selectedChannel: LiveChannel?,
    playerHostState: com.kabindra.player.PlayerHostState,
    features: PlayerFeatures,
    callbacks: PlayerCallbacks,
    isFullscreen: Boolean,
    isMinimized: Boolean,
    onBack: () -> Unit,
    onFullscreenToggle: () -> Unit,
    onMinimizeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier,
        shape = if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Black),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            UnifiedPlayer(
                playlist = playlist,
                hostState = playerHostState,
                experience = PlayerExperience.AndroidOtt,
                controllerMode = PlayerControllerMode.Custom,
                features = features,
                interactionConfig = defaultPlayerInteractionConfig(PlayerExperience.AndroidOtt),
                callbacks = callbacks,
                modifier = Modifier.fillMaxSize(),
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                FilledTonalIconButton(onClick = onMinimizeToggle) {
                    Icon(
                        imageVector = if (isMinimized) Icons.Default.OpenInFull else Icons.Default.PictureInPictureAlt,
                        contentDescription = if (isMinimized) "Expand player" else "Minimize player",
                    )
                }
            }

            FilledTonalIconButton(
                onClick = onFullscreenToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            ) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.CloseFullscreen else Icons.Default.Fullscreen,
                    contentDescription = if (isFullscreen) "Exit fullscreen" else "Fullscreen",
                )
            }

            if (isMinimized) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.62f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Text(
                        text = selectedChannel?.title ?: "Live TV",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveChannelBrowser(
    categories: List<ChannelCategory>,
    selectedCategory: ChannelCategory?,
    selectedChannel: LiveChannel?,
    onCategorySelected: (ChannelCategory) -> Unit,
    onChannelSelected: (LiveChannel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Channels",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            MobileCategoryChips(
                items = categories,
                selectedItem = selectedCategory,
                label = { category -> category.title },
                key = { category -> category.id },
                onSelected = onCategorySelected,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = selectedCategory?.channels.orEmpty(),
                    key = { channel -> channel.id },
                ) { channel ->
                    LiveChannelRow(
                        channel = channel,
                        selected = channel.id == selectedChannel?.id,
                        onClick = { onChannelSelected(channel) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveChannelRow(
    channel: LiveChannel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
            },
            contentColor = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = channel.logoUrl.takeIf(String::isNotBlank),
                        contentDescription = channel.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            },
            headlineContent = {
                Text(
                    text = channel.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            supportingContent = {
                Text(
                    text = channel.currentProgram.ifBlank { "Live channel" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
    }
}

private fun LiveChannel.toPlayerItem(): PlayerItem {
    return PlayerItem(
        id = id,
        title = title,
        streamUrl = streamUrl,
        sourceType = streamType.toPlayerSourceType(),
        contentType = playbackType.toPlayerContentType(),
        posterUrl = logoUrl,
        programInfo = PlayerProgramInfo(
            channelName = title,
            currentTitle = currentProgram,
        ),
        subtitle = currentProgram,
        isSeekable = playbackType != MediaPlaybackType.Live,
    )
}

private fun MediaStreamType.toPlayerSourceType(): PlayerSourceType {
    return when (this) {
        MediaStreamType.Hls -> PlayerSourceType.Hls
        MediaStreamType.Progressive -> PlayerSourceType.Progressive
    }
}

private fun MediaPlaybackType.toPlayerContentType(): PlayerContentType {
    return when (this) {
        MediaPlaybackType.Live -> PlayerContentType.Live
        MediaPlaybackType.Dvr -> PlayerContentType.Dvr
        MediaPlaybackType.Movie -> PlayerContentType.Vod
    }
}
