package com.kabindra.tv.iptv.presentation.viewmodel.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.domain.entity.MovieDetail
import com.kabindra.tv.iptv.domain.usecase.movie.MovieDetailUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoviePlayerState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val movie: MovieDetail? = null,
    val currentMovieId: String? = null,
)

class MoviePlayerViewModel(
    private val movieDetailUseCase: MovieDetailUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MoviePlayerState())
    val state: StateFlow<MoviePlayerState> = _state.asStateFlow()

    fun loadMovie(movieId: String) {
        if (_state.value.currentMovieId == movieId && _state.value.movie != null) return

        viewModelScope.launch {
            movieDetailUseCase.executeGetMovieDetail(movieId).collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                errorMessage = null,
                                currentMovieId = movieId
                            )
                        }
                    }

                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                movie = result.data,
                                currentMovieId = movieId
                            )
                        }
                    }

                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.error.message,
                                currentMovieId = movieId
                            )
                        }
                    }
                }
            }
        }
    }
}
