package com.kabindra.tv.iptv.domain.entity

data class User(
    val id: Int? = 0,
    val server_name: String? = "",
    val username: String? = "",
    val password: String? = "",
)
