package com.kabindra.tv.iptv.data.request

data class LoginCheckUserDataRequest(
    val username: String,
    val appLoginCode: String,
    val fcmToken: String
)