package com.kabindra.tv.iptv.data.repository.media

import com.kabindra.tv.iptv.data.model.media.toDomain
import com.kabindra.tv.iptv.data.source.remote.movies.MoviesRemoteDataSource
import com.kabindra.tv.iptv.domain.entity.media.MovieCategory
import com.kabindra.tv.iptv.domain.entity.media.MovieDetail
import com.kabindra.tv.iptv.domain.repository.media.MoviesRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MoviesRepositoryImpl(
    private val remoteDataSource: MoviesRemoteDataSource,
) : MoviesRepository {

    override suspend fun getMovieCategories(): Flow<Result<List<MovieCategory>>> = flow {
        emit(Result.Loading)
        try {
            emit(
                Result.Success(
                    remoteDataSource.getMovieCategories().map { it.toDomain() }
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }

    override suspend fun getMovieDetail(movieId: String): Flow<Result<MovieDetail>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(remoteDataSource.getMovieDetail(movieId).toDomain()))
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }
}
