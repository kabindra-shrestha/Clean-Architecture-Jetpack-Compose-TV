package com.kabindra.tv.iptv.domain.repository.media

import com.kabindra.tv.iptv.domain.entity.media.MovieCategory
import com.kabindra.tv.iptv.domain.entity.media.MovieDetail
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {
    suspend fun getMovieCategories(): Flow<Result<List<MovieCategory>>>
    suspend fun getMovieDetail(movieId: String): Flow<Result<MovieDetail>>
}
