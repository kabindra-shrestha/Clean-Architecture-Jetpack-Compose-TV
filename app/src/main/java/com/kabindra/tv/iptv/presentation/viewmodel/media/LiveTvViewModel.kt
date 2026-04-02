package com.kabindra.tv.iptv.presentation.viewmodel.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.domain.entity.media.ChannelCategory
import com.kabindra.tv.iptv.domain.entity.media.LiveChannel
import com.kabindra.tv.iptv.domain.usecase.media.LiveTvUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LiveTvState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val categories: List<ChannelCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedChannelId: String? = null,
    val isChannelOverlayVisible: Boolean = false,
)

class LiveTvViewModel(
    private val liveTvUseCase: LiveTvUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(LiveTvState())
    val state: StateFlow<LiveTvState> = _state.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            liveTvUseCase.executeGetLiveTvContent().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = null) }
                    }

                    is Result.Success -> {
                        val categories = result.data
                        val firstCategory = categories.firstOrNull()
                        val firstChannel = firstCategory?.channels?.firstOrNull()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
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
}
