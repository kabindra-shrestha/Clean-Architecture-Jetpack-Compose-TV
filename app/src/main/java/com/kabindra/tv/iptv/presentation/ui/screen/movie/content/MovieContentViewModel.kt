package com.kabindra.tv.iptv.presentation.ui.screen.movie.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieBrowseUseCase
import com.kabindra.tv.iptv.domain.usecase.xtream.movie.MovieBrowseXtreamUseCase
import com.kabindra.tv.iptv.domain.usecase.xtream.movie.MovieDetailXtreamUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieContentViewModel(
    private val movieBrowseUseCase: MovieBrowseUseCase,
    private val movieBrowseXtreamUseCase: MovieBrowseXtreamUseCase,
    private val movieDetailXtreamUseCase: MovieDetailXtreamUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieContentState())
    val state: StateFlow<MovieContentState> = _state.asStateFlow()

    init {
        getMovieCategories()
        getMovies()
        getMovieDetail()
    }

    fun getMovies() {
        viewModelScope.launch {
            movieBrowseUseCase.executeGetMovieCategories().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        println("MovieContentViewModel executeGetMovies: Success ${result.data}")
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

    /*fun getMovies() {
        viewModelScope.launch {
            movieBrowseXtreamUseCase.executeGetMovies().collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        println("MovieContentViewModel executeGetMovies: Success ${result.data}")
                        val categories = result.data
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
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
    }*/

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
                        val categories = result.data
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                            )
                        }
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

    fun getMovieDetail() {
        viewModelScope.launch {
            movieDetailXtreamUseCase.executeGetMovieDetail(123456).collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true, errorMessage = "") }
                    }

                    is Result.Success -> {
                        println("MovieContentViewModel executeGetMovieDetail: Success ${result.data}")
                        val categories = result.data
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                            )
                        }
                    }

                    is Result.Error -> {
                        println("MovieContentViewModel executeGetMovieDetail: Error ${result.error.message}")
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
