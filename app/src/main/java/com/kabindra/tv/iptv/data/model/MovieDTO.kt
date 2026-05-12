package com.kabindra.tv.iptv.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import kotlinx.serialization.Serializable

@Entity(tableName = "movie_categories")
@Serializable
data class MovieCategoryDTO(
    @PrimaryKey
    @ColumnInfo(name = "category_id")
    val category_id: String,
    @ColumnInfo(name = "category_name")
    val category_name: String?,
    @ColumnInfo(name = "parent_id")
    val parent_id: Int?,
)

@Entity(tableName = "movies")
@Serializable
data class MovieDTO(
    @ColumnInfo(name = "num")
    val num: Int?,
    @ColumnInfo(name = "name")
    val name: String?,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "year")
    val year: String?,
    @ColumnInfo(name = "stream_type")
    val stream_type: String?,
    @PrimaryKey
    @ColumnInfo(name = "stream_id")
    val stream_id: Int,
    @ColumnInfo(name = "stream_icon")
    val stream_icon: String?,
    @ColumnInfo(name = "rating")
    val rating: Double? = 0.0,
    @ColumnInfo(name = "rating_5based")
    val rating_5based: Double? = 0.0,
    @ColumnInfo(name = "added")
    val added: String?,
    @ColumnInfo(name = "plot")
    val plot: String? = "",
    @ColumnInfo(name = "cast")
    val cast: String? = "",
    @ColumnInfo(name = "director")
    val director: String? = "",
    @ColumnInfo(name = "genre")
    val genre: String? = "",
    @ColumnInfo(name = "release_date")
    val release_date: String? = "",
    @ColumnInfo(name = "youtube_trailer")
    val youtube_trailer: String? = "",
    @ColumnInfo(name = "episode_run_time")
    val episode_run_time: Int? = 0,
    @ColumnInfo(name = "category_id")
    val category_id: String?,
    @ColumnInfo(name = "category_ids")
    val category_ids: List<Int>? = emptyList(),
    @ColumnInfo(name = "container_extension")
    val container_extension: String? = "",
    @ColumnInfo(name = "custom_sid")
    val custom_sid: String? = "",
    @ColumnInfo(name = "direct_source")
    val direct_source: String?,
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
