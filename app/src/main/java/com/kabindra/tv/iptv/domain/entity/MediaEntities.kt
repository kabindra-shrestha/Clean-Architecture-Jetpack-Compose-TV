package com.kabindra.tv.iptv.domain.entity

enum class MediaStreamType {
    Hls,
    Progressive,
}

enum class MediaPlaybackType {
    Live,
    Dvr,
    Movie,
}

data class LiveChannel(
    val id: String,
    val categoryId: String,
    val title: String,
    val currentProgram: String,
    val streamUrl: String,
    val streamType: MediaStreamType,
    val playbackType: MediaPlaybackType,
    val logoUrl: String,
)

data class ChannelCategory(
    val id: String,
    val title: String,
    val channels: List<LiveChannel>,
)

data class MovieSummary(
    val id: String,
    val categoryId: String,
    val title: String,
    val subtitle: String,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val streamType: MediaStreamType,
    val playbackType: MediaPlaybackType,
)

data class MovieCategory(
    val id: String,
    val title: String,
    val movies: List<MovieSummary>,
)

data class MovieDetail(
    val id: String,
    val categoryId: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val streamType: MediaStreamType,
    val playbackType: MediaPlaybackType,
    val alsoWatch: List<MovieSummary>,
)
