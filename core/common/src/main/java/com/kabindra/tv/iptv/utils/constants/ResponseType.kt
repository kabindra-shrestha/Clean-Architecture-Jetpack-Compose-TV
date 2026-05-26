package com.kabindra.tv.iptv.utils.constants

sealed class ResponseType {
    data object None : ResponseType()
    data object LoginCheckUser : ResponseType()
    data object LoginVerifyOTP : ResponseType()
    data object Logout : ResponseType()
    data object Refresh : ResponseType()
}