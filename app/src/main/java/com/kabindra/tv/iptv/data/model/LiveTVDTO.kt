package com.kabindra.tv.iptv.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import kotlinx.serialization.Serializable

@Entity(tableName = "live_tv_categories")
@Serializable
data class LiveTVCategoryDTO(
    @PrimaryKey
    @ColumnInfo(name = "category_id")
    val category_id: String,
    @ColumnInfo(name = "category_name")
    val category_name: String?,
    @ColumnInfo(name = "parent_id")
    val parent_id: Int?,
)

@Entity(tableName = "live_tv_channels")
@Serializable
data class LiveTVDTO(
    @ColumnInfo(name = "num")
    val num: Int?,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "stream_type")
    val stream_type: String?,
    @PrimaryKey
    @ColumnInfo(name = "stream_id")
    val stream_id: Int,
    @ColumnInfo(name = "stream_icon")
    val stream_icon: String?,
    @ColumnInfo(name = "epg_channel_id")
    val epg_channel_id: String?,
    @ColumnInfo(name = "added")
    val added: String?,
    @ColumnInfo(name = "custom_sid")
    val custom_sid: String?,
    @ColumnInfo(name = "tv_archive")
    val tv_archive: Int?,
    @ColumnInfo(name = "direct_source")
    val direct_source: String?,
    @ColumnInfo(name = "tv_archive_duration")
    val tv_archive_duration: Int?,
    @ColumnInfo(name = "category_id")
    val category_id: String?,
    @ColumnInfo(name = "category_ids")
    val category_ids: List<Int>?,
    @ColumnInfo(name = "thumbnail")
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
