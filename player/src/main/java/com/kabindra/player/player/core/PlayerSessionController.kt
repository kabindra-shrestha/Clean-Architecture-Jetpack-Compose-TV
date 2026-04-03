package com.kabindra.player.player.core

/**
 * Abstraction for a playback session controller.
 * This lets UI/features evolve independently from the concrete player engine.
 */
interface PlayerSessionController {
    fun prepare(url: String)
    fun play()
    fun pause()
    fun stop()
    fun release()
}