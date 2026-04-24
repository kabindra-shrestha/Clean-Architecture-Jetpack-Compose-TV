package com.kabindra.tv.iptv.presentation.ui.screen.movie.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.domain.entity.MediaPlaybackType
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.entity.VODCategory
import com.kabindra.tv.iptv.domain.entity.VODSummary
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieBrowseUseCase
import com.kabindra.tv.iptv.domain.usecase.xtream.movie.MovieBrowseXtreamUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieContentViewModel(
    private val movieBrowseUseCase: MovieBrowseUseCase,
    private val movieBrowseXtreamUseCase: MovieBrowseXtreamUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieContentState())
    val state: StateFlow<MovieContentState> = _state.asStateFlow()

    private var categories = listOf<MovieCategory>()
    private var movies = listOf<Movie>()

    fun getMovieCategories() {
        viewModelScope.launch {
            movieBrowseXtreamUseCase.executeGetMovieCategories().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        println("MovieContentViewModel executeGetMovieCategories: Success ${result.data}")
                        categories = result.data

                        getMovies()
                    }

                    is Result.Error -> {
                        println("MovieContentViewModel executeGetMovieCategories: Error ${result.error.message}")
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

    fun getMovies() {
        viewModelScope.launch {
            movieBrowseXtreamUseCase.executeGetMovies().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        println("MovieContentViewModel executeGetMovies: Success ${result.data}")
                        movies = result.data

                        val mappedCategories = mapToMovieCategories(categories, movies)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                                categories = mappedCategories,
                                selectedCategoryId = it.selectedCategoryId
                                    ?: categories.firstOrNull()?.category_id
                            )
                        }
                    }

                    is Result.Error -> {
                        println("MovieContentViewModel executeGetMovies: Error ${result.error.message}")
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

    private fun mapToMovieCategories(
        categories: List<MovieCategory>,
        movies: List<Movie>
    ): List<VODCategory> {
        val movieMap = movies.groupBy { it.category_id }
        return categories.mapNotNull { cat ->
            val mappedMovies = movieMap[cat.category_id]?.map { movie ->
                VODSummary(
                    id = movie.stream_id.toString(),
                    categoryId = movie.category_id ?: "",
                    title = movie.name ?: "",
                    subtitle = movie.title ?: "",
                    posterUrl = movie.stream_icon ?: "",
                    backdropUrl = movie.stream_icon ?: "",
                    streamUrl = movie.direct_source ?: "",
                    streamType = MediaStreamType.Progressive,
                    playbackType = MediaPlaybackType.Live
                )
            } ?: emptyList()

            // Only include categories that have movies
            if (mappedMovies.isNotEmpty()) {
                VODCategory(
                    id = cat.category_id ?: "",
                    title = cat.category_name ?: "",
                    movies = mappedMovies
                )
            } else {
                null
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun reset() {
        categories = listOf()
        movies = listOf()

        _state.value = MovieContentState()
    }
}
