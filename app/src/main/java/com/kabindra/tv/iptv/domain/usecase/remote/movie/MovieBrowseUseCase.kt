package com.kabindra.tv.iptv.domain.usecase.remote.movie

import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.repository.remote.movie.MovieRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class MovieBrowseUseCase(
    private val repository: MovieRepository,
) {
    suspend fun executeGetMovieCategories(): Flow<Result<List<MovieCategory>>> {
        return repository.getMovieCategories()
    }
}
