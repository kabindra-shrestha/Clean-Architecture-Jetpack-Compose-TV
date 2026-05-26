package com.kabindra.tv.iptv.data.repository.xtream.livetv

import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.source.xtream.livetv.LiveTVXtreamDataSource
import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import com.kabindra.tv.iptv.domain.repository.xtream.livetv.LiveTVXtreamRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LiveTVXtreamRepositoryImpl(
    private val xtreamDataSource: LiveTVXtreamDataSource,
) : LiveTVXtreamRepository {

    override suspend fun getLiveTVCategories(): Flow<Result<List<LiveTVCategory>>> = flow {
        emit(Result.Loading)
        try {
            emit(
                Result.Success(
                    xtreamDataSource.getLiveTVCategories().map { it.toDomain() }
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }

    override suspend fun getLiveTVChannels(): Flow<Result<List<LiveTV>>> = flow {
        emit(Result.Loading)
        try {
            emit(
                Result.Success(
                    xtreamDataSource.getLiveTVChannels().map { it.toDomain() }
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }

}