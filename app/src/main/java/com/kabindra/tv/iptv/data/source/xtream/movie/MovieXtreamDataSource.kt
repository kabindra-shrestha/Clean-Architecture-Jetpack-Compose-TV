package com.kabindra.tv.iptv.data.source.xtream.movie

import com.kabindra.tv.iptv.data.source.xtream.XtreamService
import io.github.saifullah.xtream.model.XtreamCategory
import io.github.saifullah.xtream.model.XtreamMovie
import io.github.saifullah.xtream.model.XtreamMovieDetail

interface MovieXtreamDataSource {
    suspend fun getMovies(): List<XtreamMovie>
    suspend fun getMovieCategories(): List<XtreamCategory>
    suspend fun getMovieDetail(streamId: Long): XtreamMovieDetail
}

class MovieXtreamDataSourceImpl(private val xtreamService: XtreamService) : MovieXtreamDataSource {

    override suspend fun getMovies(): List<XtreamMovie> {
        return xtreamService.getMovies()
    }

    override suspend fun getMovieCategories(): List<XtreamCategory> {
        return xtreamService.getMovieCategories()
    }

    override suspend fun getMovieDetail(streamId: Long): XtreamMovieDetail {
        return xtreamService.getMovieDetail(streamId)
    }

}
