package com.kabindra.tv.iptv.domain.usecase.xtream.movie

import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.repository.xtream.movie.MovieXtreamRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class MovieBrowseXtreamUseCase(
    private val repository: MovieXtreamRepository,
) {
    suspend fun executeGetMovieCategories(): Flow<Result<List<MovieCategory>>> {
        return repository.getMovieCategories()
    }

    suspend fun executeGetMovies(): Flow<Result<List<Movie>>> {
        return repository.getMovies()
    }
}
