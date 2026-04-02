package com.kabindra.tv.iptv.presentation.ui.screen.movie.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieBrowseUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieContentViewModel(
    private val movieBrowseUseCase: MovieBrowseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieContentState())
    val state: StateFlow<MovieContentState> = _state.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            movieBrowseUseCase.executeGetMovieCategories().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        val categories = result.data
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                                categories = categories,
                                selectedCategoryId = it.selectedCategoryId
                                    ?: categories.firstOrNull()?.id
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
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }
}
