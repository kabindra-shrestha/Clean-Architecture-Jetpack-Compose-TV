package com.kabindra.tv.iptv.domain.usecase.media

import com.kabindra.tv.iptv.domain.entity.media.MovieDetail
import com.kabindra.tv.iptv.domain.repository.media.MoviesRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class MovieDetailUseCase(
    private val repository: MoviesRepository,
) {
    suspend fun executeGetMovieDetail(movieId: String): Flow<Result<MovieDetail>> {
        return repository.getMovieDetail(movieId)
    }
}
