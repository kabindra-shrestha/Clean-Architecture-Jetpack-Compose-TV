package com.kabindra.tv.iptv.utils.base

import kotlinx.serialization.Serializable

@Serializable
data class RouteEvent(
    var route: String
)