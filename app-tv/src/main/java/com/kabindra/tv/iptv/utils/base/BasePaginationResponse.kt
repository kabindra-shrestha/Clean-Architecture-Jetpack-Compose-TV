package com.kabindra.tv.iptv.utils.base

import kotlinx.serialization.Serializable

@Serializable
open class BasePaginationResponse(
    var current_page: Int = -1,
    var per_page: Int = -1,
    var total: Int = -1,
    var last_page: Int = -1
)