package com.kabindra.tv.iptv.utils.ktor

import com.kabindra.tv.iptv.utils.base.ErrorResponse

sealed class Result<out T> {
    data object Initial : Result<Nothing>()
    data object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: ErrorResponse) : Result<Nothing>()
}
