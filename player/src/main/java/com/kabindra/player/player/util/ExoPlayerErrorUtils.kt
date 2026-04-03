package com.kabindra.player.player.util

import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import com.kabindra.player.player.constants.StatusCode.Companion.STATUS_PLAYER_STREAM_FORBIDDEN
import com.kabindra.player.player.util.ErrorType.Companion.ERROR_EXO_PLAYER_COMMON_EXCEPTION
import com.kabindra.player.player.util.ErrorType.Companion.ERROR_EXO_PLAYER_CONNECT_EXCEPTION
import com.kabindra.player.player.util.ErrorType.Companion.ERROR_EXO_PLAYER_FILE_NOT_FOUND_EXCEPTION
import com.kabindra.player.player.util.ErrorType.Companion.ERROR_EXO_PLAYER_SOCKET_TIMEOUT_EXCEPTION
import com.kabindra.player.player.util.ErrorType.Companion.ERROR_EXO_PLAYER_TOO_MANY_SESSION_EXCEPTION
import com.kabindra.player.player.util.ErrorType.Companion.ERROR_EXO_PLAYER_UNKNOWN_HOST_EXCEPTION

@UnstableApi
object ExoPlayerErrorUtils {

    fun errorHandler(e: PlaybackException): String {
        val errorString: String

        when (e.errorCode) {
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                errorString =
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND.toString() + "\n" + ERROR_EXO_PLAYER_FILE_NOT_FOUND_EXCEPTION
            }

            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                errorString =
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED.toString() + "\n" + ERROR_EXO_PLAYER_CONNECT_EXCEPTION
            }

            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                errorString =
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED.toString() + "\n" + ERROR_EXO_PLAYER_SOCKET_TIMEOUT_EXCEPTION
            }

            PlaybackException.ERROR_CODE_UNSPECIFIED -> {
                errorString =
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED.toString() + "\n" + ERROR_EXO_PLAYER_UNKNOWN_HOST_EXCEPTION
            }

            STATUS_PLAYER_STREAM_FORBIDDEN -> {
                errorString =
                    STATUS_PLAYER_STREAM_FORBIDDEN.toString() + "\n" + ERROR_EXO_PLAYER_TOO_MANY_SESSION_EXCEPTION
            }

            else -> {
                errorString =
                    e.errorCode.toString() + "\n" + ERROR_EXO_PLAYER_COMMON_EXCEPTION
            }
        }

        return errorString
    }

    fun isBehindLiveWindowException(e: PlaybackException): Boolean {
        return when (e.errorCode) {
            PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> {
                true
            }

            else -> {
                false
            }
        }
    }

}