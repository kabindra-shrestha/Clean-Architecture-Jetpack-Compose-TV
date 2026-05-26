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

data class VODSummary(
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

data class VODCategory(
    val id: String,
    val title: String,
    val movies: List<VODSummary>,
)

data class VODDetail(
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
    val alsoWatch: List<VODSummary>,
    val subtitleUrls: List<String> = emptyList(),
)
