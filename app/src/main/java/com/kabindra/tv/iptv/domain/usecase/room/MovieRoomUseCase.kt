package com.kabindra.tv.iptv.domain.usecase.room

import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.entity.MovieSyncSummary
import com.kabindra.tv.iptv.domain.repository.room.MovieRoomRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class MovieRoomUseCase(
    private val repository: MovieRoomRepository,
) {
    suspend fun hasMovieData(): Boolean {
        return repository.hasMovieData()
    }

    fun observeMovieCategories(): Flow<Result<List<MovieCategory>>> {
        return repository.observeMovieCategories()
    }

    fun observeMovies(): Flow<Result<List<Movie>>> {
        return repository.observeMovies()
    }

    fun syncMovieContent(): Flow<Result<MovieSyncSummary>> {
        return repository.syncMovieContent()
    }
}
