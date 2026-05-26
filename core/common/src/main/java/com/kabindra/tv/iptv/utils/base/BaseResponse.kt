package com.kabindra.tv.iptv.utils.base

import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse(
    @Serializable(with = FlexibleIntStringSerializer::class) var status: String = "",
    var statusCode: Int = -1,
    var message: String = ""
)