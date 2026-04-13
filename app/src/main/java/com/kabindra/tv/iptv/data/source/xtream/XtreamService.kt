package com.kabindra.tv.iptv.data.source.xtream

import io.github.saifullah.xtream.Xtream
import io.github.saifullah.xtream.model.XtreamCategory
import io.github.saifullah.xtream.model.XtreamMovie
import io.github.saifullah.xtream.model.XtreamMovieDetail

class XtreamService(private val xtream: Xtream) {

    suspend fun getMovies(): List<XtreamMovie> {
        return xtream.movie.getMovies()
    }

    suspend fun getMovieCategories(): List<XtreamCategory> {
        return xtream.movie.getMovieCategories()
    }

    suspend fun getMovieDetail(streamId: Long): XtreamMovieDetail {
        return xtream.movie.getMovieDetail(streamId = streamId)
    }

}