package com.kabindra.player.player.util

class ErrorType {

    companion object {
        const val ERROR_EXO_PLAYER_COMMON_EXCEPTION =
            "Code-5001: Please refresh and try again.\nIf the problem persists contact support."
        const val ERROR_EXO_PLAYER_CONNECT_EXCEPTION =
            "Code-5223: The network connection is lost.\nPlease check your network connection and try again."
        const val ERROR_EXO_PLAYER_UNKNOWN_HOST_EXCEPTION =
            "Code-5232: The network connection is unavailable.\nPlease check your network connection and try again."
        const val ERROR_EXO_PLAYER_SOCKET_TIMEOUT_EXCEPTION =
            "Code-5242: It's taking time to load stream.\nPlease check your network connection and try again."
        const val ERROR_EXO_PLAYER_FILE_NOT_FOUND_EXCEPTION =
            "Code-2040: We couldn't find the stream you've requested.\nPlease contact support or try again later."
        const val ERROR_EXO_PLAYER_TOO_MANY_SESSION_EXCEPTION =
            "Code-4030: Too many users detected.\nPlease contact support or try again later."
    }

}