package com.kabindra.tv.iptv.data.repository.remote.livetv

import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.source.remote.livetv.LiveTVRemoteDataSource
import com.kabindra.tv.iptv.domain.entity.ChannelCategory
import com.kabindra.tv.iptv.domain.repository.remote.livetv.LiveTVRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LiveTVRepositoryImpl(
    private val remoteDataSource: LiveTVRemoteDataSource,
) : LiveTVRepository {

    override suspend fun getLiveTVContent(): Flow<Result<List<ChannelCategory>>> = flow {
        emit(Result.Loading)
        try {
            emit(
                Result.Success(
                    remoteDataSource.getLiveTVContent().map { it.toDomain() }
                )
            )
        } catch (exception: Exception) {
            emit(Result.Error(ResultError.parseException(exception)))
        }
    }
}