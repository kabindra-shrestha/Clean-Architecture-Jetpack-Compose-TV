package com.kabindra.tv.iptv.data.model

import com.kabindra.tv.iptv.domain.entity.MovieDetail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailDTO(
    @SerialName("info")
    val info: InfoDTO?,
    @SerialName("movie_data")
    val movieData: MovieDataDTO?
) {
    @Serializable
    data class InfoDTO(
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
    data class MovieDataDTO(
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

fun MovieDetailDTO.toDomain(): MovieDetail {
    return MovieDetail(
        info = info?.toDomain(),
        movieData = movieData?.toDomain()
    )
}

fun MovieDetailDTO.InfoDTO.toDomain(): MovieDetail.Info {
    return MovieDetail.Info(
        actors = actors,
        age = age,
        backdropPath = backdropPath,
        bitrate = bitrate,
        cast = cast,
        country = country,
        coverBig = coverBig,
        description = description,
        director = director,
        duration = duration,
        durationSecs = durationSecs,
        episodeRunTime = episodeRunTime,
        genre = genre,
        kinopoiskUrl = kinopoiskUrl,
        movieImage = movieImage,
        mpaaRating = mpaaRating,
        name = name,
        oName = oName,
        plot = plot,
        rating = rating,
        ratingCountKinopoisk = ratingCountKinopoisk,
        releaseDate = releaseDate,
        releasedate = releasedate,
        subtitles = subtitles,
        tmdbId = tmdbId,
        youtubeTrailer = youtubeTrailer
    )
}

fun MovieDetailDTO.MovieDataDTO.toDomain(): MovieDetail.MovieData {
    return MovieDetail.MovieData(
        added = added,
        categoryId = categoryId,
        categoryIds = categoryIds,
        containerExtension = containerExtension,
        customSid = customSid,
        directSource = directSource,
        name = name,
        streamId = streamId,
        title = title,
        year = year
    )
}
