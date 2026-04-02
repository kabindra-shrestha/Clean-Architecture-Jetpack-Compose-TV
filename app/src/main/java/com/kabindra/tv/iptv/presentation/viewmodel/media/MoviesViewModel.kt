package com.kabindra.tv.iptv.presentation.viewmodel.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.domain.entity.media.MovieCategory
import com.kabindra.tv.iptv.domain.usecase.media.MoviesBrowseUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoviesState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val categories: List<MovieCategory> = emptyList(),
    val selectedCategoryId: String? = null,
)

class MoviesViewModel(
    private val moviesBrowseUseCase: MoviesBrowseUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MoviesState())
    val state: StateFlow<MoviesState> = _state.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            moviesBrowseUseCase.executeGetMovieCategories().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = null) }
                    }

                    is Result.Success -> {
                        val categories = result.data
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                categories = categories,
                                selectedCategoryId = it.selectedCategoryId ?: categories.firstOrNull()?.id
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
