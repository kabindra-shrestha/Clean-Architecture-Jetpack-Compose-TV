package com.kabindra.tv.iptv.domain.repository.xtream.movie

import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.utils.ktor.Result
import io.github.saifullah.xtream.model.XtreamMovieDetail
import kotlinx.coroutines.flow.Flow

interface MovieXtreamRepository {
    suspend fun getMovieCategories(): Flow<Result<List<MovieCategory>>>
    suspend fun getMovies(): Flow<Result<List<Movie>>>
    suspend fun getMovieDetail(streamId: Long): Flow<Result<XtreamMovieDetail>>
}