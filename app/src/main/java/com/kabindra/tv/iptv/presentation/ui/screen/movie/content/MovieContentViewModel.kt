package com.kabindra.tv.iptv.presentation.ui.screen.movie.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.data.source.UserCredentialsProvider
import com.kabindra.tv.iptv.domain.entity.MediaPlaybackType
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.domain.entity.VODCategory
import com.kabindra.tv.iptv.domain.entity.VODSummary
import com.kabindra.tv.iptv.domain.usecase.room.MovieRoomUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieContentViewModel(
    private val movieRoomUseCase: MovieRoomUseCase,
    private val userCredentialsProvider: UserCredentialsProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(MovieContentState())
    val state: StateFlow<MovieContentState> = _state.asStateFlow()

    private var userCredentials = User()
    private var categories = listOf<MovieCategory>()
    private var movies = listOf<Movie>()

    init {
        observeMovieContent()
    }

    fun observeMovieContent() {
        viewModelScope.launch {
            combine(
                movieRoomUseCase.observeMovieCategories(),
                movieRoomUseCase.observeMovies(),
                userCredentialsProvider.observeCurrentUser(),
            ) { categoryResult, movieResult, user ->
                Triple(categoryResult, movieResult, user)
            }.collect { (categoryResult, movieResult, user) ->
                val categoryError = (categoryResult as? Result.Error)?.error?.message
                val movieError = (movieResult as? Result.Error)?.error?.message
                val errorMessage = categoryError ?: movieError

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

                if (categoryResult !is Result.Success || movieResult !is Result.Success) {
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
                movies = movieResult.data

                val mappedCategories = mapToMovieCategories(categories, movies)
                val selectedCategory = _state.value.selectedCategoryId
                    ?.let { selectedId -> mappedCategories.firstOrNull { it.id == selectedId } }
                    ?: mappedCategories.firstOrNull()

                _state.update {
                    it.copy(
                        isLoading = false,
                        isEmpty = mappedCategories.isEmpty(),
                        errorMessage = "",
                        categories = mappedCategories,
                        selectedCategoryId = selectedCategory?.id,
                    )
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
            val mappedMovies = movieMap[cat.category_id]?.mapNotNull { movie ->
                val streamId = movie.stream_id?.toString() ?: return@mapNotNull null
                val streamUrl = if (!movie.direct_source.isNullOrEmpty()) {
                    movie.direct_source
                } else {
                    buildStreamUrl(
                        serverName = userCredentials.server_name ?: "",
                        username = userCredentials.username ?: "",
                        password = userCredentials.password ?: "",
                        streamId = streamId
                    )
                }
                VODSummary(
                    id = streamId,
                    categoryId = movie.category_id ?: "",
                    title = movie.name ?: "",
                    subtitle = movie.title ?: "",
                    posterUrl = movie.stream_icon ?: "",
                    backdropUrl = movie.stream_icon ?: "",
                    streamUrl = streamUrl,
                    streamType = MediaStreamType.Progressive,
                    playbackType = MediaPlaybackType.Movie
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

    private fun buildStreamUrl(
        serverName: String,
        username: String,
        password: String,
        streamId: String,
    ): String {
        return "http://$serverName/movie/$username/$password/$streamId.ts}"
    }
}
