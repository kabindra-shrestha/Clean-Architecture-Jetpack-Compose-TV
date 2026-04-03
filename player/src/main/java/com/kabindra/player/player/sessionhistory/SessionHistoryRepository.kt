package com.kabindra.player.player.sessionhistory

import com.kabindra.player.player.telemetry.model.PlaybackSessionReport

/**
 * Storage boundary for session history.
 * Milestone 0 keeps this as a contract while implementation is added later.
 */
interface SessionHistoryRepository {
    fun save(report: PlaybackSessionReport)
    fun loadRecent(limit: Int = 20): List<PlaybackSessionReport>
    fun clearAll()
}