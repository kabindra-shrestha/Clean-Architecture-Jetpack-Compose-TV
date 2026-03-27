package com.kabindra.tv.iptv.utils.base

import kotlinx.serialization.Serializable

@Serializable
data class ErrorEvent(
    var isVisible: Boolean = false,
    var isAction: Boolean = false,
    var statusCode: Int = -1,
    var title: String,
    var message: String
)