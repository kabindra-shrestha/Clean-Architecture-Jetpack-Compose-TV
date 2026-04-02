package com.kabindra.tv.iptv.domain.usecase.movie

import com.kabindra.tv.iptv.domain.entity.MovieDetail
import com.kabindra.tv.iptv.domain.repository.movie.MovieRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class MovieDetailUseCase(
    private val repository: MovieRepository,
) {
    suspend fun executeGetMovieDetail(movieId: String): Flow<Result<MovieDetail>> {
        return repository.getMovieDetail(movieId)
    }
}
