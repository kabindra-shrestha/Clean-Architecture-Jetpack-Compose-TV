package com.kabindra.tv.iptv.presentation.ui.screen.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.MainActivity
import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import com.kabindra.tv.iptv.domain.usecase.room.LiveTVRoomUseCase
import com.kabindra.tv.iptv.domain.usecase.room.MovieRoomUseCase
import com.kabindra.tv.iptv.service.SocketForegroundService
import com.kabindra.tv.iptv.socket.KtorSocketClient
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "DashboardViewModel"
private const val POLL_INTERVAL_MS = 500L   // Poll for service availability

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

enum class MediaCacheStatus {
    Checking,
    Preparing,
    Ready,
    Updating,
    Error,
}

data class DashboardUiState(
    val connectionState: ConnectionState = ConnectionState.Connecting,
    val notifications: List<NotificationMessage> = emptyList(),
    val activeAlert: NotificationMessage? = null,         // Foreground in-app alert
    val showAlertDialog: Boolean = false,
    val liveTVCacheStatus: MediaCacheStatus = MediaCacheStatus.Checking,
    val liveTVStatusMessage: String = "Checking live TV data...",
    val liveTVSyncErrorMessage: String = "",
    val liveTVCategoryCount: Int = 0,
    val liveTVChannelCount: Int = 0,
    val hasLiveTVData: Boolean = false,
    val isLiveTVSyncing: Boolean = false,
    val movieCacheStatus: MediaCacheStatus = MediaCacheStatus.Checking,
    val movieStatusMessage: String = "Checking movie data...",
    val movieSyncErrorMessage: String = "",
    val movieCategoryCount: Int = 0,
    val movieCount: Int = 0,
    val hasMovieData: Boolean = false,
    val isMovieSyncing: Boolean = false,
)

sealed class DashboardEvent {

    data object observeService : DashboardEvent()

    data object observeLiveTVCache : DashboardEvent()

    data object observeMovieCache : DashboardEvent()

    data object prepareLiveTVCacheIfNeeded : DashboardEvent()

    data object prepareMovieCacheIfNeeded : DashboardEvent()

}

// ─────────────────────────────────────────────────────────────────────────────
// DashboardViewModel
//
// Collects flows from the Foreground Service's shared KtorSocketClient and
// exposes a single UI state to Compose screens.
// ─────────────────────────────────────────────────────────────────────────────

class DashboardViewModel(
    private val liveTVRoomUseCase: LiveTVRoomUseCase,
    private val movieRoomUseCase: MovieRoomUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val handledPayloadIds = linkedSetOf<String>()
    private var liveTVSyncJob: Job? = null
    private var movieSyncJob: Job? = null

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.observeService -> {
                observeService()
            }

            is DashboardEvent.observeLiveTVCache -> {
                observeLiveTVCache()
            }

            is DashboardEvent.observeMovieCache -> {
                observeMovieCache()
            }

            is DashboardEvent.prepareLiveTVCacheIfNeeded -> {
                prepareLiveTVCacheIfNeeded()
            }

            is DashboardEvent.prepareMovieCacheIfNeeded -> {
                prepareMovieCacheIfNeeded()
            }
        }
    }

    /**
     * Poll until the service is alive, then subscribe to its flows.
     * The ViewModel re-subscribes if the service restarts.
     */
    private fun observeService() {
        viewModelScope.launch {
            while (true) {
                val client = SocketForegroundService.socketClient
                if (client != null) {
                    Log.i(TAG, "Service available — subscribing to flows")
                    subscribeToClient(client)
                    break
                }
                Log.d(TAG, "Waiting for service…")
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun subscribeToClient(
        client: KtorSocketClient
    ) {
        // ── Connection State ──
        viewModelScope.launch {
            client.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }

        // ── Incoming Notifications ──
        viewModelScope.launch {
            client.notifications.collect { message ->
                Log.i(TAG, "UI received: ${message.title}")
                _uiState.update { current ->
                    current.copy(
                        notifications = (listOf(message) + current.notifications).take(50),
                        activeAlert = message,
                        showAlertDialog = true
                    )
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
                    _uiState.update {
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

                _uiState.update { current ->
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
            _uiState.update {
                it.copy(
                    liveTVCacheStatus = MediaCacheStatus.Checking,
                    liveTVStatusMessage = "Checking live TV data...",
                    liveTVSyncErrorMessage = "",
                )
            }

            if (liveTVRoomUseCase.hasLiveTVData()) {
                _uiState.update {
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
                    _uiState.update {
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

                _uiState.update { current ->
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
            _uiState.update {
                it.copy(
                    movieCacheStatus = MediaCacheStatus.Checking,
                    movieStatusMessage = "Checking movie data...",
                    movieSyncErrorMessage = "",
                )
            }

            if (movieRoomUseCase.hasMovieData()) {
                _uiState.update {
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
                    _uiState.update {
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
                    _uiState.update {
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
                    val hasCachedData = _uiState.value.hasLiveTVData
                    _uiState.update {
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
                    _uiState.update {
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
                    _uiState.update {
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
                    val hasCachedData = _uiState.value.hasMovieData
                    _uiState.update {
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

    fun updatePayloadAlert(payload: MainActivity.AlertPayload?) {
        if (payload == null) {
            Log.w(TAG, "Received null payload, ignoring")
            return
        }
        if (payload.notifId.isBlank()) {
            Log.w(TAG, "Received blank payload id, ignoring")
            return
        }
        if (!handledPayloadIds.add(payload.notifId)) {
            Log.i(TAG, "Payload ${payload.notifId} already handled, ignoring duplicate")
            return
        }

        payload.let {
            val message = NotificationMessage(
                id = payload.notifId,
                title = payload.title,
                message = payload.message,
                priority = payload.priority.uppercase()
            )

            Log.i(TAG, "UI received: payload ${payload.title}")
            _uiState.update { current ->
                current.copy(
                    notifications = (listOf(message) + current.notifications).take(50),
                    activeAlert = message,
                    showAlertDialog = true
                )
            }
        }
    }

    fun dismissActiveAlert() {
        _uiState.update {
            it.copy(
                showAlertDialog = false,
                activeAlert = null,
            )
        }
    }

    fun clearNotifications() {
        _uiState.update {
            it.copy(
                notifications = emptyList(),
                showAlertDialog = false,
                activeAlert = null
            )
        }
    }

    private fun liveTVReadyMessage(categoryCount: Int, channelCount: Int): String {
        return if (categoryCount > 0 && channelCount > 0) {
            "Live TV ready: $categoryCount categories, $channelCount channels"
        } else {
            "Live TV data is ready"
        }
    }

    private fun movieReadyMessage(categoryCount: Int, movieCount: Int): String {
        return if (categoryCount > 0 && movieCount > 0) {
            "Movies ready: $categoryCount categories, $movieCount movies"
        } else {
            "Movie data is ready"
        }
    }
}
