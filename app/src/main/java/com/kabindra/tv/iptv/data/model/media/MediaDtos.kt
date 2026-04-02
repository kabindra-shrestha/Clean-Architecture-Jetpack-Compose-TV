package com.kabindra.tv.iptv.data.model.media

import com.kabindra.tv.iptv.domain.entity.media.ChannelCategory
import com.kabindra.tv.iptv.domain.entity.media.LiveChannel
import com.kabindra.tv.iptv.domain.entity.media.MediaStreamType
import com.kabindra.tv.iptv.domain.entity.media.MovieCategory
import com.kabindra.tv.iptv.domain.entity.media.MovieDetail
import com.kabindra.tv.iptv.domain.entity.media.MovieSummary

enum class MediaStreamTypeDto {
    Hls,
    Progressive,
}

data class LiveChannelDto(
    val id: String,
    val categoryId: String,
    val title: String,
    val currentProgram: String,
    val streamUrl: String,
    val streamType: MediaStreamTypeDto,
    val logoUrl: String,
)

data class ChannelCategoryDto(
    val id: String,
    val title: String,
    val channels: List<LiveChannelDto>,
)

data class MovieSummaryDto(
    val id: String,
    val categoryId: String,
    val title: String,
    val subtitle: String,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val streamType: MediaStreamTypeDto,
)

data class MovieCategoryDto(
    val id: String,
    val title: String,
    val movies: List<MovieSummaryDto>,
)

data class MovieDetailDto(
    val id: String,
    val categoryId: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val streamType: MediaStreamTypeDto,
    val alsoWatch: List<MovieSummaryDto>,
)

fun ChannelCategoryDto.toDomain(): ChannelCategory {
    return ChannelCategory(
        id = id,
        title = title,
        channels = channels.map(LiveChannelDto::toDomain)
    )
}

fun MovieCategoryDto.toDomain(): MovieCategory {
    return MovieCategory(
        id = id,
        title = title,
        movies = movies.map(MovieSummaryDto::toDomain)
    )
}

fun MovieDetailDto.toDomain(): MovieDetail {
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
        alsoWatch = alsoWatch.map(MovieSummaryDto::toDomain)
    )
}

private fun LiveChannelDto.toDomain(): LiveChannel {
    return LiveChannel(
        id = id,
        categoryId = categoryId,
        title = title,
        currentProgram = currentProgram,
        streamUrl = streamUrl,
        streamType = streamType.toDomain(),
        logoUrl = logoUrl
    )
}

private fun MovieSummaryDto.toDomain(): MovieSummary {
    return MovieSummary(
        id = id,
        categoryId = categoryId,
        title = title,
        subtitle = subtitle,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        streamUrl = streamUrl,
        streamType = streamType.toDomain()
    )
}

private fun MediaStreamTypeDto.toDomain(): MediaStreamType {
    return when (this) {
        MediaStreamTypeDto.Hls -> MediaStreamType.Hls
        MediaStreamTypeDto.Progressive -> MediaStreamType.Progressive
    }
}
