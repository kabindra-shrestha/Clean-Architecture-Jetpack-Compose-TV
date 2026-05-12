package com.kabindra.tv.iptv.presentation.ui.screen.livetv.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.data.source.UserCredentialsProvider
import com.kabindra.tv.iptv.domain.entity.ChannelCategory
import com.kabindra.tv.iptv.domain.entity.LiveChannel
import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import com.kabindra.tv.iptv.domain.entity.MediaPlaybackType
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.domain.usecase.room.LiveTVRoomUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiveTVPlayerViewModel(
    private val liveTVRoomUseCase: LiveTVRoomUseCase,
    private val userCredentialsProvider: UserCredentialsProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(LiveTVPlayerState())
    val state: StateFlow<LiveTVPlayerState> = _state.asStateFlow()

    private var userCredentials = User()
    private var categories = listOf<LiveTVCategory>()
    private var channels = listOf<LiveTV>()

    init {
        observeLiveTVContent()
    }

    fun observeLiveTVContent() {
        viewModelScope.launch {
            combine(
                liveTVRoomUseCase.observeLiveTVCategories(),
                liveTVRoomUseCase.observeLiveTVChannels(),
                userCredentialsProvider.observeCurrentUser(),
            ) { categoryResult, channelResult, user ->
                Triple(categoryResult, channelResult, user)
            }.collect { (categoryResult, channelResult, user) ->
                val categoryError = (categoryResult as? Result.Error)?.error?.message
                val channelError = (channelResult as? Result.Error)?.error?.message
                val errorMessage = categoryError ?: channelError

                if (errorMessage != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEmpty = false,
                            errorMessage = errorMessage,
                        )
                    }
                    return@collect
                }

                if (categoryResult !is Result.Success || channelResult !is Result.Success) {
                    _state.update {
                        if (it.categories.isEmpty()) {
                            it.copy(isLoading = true, isEmpty = false, errorMessage = "")
                        } else {
                            it
                        }
                    }
                    return@collect
                }

                userCredentials = user ?: User()
                categories = categoryResult.data
                channels = channelResult.data

                val mappedCategories = mapToChannelCategories(categories, channels)
                val firstCategory = mappedCategories.firstOrNull()
                val selectedCategory = _state.value.selectedCategoryId
                    ?.let { selectedId -> mappedCategories.firstOrNull { it.id == selectedId } }
                    ?: firstCategory
                val selectedChannel = _state.value.selectedChannelId
                    ?.let { selectedId ->
                        mappedCategories
                            .flatMap(ChannelCategory::channels)
                            .firstOrNull { it.id == selectedId }
                    }
                    ?: selectedCategory?.channels?.firstOrNull()
                    ?: firstCategory?.channels?.firstOrNull()

                _state.update {
                    it.copy(
                        isLoading = false,
                        isEmpty = mappedCategories.isEmpty(),
                        errorMessage = "",
                        categories = mappedCategories,
                        selectedCategoryId = selectedCategory?.id,
                        selectedChannelId = selectedChannel?.id,
                    )
                }
            }
        }
    }

    private fun mapToChannelCategories(
        categories: List<LiveTVCategory>,
        channels: List<LiveTV>
    ): List<ChannelCategory> {
        val channelMap = channels.groupBy { it.category_id }

        return categories.mapNotNull { cat ->
            val mappedChannels = channelMap[cat.category_id]?.mapNotNull { channel ->
                val streamId = channel.stream_id?.toString() ?: return@mapNotNull null
                val streamUrl = if (!channel.direct_source.isNullOrEmpty()) {
                    channel.direct_source
                } else {
                    buildStreamUrl(
                        serverName = userCredentials.server_name ?: "",
                        username = userCredentials.username ?: "",
                        password = userCredentials.password ?: "",
                        streamId = streamId
                    )
                }
                LiveChannel(
                    id = streamId,
                    categoryId = channel.category_id ?: "",
                    title = channel.name ?: "",
                    currentProgram = "",
                    streamUrl = streamUrl,
                    streamType = MediaStreamType.Progressive,
                    playbackType = MediaPlaybackType.Live,
                    logoUrl = channel.stream_icon ?: ""
                )
            } ?: emptyList()

            // Only include categories that have channels
            if (mappedChannels.isNotEmpty()) {
                ChannelCategory(
                    id = cat.category_id ?: "",
                    title = cat.category_name ?: "",
                    channels = mappedChannels
                )
            } else {
                null
            }
        }
    }

    fun selectCategory(categoryId: String) {
        val category = _state.value.categories.firstOrNull { it.id == categoryId } ?: return
        _state.update {
            it.copy(
                selectedCategoryId = category.id,
                selectedChannelId = category.channels.firstOrNull()?.id
            )
        }
    }

    fun selectChannel(channelId: String, closeOverlay: Boolean = true) {
        val match = findChannel(channelId) ?: return
        _state.update {
            it.copy(
                selectedCategoryId = match.first.id,
                selectedChannelId = match.second.id,
                isChannelOverlayVisible = if (closeOverlay) false else it.isChannelOverlayVisible
            )
        }
    }

    fun selectRelativeChannel(offset: Int) {
        if (offset == 0) return

        val allChannels = _state.value.categories.flatMap(ChannelCategory::channels)
        if (allChannels.isEmpty()) return

        val currentIndex = allChannels.indexOfFirst { it.id == _state.value.selectedChannelId }
            .takeIf { it >= 0 }
            ?: 0
        val targetIndex = (currentIndex + offset).floorMod(allChannels.size)
        val targetChannel = allChannels[targetIndex]
        selectChannel(targetChannel.id, closeOverlay = true)
    }

    fun showChannelOverlay() {
        _state.update { it.copy(isChannelOverlayVisible = true) }
    }

    fun hideChannelOverlay() {
        _state.update { it.copy(isChannelOverlayVisible = false) }
    }

    private fun findChannel(channelId: String): Pair<ChannelCategory, LiveChannel>? {
        state.value.categories.forEach { category ->
            val channel = category.channels.firstOrNull { it.id == channelId }
            if (channel != null) {
                return category to channel
            }
        }
        return null
    }

    private fun Int.floorMod(mod: Int): Int {
        return ((this % mod) + mod) % mod
    }

    private fun buildStreamUrl(
        serverName: String,
        username: String,
        password: String,
        streamId: String,
    ): String {
        return "http://$serverName/live/$username/$password/$streamId.ts"
    }
}
