package com.kabindra.tv.iptv.domain.repository.movie

import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.entity.MovieDetail
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getMovieCategories(): Flow<Result<List<MovieCategory>>>
    suspend fun getMovieDetail(movieId: String): Flow<Result<MovieDetail>>
}