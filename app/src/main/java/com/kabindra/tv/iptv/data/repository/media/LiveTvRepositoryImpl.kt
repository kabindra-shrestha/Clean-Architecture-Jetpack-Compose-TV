package com.kabindra.tv.iptv.data.repository.media

import com.kabindra.tv.iptv.data.model.media.toDomain
import com.kabindra.tv.iptv.data.source.remote.livetv.LiveTvRemoteDataSource
import com.kabindra.tv.iptv.domain.entity.media.ChannelCategory
import com.kabindra.tv.iptv.domain.repository.media.LiveTvRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LiveTvRepositoryImpl(
    private val remoteDataSource: LiveTvRemoteDataSource,
) : LiveTvRepository {

    override suspend fun getLiveTvContent(): Flow<Result<List<ChannelCategory>>> = flow {
        emit(Result.Loading)
        try {
            emit(
                Result.Success(
                    remoteDataSource.getLiveTvContent().map { it.toDomain() }
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }
}
