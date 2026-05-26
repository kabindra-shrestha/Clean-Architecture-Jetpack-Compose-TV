package com.kabindra.tv.iptv.domain.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetail(
    @SerialName("info")
    val info: Info?,
    @SerialName("movie_data")
    val movieData: MovieData?
) {
    @Serializable
    data class Info(
        @SerialName("actors")
        val actors: String?,
        @SerialName("age")
        val age: String?,
        @SerialName("backdrop_path")
        val backdropPath: List<String>?,
        @SerialName("bitrate")
        val bitrate: Int?,
        @SerialName("cast")
        val cast: String?,
        @SerialName("country")
        val country: String?,
        @SerialName("cover_big")
        val coverBig: String?,
        @SerialName("description")
        val description: String?,
        @SerialName("director")
        val director: String?,
        @SerialName("duration")
        val duration: String?,
        @SerialName("duration_secs")
        val durationSecs: Int?,
        @SerialName("episode_run_time")
        val episodeRunTime: Int?,
        @SerialName("genre")
        val genre: String?,
        @SerialName("kinopoisk_url")
        val kinopoiskUrl: String?,
        @SerialName("movie_image")
        val movieImage: String?,
        @SerialName("mpaa_rating")
        val mpaaRating: String?,
        @SerialName("name")
        val name: String?,
        @SerialName("o_name")
        val oName: String?,
        @SerialName("plot")
        val plot: String?,
        @SerialName("rating")
        val rating: Double?,
        @SerialName("rating_count_kinopoisk")
        val ratingCountKinopoisk: Int?,
        @SerialName("release_date")
        val releaseDate: String?,
        @SerialName("releasedate")
        val releasedate: String?,
        @SerialName("subtitles")
        val subtitles: List<String>?,
        @SerialName("tmdb_id")
        val tmdbId: Int?,
        @SerialName("youtube_trailer")
        val youtubeTrailer: String?
    )

    @Serializable
    data class MovieData(
        @SerialName("added")
        val added: String?,
        @SerialName("category_id")
        val categoryId: String?,
        @SerialName("category_ids")
        val categoryIds: List<Int>?,
        @SerialName("container_extension")
        val containerExtension: String?,
        @SerialName("custom_sid")
        val customSid: String?,
        @SerialName("direct_source")
        val directSource: String?,
        @SerialName("name")
        val name: String?,
        @SerialName("stream_id")
        val streamId: Int?,
        @SerialName("title")
        val title: String?,
        @SerialName("year")
        val year: String?
    )
}

// Extension function to convert MovieDetail to VODDetail
fun MovieDetail.toVODDetail(): VODDetail {
    return VODDetail(
        id = movieData?.streamId?.toString() ?: "",
        categoryId = movieData?.categoryId ?: "",
        title = info?.name ?: movieData?.title ?: "",
        subtitle = info?.genre ?: "",
        description = info?.plot ?: info?.description ?: "",
        posterUrl = info?.coverBig ?: info?.movieImage ?: "",
        backdropUrl = info?.backdropPath?.firstOrNull() ?: "",
        streamUrl = movieData?.directSource ?: "",
        streamType = MediaStreamType.Progressive,
        playbackType = MediaPlaybackType.Movie,
        alsoWatch = emptyList(),
        subtitleUrls = info?.subtitles.orEmpty()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
    )
}
