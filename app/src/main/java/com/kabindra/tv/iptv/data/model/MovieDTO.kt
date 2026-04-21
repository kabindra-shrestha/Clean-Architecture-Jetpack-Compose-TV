package com.kabindra.tv.iptv.data.model

import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import kotlinx.serialization.Serializable

@Serializable
data class MovieCategoryDTO(
    val category_id: String,
    val category_name: String,
    val parent_id: Int,
)

@Serializable
data class MovieDTO(
    val num: Int,
    val name: String,
    val title: String,
    val year: String,
    val stream_type: String,
    val stream_id: Int,
    val stream_icon: String,
    val rating: Double = 0.0,
    val rating_5based: Double = 0.0,
    val added: String,
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    val release_date: String = "",
    val youtube_trailer: String = "",
    val episode_run_time: Int = 0,
    val category_id: String,
    val category_ids: List<Int> = emptyList(),
    val container_extension: String = "",
    val custom_sid: String = "",
    val direct_source: String,
)

fun MovieCategoryDTO.toDomain(): MovieCategory {
    return MovieCategory(
        category_id = category_id,
        category_name = category_name,
        parent_id = parent_id,
    )
}

fun MovieDTO.toDomain(): Movie {
    return Movie(
        num = num,
        name = name,
        title = title,
        year = year,
        stream_type = stream_type,
        stream_id = stream_id,
        stream_icon = stream_icon,
        rating = rating,
        rating_5based = rating_5based,
        added = added,
        plot = plot,
        cast = cast,
        director = director,
        genre = genre,
        release_date = release_date,
        youtube_trailer = youtube_trailer,
        episode_run_time = episode_run_time,
        category_id = category_id,
        category_ids = category_ids,
        container_extension = container_extension,
        custom_sid = custom_sid,
        direct_source = direct_source,
    )
}