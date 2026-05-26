package com.kabindra.tv.iptv.socket

import android.util.Log
import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private const val TAG = "SocketClient"
private const val RECONNECT_INITIAL_DELAY_MS = 1_000L
private const val RECONNECT_MAX_DELAY_MS = 30_000L
private const val RECONNECT_MULTIPLIER = 2.0

class KtorSocketClient {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _notifications = MutableSharedFlow<NotificationMessage>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val notifications: SharedFlow<NotificationMessage> = _notifications.asSharedFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient: HttpClient = HttpClient(CIO) {
        install(WebSockets) {
            pingIntervalMillis = 20_000L
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
        }
        engine {
            requestTimeout = 10_000L
            // Removing endpoint block for now to see if it resolves, 
            // as it was causing compilation errors in this environment
        }
    }

    private var socketJob: Job? = null
    private var session: DefaultClientWebSocketSession? = null

    fun connect(
        host: String,
        port: Int,
        path: String,
        scope: CoroutineScope
    ) {
        socketJob?.cancel()
        socketJob = scope.launch(Dispatchers.IO) {
            var retryDelay = RECONNECT_INITIAL_DELAY_MS
            var attempt = 0

            while (isActive) {
                try {
                    _connectionState.value = if (attempt == 0)
                        ConnectionState.Connecting
                    else
                        ConnectionState.Reconnecting(attempt)

                    Log.i(TAG, "Connecting to ws://$host:$port$path (attempt ${attempt + 1})")

                    httpClient.webSocket(host = host, port = port, path = path) {
                        session = this
                        _connectionState.value = ConnectionState.Connected
                        retryDelay = RECONNECT_INITIAL_DELAY_MS
                        attempt = 0

                        Log.i(TAG, "WebSocket connected!")
                        receiveFrames()
                    }

                } catch (e: CancellationException) {
                    Log.i(TAG, "Socket coroutine cancelled")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "WebSocket error: ${e.message}", e)
                    _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
                }

                if (!isActive) break

                attempt++
                Log.i(TAG, "Reconnecting in ${retryDelay}ms (attempt $attempt)...")
                _connectionState.value = ConnectionState.Reconnecting(attempt)
                delay(retryDelay)
                retryDelay =
                    minOf((retryDelay * RECONNECT_MULTIPLIER).toLong(), RECONNECT_MAX_DELAY_MS)
            }

            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private suspend fun DefaultClientWebSocketSession.receiveFrames() {
        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val raw = frame.readText()
                    Log.d(TAG, "Frame received: $raw")
                    parseAndEmit(raw)
                }

                is Frame.Close -> {
                    Log.i(TAG, "Server sent close frame")
                    break
                }

                else -> {}
            }
        }
    }

    private suspend fun parseAndEmit(raw: String) {
        try {
            val message = json.decodeFromString<NotificationMessage>(raw)
            Log.i(TAG, "Parsed notification: id=${message.id}, priority=${message.priority}")
            _notifications.emit(message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse message: $raw", e)
        }
    }

    suspend fun send(text: String) {
        try {
            session?.send(Frame.Text(text))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send: ${e.message}")
        }
    }

    fun disconnect() {
        socketJob?.cancel()
        socketJob = null
        session = null
        _connectionState.value = ConnectionState.Disconnected
        Log.i(TAG, "Disconnected")
    }

    fun close() {
        disconnect()
        httpClient.close()
    }
}
