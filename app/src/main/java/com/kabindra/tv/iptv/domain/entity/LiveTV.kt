package com.kabindra.tv.iptv.domain.entity

data class LiveTVCategory(
    val category_id: String?,
    val category_name: String?,
    val parent_id: Int?,
)

data class LiveTV(
    val num: Int?,
    val name: String?,
    val stream_type: String?,
    val stream_id: Int?,
    val stream_icon: String?,
    val epg_channel_id: String?,
    val added: String?,
    val custom_sid: String?,
    val tv_archive: Int?,
    val direct_source: String?,
    val tv_archive_duration: Int?,
    val category_id: String?,
    val category_ids: List<Int>?,
    val thumbnail: String?,
)

data class LiveTVSyncSummary(
    val categoryCount: Int,
    val channelCount: Int,
)
