package com.kabindra.tv.iptv.data.model

import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import kotlinx.serialization.Serializable

@Serializable
data class LiveTVCategoryDTO(
    val category_id: String?,
    val category_name: String?,
    val parent_id: Int?,
)

@Serializable
data class LiveTVDTO(
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

fun LiveTVCategoryDTO.toDomain(): LiveTVCategory {
    return LiveTVCategory(
        category_id = category_id,
        category_name = category_name,
        parent_id = parent_id,
    )
}

fun LiveTVDTO.toDomain(): LiveTV {
    return LiveTV(
        num = num,
        name = name,
        stream_type = stream_type,
        stream_id = stream_id,
        stream_icon = stream_icon,
        epg_channel_id = epg_channel_id,
        added = added,
        custom_sid = custom_sid,
        tv_archive = tv_archive,
        direct_source = direct_source,
        tv_archive_duration = tv_archive_duration,
        category_id = category_id,
        category_ids = category_ids,
        thumbnail = thumbnail,
    )
}