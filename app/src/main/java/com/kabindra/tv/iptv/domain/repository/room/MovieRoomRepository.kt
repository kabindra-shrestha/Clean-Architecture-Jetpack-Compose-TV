package com.kabindra.tv.iptv.domain.repository.room

import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.entity.MovieSyncSummary
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface MovieRoomRepository {
    suspend fun hasMovieData(): Boolean
    fun observeMovieCategories(): Flow<Result<List<MovieCategory>>>
    fun observeMovies(): Flow<Result<List<Movie>>>
    fun syncMovieContent(): Flow<Result<MovieSyncSummary>>
}
