package com.kabindra.tv.iptv.presentation.ui.screen.livetv.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.domain.entity.ChannelCategory
import com.kabindra.tv.iptv.domain.entity.LiveChannel
import com.kabindra.tv.iptv.domain.usecase.remote.livetv.LiveTVUseCase
import com.kabindra.tv.iptv.domain.usecase.xtream.livetv.LiveTVXtreamUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiveTVPlayerViewModel(
    private val liveTVUseCase: LiveTVUseCase,
    private val liveTVXtreamUseCase: LiveTVXtreamUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(LiveTVPlayerState())
    val state: StateFlow<LiveTVPlayerState> = _state.asStateFlow()

    init {
        getLiveTVCategories()
        getLiveTVChannels()
    }

    /*fun getLiveTVChannels() {
        viewModelScope.launch {
            liveTVUseCase.executeGetLiveTVContent().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        val categories = result.data
                        val firstCategory = categories.firstOrNull()
                        val firstChannel = firstCategory?.channels?.firstOrNull()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                                categories = categories,
                                selectedCategoryId = it.selectedCategoryId ?: firstCategory?.id,
                                selectedChannelId = it.selectedChannelId ?: firstChannel?.id
                            )
                        }
                    }

                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.error.message
                            )
                        }
                    }
                }
            }
        }
    }*/

    fun getLiveTVCategories() {
        viewModelScope.launch {
            liveTVXtreamUseCase.executeGetLiveTVCategories().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        println("LiveTVPlayerViewModel executeGetLiveTVCategories: Success ${result.data}")
                        val categories = result.data
                        val firstCategory = categories.firstOrNull()
                        // val firstChannel = firstCategory?.channels?.firstOrNull()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                                // categories = categories,
                                // selectedCategoryId = it.selectedCategoryId ?: firstCategory?.id,
                                // selectedChannelId = it.selectedChannelId ?: firstChannel?.id
                            )
                        }
                    }

                    is Result.Error -> {
                        println("LiveTVPlayerViewModel executeGetLiveTVCategories: Error ${result.error.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.error.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun getLiveTVChannels() {
        viewModelScope.launch {
            liveTVXtreamUseCase.executeGetLiveTVChannels().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        println("LiveTVPlayerViewModel executeGetLiveTVChannels: Success ${result.data}")
                        val categories = result.data
                        val firstCategory = categories.firstOrNull()
                        // val firstChannel = firstCategory?.channels?.firstOrNull()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                                // categories = categories,
                                // selectedCategoryId = it.selectedCategoryId ?: firstCategory?.id,
                                // selectedChannelId = it.selectedChannelId ?: firstChannel?.id
                            )
                        }
                    }

                    is Result.Error -> {
                        println("LiveTVPlayerViewModel executeGetMovieCategories: Error ${result.error.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.error.message
                            )
                        }
                    }
                }
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
}
