package com.kabindra.tv.iptv.data.repository.remote.movie

import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.source.remote.movie.MovieRemoteDataSource
import com.kabindra.tv.iptv.domain.entity.VODCategory
import com.kabindra.tv.iptv.domain.entity.VODDetail
import com.kabindra.tv.iptv.domain.repository.remote.movie.MovieRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MovieRepositoryImpl(
    private val remoteDataSource: MovieRemoteDataSource,
) : MovieRepository {

    override suspend fun getMovieCategories(): Flow<Result<List<VODCategory>>> = flow {
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

    override suspend fun getMovieDetail(movieId: String): Flow<Result<VODDetail>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(remoteDataSource.getMovieDetail(movieId).toDomain()))
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }
}