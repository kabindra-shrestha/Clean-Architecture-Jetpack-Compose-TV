package com.kabindra.tv.iptv.domain.usecase.xtream.movie

import com.kabindra.tv.iptv.domain.repository.xtream.movie.MovieXtreamRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import io.github.saifullah.xtream.model.XtreamCategory
import io.github.saifullah.xtream.model.XtreamMovie
import kotlinx.coroutines.flow.Flow

class MovieBrowseXtreamUseCase(
    private val repository: MovieXtreamRepository,
) {
    suspend fun executeGetMovies(): Flow<Result<List<XtreamMovie>>> {
        return repository.getMovies()
    }

    suspend fun executeGetMovieCategories(): Flow<Result<List<XtreamCategory>>> {
        return repository.getMovieCategories()
    }
}
