package com.kabindra.tv.iptv.domain.entity

data class MovieCategory(
    val category_id: String?,
    val category_name: String?,
    val parent_id: Int?,
)

data class Movie(
    val num: Int?,
    val name: String?,
    val title: String?,
    val year: String?,
    val stream_type: String?,
    val stream_id: Int?,
    val stream_icon: String?,
    val rating: Double? = 0.0,
    val rating_5based: Double? = 0.0,
    val added: String?,
    val plot: String? = "",
    val cast: String? = "",
    val director: String? = "",
    val genre: String? = "",
    val release_date: String? = "",
    val youtube_trailer: String? = "",
    val episode_run_time: Int? = 0,
    val category_id: String?,
    val category_ids: List<Int>? = emptyList(),
    val container_extension: String? = "",
    val custom_sid: String? = "",
    val direct_source: String?,
)
