package com.kabindra.tv.iptv.domain.entity

import com.kabindra.tv.iptv.utils.base.BaseResponse
import kotlinx.serialization.Serializable

@Serializable
data class RefreshToken(
    val response: RefreshTokenInfo?
) : BaseResponse()

@Serializable
data class RefreshTokenInfo(
    val token: String? = "",
    var refresh_token: String? = ""
)