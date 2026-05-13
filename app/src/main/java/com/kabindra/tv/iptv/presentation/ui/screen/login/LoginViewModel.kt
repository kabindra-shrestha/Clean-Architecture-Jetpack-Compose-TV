package com.kabindra.tv.iptv.presentation.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.data.request.LoginUserDataRequest
import com.kabindra.tv.iptv.domain.usecase.room.LiveTVRoomUseCase
import com.kabindra.tv.iptv.domain.usecase.room.LoginRoomUseCase
import com.kabindra.tv.iptv.domain.usecase.room.MovieRoomUseCase
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.MediaCacheStatus
import com.kabindra.tv.iptv.utils.constants.ResponseType
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRoomUseCase: LoginRoomUseCase,
    private val liveTVRoomUseCase: LiveTVRoomUseCase,
    private val movieRoomUseCase: MovieRoomUseCase,
) : ViewModel() {
    private val _loginState = MutableStateFlow(LoginState())

    private var liveTVSyncJob: Job? = null
    private var movieSyncJob: Job? = null

    val loginState = _loginState
        .onStart { }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LoginState()
        )

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.GetLogin -> {
                getLoginUser(event.loginUserDataRequest)
            }

            is LoginEvent.GetIsLogged -> {
            }

            is LoginEvent.GetUser -> {
                getUser()
            }
        }
    }

    fun getLoginUser(loginCheckDataRequest: LoginUserDataRequest) {
        viewModelScope.launch {
            loginRoomUseCase.executeGetLoginUser(loginCheckDataRequest).collect { result ->
                when (result) {
                    is Result.Initial -> Unit

                    is Result.Loading -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = true
                        )
                    }

                    is Result.Success -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            isLogged = result.data.server_name?.isNotEmpty() == true,
                            user = result.data
                        )
                    }

                    is Result.Error -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            isError = true,
                            errorType = ResponseType.None,
                            errorStatusCode = result.error.statusCode,
                            errorTitle = "",
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    fun getUser() {
        viewModelScope.launch {
            loginRoomUseCase.executeGetUser().collect { result ->
                when (result) {
                    is Result.Initial -> Unit

                    is Result.Loading -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = true
                        )
                    }

                    is Result.Success -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            user = result.data
                        )

                        observeLiveTVCache()
                        observeMovieCache()
                        prepareLiveTVCacheIfNeeded()
                        prepareMovieCacheIfNeeded()
                    }

                    is Result.Error -> {
                        _loginState.value = _loginState.value.copy(
                            isLoading = false,
                            isError = false,
                            errorType = ResponseType.None,
                            errorStatusCode = result.error.statusCode,
                            errorTitle = "",
                            errorMessage = result.error.message
                        )
                    }
                }
            }
        }
    }

    private fun observeLiveTVCache() {
        viewModelScope.launch {
            combine(
                liveTVRoomUseCase.observeLiveTVCategories(),
                liveTVRoomUseCase.observeLiveTVChannels(),
            ) { categoryResult, channelResult ->
                categoryResult to channelResult
            }.collect { (categoryResult, channelResult) ->
                val categoryError = (categoryResult as? Result.Error)?.error?.message
                val channelError = (channelResult as? Result.Error)?.error?.message
                val errorMessage = categoryError ?: channelError

                if (errorMessage != null) {
                    _loginState.update {
                        it.copy(
                            liveTVCacheStatus = MediaCacheStatus.Error,
                            liveTVStatusMessage = "Unable to read saved live TV data.",
                            liveTVSyncErrorMessage = errorMessage,
                            isLiveTVSyncing = false,
                        )
                    }
                    return@collect
                }

                val categories = (categoryResult as? Result.Success)?.data ?: return@collect
                val channels = (channelResult as? Result.Success)?.data ?: return@collect
                val hasData = categories.isNotEmpty() && channels.isNotEmpty()

                _loginState.update { current ->
                    val status = when {
                        current.isLiveTVSyncing -> current.liveTVCacheStatus
                        hasData -> MediaCacheStatus.Ready
                        else -> current.liveTVCacheStatus
                    }
                    val message = when {
                        current.isLiveTVSyncing -> current.liveTVStatusMessage
                        hasData -> liveTVReadyMessage(categories.size, channels.size)
                        else -> current.liveTVStatusMessage
                    }

                    current.copy(
                        liveTVCacheStatus = status,
                        liveTVStatusMessage = message,
                        liveTVCategoryCount = categories.size,
                        liveTVChannelCount = channels.size,
                        hasLiveTVData = hasData,
                        liveTVSyncErrorMessage = if (hasData) "" else current.liveTVSyncErrorMessage,
                    )
                }
            }
        }
    }

    private fun prepareLiveTVCacheIfNeeded() {
        liveTVSyncJob = viewModelScope.launch {
            _loginState.update {
                it.copy(
                    liveTVCacheStatus = MediaCacheStatus.Checking,
                    liveTVStatusMessage = "Checking live TV data...",
                    liveTVSyncErrorMessage = "",
                )
            }

            if (liveTVRoomUseCase.hasLiveTVData()) {
                _loginState.update {
                    it.copy(
                        liveTVCacheStatus = MediaCacheStatus.Ready,
                        liveTVStatusMessage = liveTVReadyMessage(
                            categoryCount = it.liveTVCategoryCount,
                            channelCount = it.liveTVChannelCount,
                        ),
                        hasLiveTVData = true,
                    )
                }
                return@launch
            }

            runLiveTVSync(isManual = false)
        }
    }

    private fun observeMovieCache() {
        viewModelScope.launch {
            combine(
                movieRoomUseCase.observeMovieCategories(),
                movieRoomUseCase.observeMovies(),
            ) { categoryResult, movieResult ->
                categoryResult to movieResult
            }.collect { (categoryResult, movieResult) ->
                val categoryError = (categoryResult as? Result.Error)?.error?.message
                val movieError = (movieResult as? Result.Error)?.error?.message
                val errorMessage = categoryError ?: movieError

                if (errorMessage != null) {
                    _loginState.update {
                        it.copy(
                            movieCacheStatus = MediaCacheStatus.Error,
                            movieStatusMessage = "Unable to read saved movie data.",
                            movieSyncErrorMessage = errorMessage,
                            isMovieSyncing = false,
                        )
                    }
                    return@collect
                }

                val categories = (categoryResult as? Result.Success)?.data ?: return@collect
                val movies = (movieResult as? Result.Success)?.data ?: return@collect
                val hasData = categories.isNotEmpty() && movies.isNotEmpty()

                _loginState.update { current ->
                    val status = when {
                        current.isMovieSyncing -> current.movieCacheStatus
                        hasData -> MediaCacheStatus.Ready
                        else -> current.movieCacheStatus
                    }
                    val message = when {
                        current.isMovieSyncing -> current.movieStatusMessage
                        hasData -> movieReadyMessage(categories.size, movies.size)
                        else -> current.movieStatusMessage
                    }

                    current.copy(
                        movieCacheStatus = status,
                        movieStatusMessage = message,
                        movieCategoryCount = categories.size,
                        movieCount = movies.size,
                        hasMovieData = hasData,
                        movieSyncErrorMessage = if (hasData) "" else current.movieSyncErrorMessage,
                    )
                }
            }
        }
    }

    private fun prepareMovieCacheIfNeeded() {
        movieSyncJob = viewModelScope.launch {
            _loginState.update {
                it.copy(
                    movieCacheStatus = MediaCacheStatus.Checking,
                    movieStatusMessage = "Checking movie data...",
                    movieSyncErrorMessage = "",
                )
            }

            if (movieRoomUseCase.hasMovieData()) {
                _loginState.update {
                    it.copy(
                        movieCacheStatus = MediaCacheStatus.Ready,
                        movieStatusMessage = movieReadyMessage(
                            categoryCount = it.movieCategoryCount,
                            movieCount = it.movieCount,
                        ),
                        hasMovieData = true,
                    )
                }
                return@launch
            }

            runMovieSync(isManual = false)
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun syncLiveTVContent() {
        if (liveTVSyncJob?.isActive == true) return

        liveTVSyncJob = viewModelScope.launch {
            runLiveTVSync(isManual = true)
        }
    }

    fun syncMovieContent() {
        if (movieSyncJob?.isActive == true) return

        movieSyncJob = viewModelScope.launch {
            runMovieSync(isManual = true)
        }
    }

    private suspend fun runLiveTVSync(isManual: Boolean) {
        liveTVRoomUseCase.syncLiveTVContent().collect { result ->
            when (result) {
                is Result.Initial -> Unit
                is Result.Loading -> {
                    _loginState.update {
                        it.copy(
                            liveTVCacheStatus = if (isManual) {
                                MediaCacheStatus.Updating
                            } else {
                                MediaCacheStatus.Preparing
                            },
                            liveTVStatusMessage = if (isManual) {
                                "Updating live TV..."
                            } else {
                                "Preparing live TV..."
                            },
                            liveTVSyncErrorMessage = "",
                            isLiveTVSyncing = true,
                        )
                    }
                }

                is Result.Success -> {
                    _loginState.update {
                        it.copy(
                            liveTVCacheStatus = MediaCacheStatus.Ready,
                            liveTVStatusMessage = liveTVReadyMessage(
                                categoryCount = result.data.categoryCount,
                                channelCount = result.data.channelCount,
                            ),
                            liveTVCategoryCount = result.data.categoryCount,
                            liveTVChannelCount = result.data.channelCount,
                            hasLiveTVData = result.data.categoryCount > 0 && result.data.channelCount > 0,
                            liveTVSyncErrorMessage = "",
                            isLiveTVSyncing = false,
                        )
                    }
                }

                is Result.Error -> {
                    val hasCachedData = _loginState.value.hasLiveTVData
                    _loginState.update {
                        it.copy(
                            liveTVCacheStatus = MediaCacheStatus.Error,
                            liveTVStatusMessage = if (hasCachedData) {
                                "Live TV Update failed. Using saved live TV data."
                            } else {
                                "Live TV sync failed."
                            },
                            liveTVSyncErrorMessage = result.error.message,
                            isLiveTVSyncing = false,
                        )
                    }
                }
            }
        }
    }

    private suspend fun runMovieSync(isManual: Boolean) {
        movieRoomUseCase.syncMovieContent().collect { result ->
            when (result) {
                is Result.Initial -> Unit
                is Result.Loading -> {
                    _loginState.update {
                        it.copy(
                            movieCacheStatus = if (isManual) {
                                MediaCacheStatus.Updating
                            } else {
                                MediaCacheStatus.Preparing
                            },
                            movieStatusMessage = if (isManual) {
                                "Updating movies..."
                            } else {
                                "Preparing movies..."
                            },
                            movieSyncErrorMessage = "",
                            isMovieSyncing = true,
                        )
                    }
                }

                is Result.Success -> {
                    _loginState.update {
                        it.copy(
                            movieCacheStatus = MediaCacheStatus.Ready,
                            movieStatusMessage = movieReadyMessage(
                                categoryCount = result.data.categoryCount,
                                movieCount = result.data.movieCount,
                            ),
                            movieCategoryCount = result.data.categoryCount,
                            movieCount = result.data.movieCount,
                            hasMovieData = result.data.categoryCount > 0 && result.data.movieCount > 0,
                            movieSyncErrorMessage = "",
                            isMovieSyncing = false,
                        )
                    }
                }

                is Result.Error -> {
                    val hasCachedData = _loginState.value.hasMovieData
                    _loginState.update {
                        it.copy(
                            movieCacheStatus = MediaCacheStatus.Error,
                            movieStatusMessage = if (hasCachedData) {
                                "Movie update failed. Using saved movie data."
                            } else {
                                "Movie sync failed."
                            },
                            movieSyncErrorMessage = result.error.message,
                            isMovieSyncing = false,
                        )
                    }
                }
            }
        }
    }

    fun resetStates() {
        _loginState.value = LoginState()
    }

    private fun liveTVReadyMessage(categoryCount: Int, channelCount: Int): String {
        return if (categoryCount > 0 && channelCount > 0) {
            "Live TV ready:\n$categoryCount categories, $channelCount channels"
        } else {
            "Live TV data is ready"
        }
    }

    private fun movieReadyMessage(categoryCount: Int, movieCount: Int): String {
        return if (categoryCount > 0 && movieCount > 0) {
            "Movies ready:\n$categoryCount categories, $movieCount movies"
        } else {
            "Movie data is ready"
        }
    }
}