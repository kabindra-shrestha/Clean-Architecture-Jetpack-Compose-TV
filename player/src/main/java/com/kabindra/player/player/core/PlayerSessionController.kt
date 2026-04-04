package com.kabindra.player.player.core

import com.kabindra.player.PlayerRepeatMode

/**
 * Abstraction for a playback session controller.
 * This lets UI/features evolve independently from the concrete player engine.
 */
interface PlayerSessionController {
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun seekTo(positionMs: Long)
    fun fastForward()
    fun rewind()
    fun next()
    fun previous()
    fun playItem(index: Int)
    fun togglePlayPause()
    fun jumpToLiveEdge()
    fun setRepeatMode(repeatMode: PlayerRepeatMode)
    fun setShuffleEnabled(enabled: Boolean)
    fun setPlaybackSpeed(speed: Float)
    fun selectSubtitleTrack(trackId: String)
    fun disableSubtitles()
    fun selectAudioTrack(trackId: String)
    fun selectVideoTrack(trackId: String)
    fun showController()
    fun hideController()
    fun showStatsPanel()
    fun hideStatsPanel()
}
