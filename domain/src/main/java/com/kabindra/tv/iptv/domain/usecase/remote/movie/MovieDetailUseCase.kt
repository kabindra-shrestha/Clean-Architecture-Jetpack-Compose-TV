package com.kabindra.tv.iptv.domain.usecase.remote.movie

import com.kabindra.tv.iptv.domain.entity.VODDetail
import com.kabindra.tv.iptv.domain.repository.remote.movie.MovieRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class MovieDetailUseCase(
    private val repository: MovieRepository,
) {
    suspend fun executeGetMovieDetail(movieId: String): Flow<Result<VODDetail>> {
        return repository.getMovieDetail(movieId)
    }
}
