package com.kabindra.tv.iptv.data.request

data class LoginUserDataRequest(
    val server_name: String,
    val username: String,
    val password: String
)