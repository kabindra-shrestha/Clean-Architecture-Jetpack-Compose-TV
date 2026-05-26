package com.kabindra.tv.iptv.data.repository.room

import com.kabindra.tv.iptv.data.model.MovieDTO
import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.source.room.AppDatabase
import com.kabindra.tv.iptv.data.source.xtream.movie.MovieXtreamDataSource
import com.kabindra.tv.iptv.domain.entity.Movie
import com.kabindra.tv.iptv.domain.entity.MovieCategory
import com.kabindra.tv.iptv.domain.entity.MovieSyncSummary
import com.kabindra.tv.iptv.domain.repository.room.MovieRoomRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

private const val MOVIE_PAGE_SIZE = 500

class MovieRoomRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val xtreamDataSource: MovieXtreamDataSource,
) : MovieRoomRepository {

    override suspend fun hasMovieData(): Boolean {
        return appDatabase.movieDao.hasMovieData()
    }

    override fun observeMovieCategories(): Flow<Result<List<MovieCategory>>> {
        return appDatabase.movieDao.observeCategories()
            .map { categories ->
                Result.Success(categories.map { it.toDomain() }) as Result<List<MovieCategory>>
            }
            .onStart { emit(Result.Loading) }
            .catch { throwable ->
                emit(Result.Error(ResultError.parseException(throwable.toException())))
            }
    }

    override fun observeMovies(): Flow<Result<List<Movie>>> {
        return appDatabase.movieDao.observeMovies()
            .map { movies ->
                Result.Success(movies.map { it.toDomain() }) as Result<List<Movie>>
            }
            .onStart { emit(Result.Loading) }
            .catch { throwable ->
                emit(Result.Error(ResultError.parseException(throwable.toException())))
            }
    }

    override fun syncMovieContent(): Flow<Result<MovieSyncSummary>> = flow {
        emit(Result.Loading)

        try {
            val categories = xtreamDataSource.getMovieCategories()
                .filter { it.category_id.isNotBlank() }
                .distinctBy { it.category_id }
            val movies = fetchAllMovies()

            appDatabase.movieDao.replaceAll(
                categories = categories,
                movies = movies,
            )

            emit(
                Result.Success(
                    MovieSyncSummary(
                        categoryCount = categories.size,
                        movieCount = movies.size,
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }

    private suspend fun fetchAllMovies(): List<MovieDTO> {
        val movies = linkedMapOf<Int, MovieDTO>()
        var offset = 0

        while (true) {
            val page = xtreamDataSource.getMovies(
                offset = offset,
                itemsPerPage = MOVIE_PAGE_SIZE,
            )

            if (page.isEmpty()) break

            val previousSize = movies.size
            page.filter { it.stream_id > 0 }
                .forEach { movie ->
                    movies[movie.stream_id] = movie
                }

            val addedCount = movies.size - previousSize
            if (page.size < MOVIE_PAGE_SIZE || addedCount == 0) break

            offset += MOVIE_PAGE_SIZE
        }

        return movies.values.toList()
    }

    private fun Throwable.toException(): Exception {
        return this as? Exception ?: Exception(this)
    }
}
