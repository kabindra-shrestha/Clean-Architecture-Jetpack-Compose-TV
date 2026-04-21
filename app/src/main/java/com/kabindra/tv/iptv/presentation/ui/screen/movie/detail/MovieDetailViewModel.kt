package com.kabindra.tv.iptv.presentation.ui.screen.movie.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.domain.entity.VODCategory
import com.kabindra.tv.iptv.domain.entity.VODSummary
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieBrowseUseCase
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieDetailUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class MovieDetailViewModel(
    private val movieBrowseUseCase: MovieBrowseUseCase,
    private val movieDetailUseCase: MovieDetailUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieDetailState())
    val state: StateFlow<MovieDetailState> = _state.asStateFlow()

    fun loadMovie(movieId: String) {
        if (_state.value.currentMovieId == movieId && _state.value.movie != null) return

        viewModelScope.launch {
            movieBrowseUseCase.executeGetMovieCategories().collect { result ->
                when (result) {
                    is Result.Initial,
                    is Result.Loading -> Unit

                    is Result.Success -> {
                        _state.update { currentState ->
                            currentState.copy(
                                recommendedMovies = buildRecommendedMovies(
                                    movieId = movieId,
                                    categories = result.data,
                                    fallback = currentState.movie?.alsoWatch.orEmpty()
                                )
                            )
                        }
                    }

                    is Result.Error -> {
                        _state.update { currentState ->
                            currentState.copy(
                                recommendedMovies = currentState.movie?.alsoWatch.orEmpty()
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            movieDetailUseCase.executeGetMovieDetail(movieId).collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                errorMessage = "",
                                recommendedMovies = emptyList(),
                                currentMovieId = movieId
                            )
                        }
                    }

                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                                movie = result.data,
                                recommendedMovies = it.recommendedMovies.ifEmpty { result.data.alsoWatch },
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

    private fun buildRecommendedMovies(
        movieId: String,
        categories: List<VODCategory>,
        fallback: List<VODSummary>,
    ): List<VODSummary> {
        val seed = movieId.hashCode().toLong()
        return categories
            .flatMap(VODCategory::movies)
            .filterNot { it.id == movieId }
            .ifEmpty { fallback }
            .shuffled(Random(seed))
    }
}
