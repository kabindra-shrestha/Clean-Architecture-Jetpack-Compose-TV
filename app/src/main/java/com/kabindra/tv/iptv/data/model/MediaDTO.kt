package com.kabindra.tv.iptv.data.model

import com.kabindra.tv.iptv.domain.entity.ChannelCategory
import com.kabindra.tv.iptv.domain.entity.LiveChannel
import com.kabindra.tv.iptv.domain.entity.MediaPlaybackType
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.entity.MovieDetail
import com.kabindra.tv.iptv.domain.entity.MovieSummary

enum class MediaStreamTypeDTO {
    Hls,
    Progressive,
}

enum class MediaPlaybackTypeDTO {
    Live,
    Dvr,
    Movie,
}

data class LiveChannelDTO(
    val id: String,
    val categoryId: String,
    val title: String,
    val currentProgram: String,
    val streamUrl: String,
    val streamType: MediaStreamTypeDTO,
    val playbackType: MediaPlaybackTypeDTO,
    val logoUrl: String,
)

data class ChannelCategoryDTO(
    val id: String,
    val title: String,
    val channels: List<LiveChannelDTO>,
)

data class MovieSummaryDTO(
    val id: String,
    val categoryId: String,
    val title: String,
    val subtitle: String,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val streamType: MediaStreamTypeDTO,
    val playbackType: MediaPlaybackTypeDTO,
)

data class MovieCategoryDTO(
    val id: String,
    val title: String,
    val movies: List<MovieSummaryDTO>,
)

data class MovieDetailDTO(
    val id: String,
    val categoryId: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val streamType: MediaStreamTypeDTO,
    val playbackType: MediaPlaybackTypeDTO,
    val alsoWatch: List<MovieSummaryDTO>,
)

fun ChannelCategoryDTO.toDomain(): ChannelCategory {
    return ChannelCategory(
        id = id,
        title = title,
        channels = channels.map(LiveChannelDTO::toDomain)
    )
}

fun MovieCategoryDTO.toDomain(): MovieCategory {
    return MovieCategory(
        id = id,
        title = title,
        movies = movies.map(MovieSummaryDTO::toDomain)
    )
}

fun MovieDetailDTO.toDomain(): MovieDetail {
    return MovieDetail(
        id = id,
        categoryId = categoryId,
        title = title,
        subtitle = subtitle,
        description = description,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        streamUrl = streamUrl,
        streamType = streamType.toDomain(),
        playbackType = playbackType.toDomain(),
        alsoWatch = alsoWatch.map(MovieSummaryDTO::toDomain)
    )
}

private fun LiveChannelDTO.toDomain(): LiveChannel {
    return LiveChannel(
        id = id,
        categoryId = categoryId,
        title = title,
        currentProgram = currentProgram,
        streamUrl = streamUrl,
        streamType = streamType.toDomain(),
        playbackType = playbackType.toDomain(),
        logoUrl = logoUrl
    )
}

private fun MovieSummaryDTO.toDomain(): MovieSummary {
    return MovieSummary(
        id = id,
        categoryId = categoryId,
        title = title,
        subtitle = subtitle,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        streamUrl = streamUrl,
        streamType = streamType.toDomain(),
        playbackType = playbackType.toDomain()
    )
}

private fun MediaStreamTypeDTO.toDomain(): MediaStreamType {
    return when (this) {
        MediaStreamTypeDTO.Hls -> MediaStreamType.Hls
        MediaStreamTypeDTO.Progressive -> MediaStreamType.Progressive
    }
}

private fun MediaPlaybackTypeDTO.toDomain(): MediaPlaybackType {
    return when (this) {
        MediaPlaybackTypeDTO.Live -> MediaPlaybackType.Live
        MediaPlaybackTypeDTO.Dvr -> MediaPlaybackType.Dvr
        MediaPlaybackTypeDTO.Movie -> MediaPlaybackType.Movie
    }
}
