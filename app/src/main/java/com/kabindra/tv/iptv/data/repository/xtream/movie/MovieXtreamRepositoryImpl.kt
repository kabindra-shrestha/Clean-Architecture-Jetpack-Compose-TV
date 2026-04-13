package com.kabindra.tv.iptv.data.repository.xtream.movie

import com.kabindra.tv.iptv.data.source.xtream.movie.MovieXtreamDataSource
import com.kabindra.tv.iptv.domain.repository.xtream.movie.MovieXtreamRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import io.github.saifullah.xtream.model.XtreamCategory
import io.github.saifullah.xtream.model.XtreamMovie
import io.github.saifullah.xtream.model.XtreamMovieDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MovieXtreamRepositoryImpl(
    private val xtreamDataSource: MovieXtreamDataSource,
) : MovieXtreamRepository {

    override suspend fun getMovies(): Flow<Result<List<XtreamMovie>>> = flow {
        emit(Result.Loading)
        try {
            emit(
                Result.Success(
                    xtreamDataSource.getMovies().map { it }
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }

    override suspend fun getMovieCategories(): Flow<Result<List<XtreamCategory>>> = flow {
        emit(Result.Loading)
        try {
            emit(
                Result.Success(
                    xtreamDataSource.getMovieCategories().map { it }
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }

    override suspend fun getMovieDetail(streamId: Long): Flow<Result<XtreamMovieDetail>> = flow {
        emit(Result.Loading)
        try {
            emit(Result.Success(xtreamDataSource.getMovieDetail(streamId)))
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }

}