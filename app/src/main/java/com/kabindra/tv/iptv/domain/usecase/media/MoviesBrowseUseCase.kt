package com.kabindra.tv.iptv.domain.usecase.media

import com.kabindra.tv.iptv.domain.entity.media.MovieCategory
import com.kabindra.tv.iptv.domain.repository.media.MoviesRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class MoviesBrowseUseCase(
    private val repository: MoviesRepository,
) {
    suspend fun executeGetMovieCategories(): Flow<Result<List<MovieCategory>>> {
        return repository.getMovieCategories()
    }
}
