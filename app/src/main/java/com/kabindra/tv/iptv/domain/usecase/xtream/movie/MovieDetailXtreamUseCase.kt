package com.kabindra.tv.iptv.domain.usecase.xtream.movie

import com.kabindra.tv.iptv.domain.repository.xtream.movie.MovieXtreamRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import io.github.saifullah.xtream.model.XtreamMovieDetail
import kotlinx.coroutines.flow.Flow

class MovieDetailXtreamUseCase(
    private val repository: MovieXtreamRepository,
) {
    suspend fun executeGetMovieDetail(streamId: Long): Flow<Result<XtreamMovieDetail>> {
        return repository.getMovieDetail(streamId)
    }
}
