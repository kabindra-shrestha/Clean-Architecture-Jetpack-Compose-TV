package com.kabindra.tv.iptv.domain.repository.remote.movie

import com.kabindra.tv.iptv.domain.entity.VODCategory
import com.kabindra.tv.iptv.domain.entity.VODDetail
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getMovieCategories(): Flow<Result<List<VODCategory>>>
    suspend fun getMovieDetail(movieId: String): Flow<Result<VODDetail>>
}