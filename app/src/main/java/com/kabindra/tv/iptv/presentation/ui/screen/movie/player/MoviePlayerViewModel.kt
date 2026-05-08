package com.kabindra.tv.iptv.presentation.ui.screen.movie.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.source.UserCredentialsProvider
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.domain.entity.toVODDetail
import com.kabindra.tv.iptv.domain.usecase.remote.movie.MovieDetailUseCase
import com.kabindra.tv.iptv.domain.usecase.xtream.movie.MovieDetailXtreamUseCase
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MoviePlayerViewModel(
    private val movieDetailUseCase: MovieDetailUseCase,
    private val movieDetailXtreamUseCase: MovieDetailXtreamUseCase,
    private val userCredentialsProvider: UserCredentialsProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(MoviePlayerState())
    val state: StateFlow<MoviePlayerState> = _state.asStateFlow()

    private var userCredentials = User()

    init {
        getUserCredentials()
    }

    fun getUserCredentials() {
        viewModelScope.launch {
            userCredentials = userCredentialsProvider.getCurrentUser()
                ?: throw IllegalStateException("User not logged in")
        }
    }

    fun getMovieDetail(movieId: String) {
        if (_state.value.currentMovieId == movieId && _state.value.movie != null) return

        viewModelScope.launch {
            movieDetailXtreamUseCase.executeGetMovieDetail(movieId.toLong()).collect { result ->
                when (result) {
                    is Result.Initial -> Unit
                    is Result.Loading -> {
                        _state.update {
                            it.copy(
                                isLoading = true,
                                errorMessage = "",
                                currentMovieId = movieId
                            )
                        }
                    }

                    is Result.Success -> {
                        println("MovieDetailViewModel executeGetMovieDetail: Success ${result.data}")
                        val movieDetailDTO = result.data
                        val movieDetail = movieDetailDTO.toDomain()
                        val streamUrl = if (!movieDetail.movieData?.directSource.isNullOrEmpty()) {
                            movieDetail.movieData.directSource
                        } else {
                            buildStreamUrl(
                                serverName = userCredentials.server_name ?: "",
                                username = userCredentials.username ?: "",
                                password = userCredentials.password ?: "",
                                streamId = movieDetail.movieData?.streamId.toString()
                            )
                        }
                        val vodDetail = movieDetail.toVODDetail().copy(streamUrl = streamUrl)
                        println("MovieDetailViewModel convertedToVODDetail: $vodDetail")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "",
                                movie = vodDetail,
                                currentMovieId = movieId
                            )
                        }
                    }

                    is Result.Error -> {
                        println("MovieDetailViewModel executeGetMovieDetail: Error ${result.error.message}")
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

    private fun buildStreamUrl(
        serverName: String,
        username: String,
        password: String,
        streamId: String,
    ): String {
        return "http://$serverName/movie/$username/$password/$streamId.ts"
    }
}
