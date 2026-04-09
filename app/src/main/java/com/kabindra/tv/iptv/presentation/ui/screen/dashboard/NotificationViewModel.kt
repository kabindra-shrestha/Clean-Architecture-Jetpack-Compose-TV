package com.kabindra.tv.iptv.presentation.ui.screen.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kabindra.tv.iptv.MainActivity
import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import com.kabindra.tv.iptv.service.SocketForegroundService
import com.kabindra.tv.iptv.socket.KtorSocketClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "NotificationViewModel"
private const val POLL_INTERVAL_MS = 500L   // Poll for service availability

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

data class NotificationUiState(
    val connectionState: ConnectionState = ConnectionState.Connecting,
    val notifications: List<NotificationMessage> = emptyList(),
    val activeAlert: NotificationMessage? = null,         // Foreground in-app alert
    val showAlertDialog: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// NotificationViewModel
//
// Collects flows from the Foreground Service's shared KtorSocketClient and
// exposes a single UI state to Compose screens.
// ─────────────────────────────────────────────────────────────────────────────

class NotificationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    private val handledPayloadIds = linkedSetOf<String>()

    init {
        observeService()
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

    // ── Actions ───────────────────────────────────────────────────────────────

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
}
