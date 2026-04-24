package com.kabindra.tv.iptv.data.source.xtream.movie

import com.kabindra.tv.iptv.data.model.MovieCategoryDTO
import com.kabindra.tv.iptv.data.model.MovieDTO
import com.kabindra.tv.iptv.data.model.MovieDetailDTO
import com.kabindra.tv.iptv.data.source.xtream.XtreamService
import io.github.saifullah.xtream.model.XtreamMovieDetail

interface MovieXtreamDataSource {
    suspend fun getMovies(): List<MovieDTO>
    suspend fun getMovieCategories(): List<MovieCategoryDTO>
    suspend fun getMovieDetail(streamId: Long): MovieDetailDTO
}

class MovieXtreamDataSourceImpl(private val xtreamService: XtreamService) : MovieXtreamDataSource {

    override suspend fun getMovies(): List<MovieDTO> {
        return xtreamService.getMovies()
    }

    override suspend fun getMovieCategories(): List<MovieCategoryDTO> {
        return xtreamService.getMovieCategories()
    }

    override suspend fun getMovieDetail(streamId: Long): MovieDetailDTO {
        return xtreamService.getMovieDetail(streamId)
    }

}
