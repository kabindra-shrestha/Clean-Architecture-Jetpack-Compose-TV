package com.kabindra.tv.iptv.utils.ktor

import com.kabindra.tv.iptv.utils.base.ErrorResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object ResultError {
    private const val IS_DEBUG = false
    private const val ERROR_TITLE_NO_INTERNET_CONNECTIVITY = "No Internet Connectivity"
    private const val ERROR_HTTP_EXCEPTION =
        "Code-5222: No Internet Connection.\nPlease check your network connection and try again."
    private const val ERROR_CONNECT_EXCEPTION =
        "Code-5223: The network connection is lost.\nPlease check your network connection and try again."
    private const val ERROR_UNKNOWN_HOST_EXCEPTION =
        "Code-5230: The network connection is unavailable.\nPlease check your network connection."
    private const val ERROR_SOCKET_TIMEOUT_EXCEPTION =
        "Code-5241: It's taking time to load content.\nPlease check your network connection and try again."
    private const val ERROR_INTERNAL_SERVER_EXCEPTION =
        "Code-5000: Something went wrong.\nPlease check your network connection and try again."
    private const val ERROR_JSON_SYNTAX_EXCEPTION =
        "Code-4001: Contents couldn't load due to unexpected response.\nPlease try refreshing it again."
    private const val ERROR_NUMBER_FORMAT_EXCEPTION =
        "Code-4003: Number format mismatched.\nPlease try refreshing it again."

    private const val STATUS_HTTP_EXCEPTION = 5222
    private const val STATUS_CONNECT_EXCEPTION = 5223
    private const val STATUS_UNKNOWN_HOST_EXCEPTION = 5230
    private const val STATUS_SOCKET_TIMEOUT_EXCEPTION = 5241
    private const val STATUS_INTERNAL_SERVER_EXCEPTION = 5000
    private const val STATUS_JSON_SYNTAX_EXCEPTION = 4001
    private const val STATUS_NUMBER_FORMAT_EXCEPTION = 4003

    suspend fun parseError(response: HttpResponse): ErrorResponse {
        println("Error: Error:")
        val json = Json {
            ignoreUnknownKeys = true
        }
        val errorResponse: ErrorResponse

        return try {
            errorResponse = json.decodeFromString<ErrorResponse>(response.body())

            println("Error: ${errorResponse.statusCode}: ${errorResponse.message}")
            errorResponse
        } catch (e: Exception) {
            parseException(e)
        }
    }

    fun parseException(exception: Exception): ErrorResponse {
        println("Error: Exception: ${exception.message.toString()}")
        return handleException(exception)
    }

    private fun handleException(e: Throwable): ErrorResponse {
        val errorResponse = ErrorResponse()

        return when (e) {
            // Ktor HTTP exceptions
            is ClientRequestException -> { // 4xx errors
                errorResponse.status = STATUS_HTTP_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_HTTP_EXCEPTION
                errorResponse.message = if (IS_DEBUG) e.message else ERROR_HTTP_EXCEPTION
                errorResponse.title = ERROR_TITLE_NO_INTERNET_CONNECTIVITY
                errorResponse
            }

            is ServerResponseException -> { // 5xx errors
                errorResponse.status = STATUS_INTERNAL_SERVER_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_INTERNAL_SERVER_EXCEPTION
                errorResponse.message =
                    if (IS_DEBUG) e.message else ERROR_INTERNAL_SERVER_EXCEPTION
                errorResponse.title = ""
                errorResponse
            }

            is ResponseException -> { // Generic HTTP response errors
                errorResponse.status = STATUS_HTTP_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_HTTP_EXCEPTION
                errorResponse.message = if (IS_DEBUG) e.message!! else ERROR_HTTP_EXCEPTION
                errorResponse.title = ERROR_TITLE_NO_INTERNET_CONNECTIVITY
                errorResponse
            }

            // Connection errors
            is TimeoutCancellationException -> { // Request timeout
                errorResponse.status = STATUS_SOCKET_TIMEOUT_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_SOCKET_TIMEOUT_EXCEPTION
                errorResponse.message =
                    if (IS_DEBUG) e.message!! else ERROR_SOCKET_TIMEOUT_EXCEPTION
                errorResponse.title = ERROR_TITLE_NO_INTERNET_CONNECTIVITY
                errorResponse
            }

            is UnresolvedAddressException -> { // No internet or DNS issues
                errorResponse.status = STATUS_UNKNOWN_HOST_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_UNKNOWN_HOST_EXCEPTION
                errorResponse.message = if (IS_DEBUG) e.message!! else ERROR_UNKNOWN_HOST_EXCEPTION
                errorResponse.title = ERROR_TITLE_NO_INTERNET_CONNECTIVITY
                errorResponse
            }

            is kotlinx.io.IOException -> { // Network failure
                errorResponse.status = STATUS_CONNECT_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_CONNECT_EXCEPTION
                errorResponse.message = if (IS_DEBUG) e.message!! else ERROR_CONNECT_EXCEPTION
                errorResponse.title = ERROR_TITLE_NO_INTERNET_CONNECTIVITY
                errorResponse
            }

            // JSON parsing errors
            is SerializationException -> {
                errorResponse.status = STATUS_JSON_SYNTAX_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_JSON_SYNTAX_EXCEPTION
                errorResponse.message = if (IS_DEBUG) e.message!! else ERROR_JSON_SYNTAX_EXCEPTION
                errorResponse.title = ""
                errorResponse
            }

            // Number format issues
            is NumberFormatException -> {
                errorResponse.status = STATUS_NUMBER_FORMAT_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_NUMBER_FORMAT_EXCEPTION
                errorResponse.message = if (IS_DEBUG) e.message!! else ERROR_NUMBER_FORMAT_EXCEPTION
                errorResponse.title = ""
                errorResponse
            }

            // Cancellation
            is CancellationException -> {
                errorResponse.status = STATUS_INTERNAL_SERVER_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_INTERNAL_SERVER_EXCEPTION
                errorResponse.message =
                    if (IS_DEBUG) e.message!! else ERROR_INTERNAL_SERVER_EXCEPTION
                errorResponse.title = ""
                errorResponse
            }

            // Generic error
            else -> {
                errorResponse.status = STATUS_INTERNAL_SERVER_EXCEPTION.toString()
                errorResponse.statusCode = STATUS_INTERNAL_SERVER_EXCEPTION
                errorResponse.message =
                    if (IS_DEBUG) e.message!! else ERROR_INTERNAL_SERVER_EXCEPTION
                errorResponse.title = ""
                errorResponse
            }
        }
    }

}
