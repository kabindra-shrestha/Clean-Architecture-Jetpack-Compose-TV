package com.kabindra.tv.iptv.data.model

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Notification Priority
// ─────────────────────────────────────────────────────────────────────────────

enum class NotificationPriority {
    LOW, NORMAL, HIGH, CRITICAL;

    val label: String get() = name
}

// ─────────────────────────────────────────────────────────────────────────────
// Socket Notification Message (JSON payload from server)
//
// Example server payload:
// {
//   "id": "abc123",
//   "title": "Server Alert",
//   "message": "Database CPU at 98%",
//   "priority": "CRITICAL",
//   "timestamp": 1700000000000
// }
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class NotificationMessage(
    val id: String,
    val title: String,
    val message: String,
    val priority: String = "NORMAL",   // String from JSON, mapped to enum below
    val timestamp: Long = System.currentTimeMillis()
) {
    val priorityEnum: NotificationPriority
        get() = runCatching { NotificationPriority.valueOf(priority.uppercase()) }
            .getOrDefault(NotificationPriority.NORMAL)
}

// ─────────────────────────────────────────────────────────────────────────────
// Connection State
// ─────────────────────────────────────────────────────────────────────────────

sealed class ConnectionState {
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
    data object Disconnected : ConnectionState()
}
