package com.kabindra.tv.iptv.data.repository.remote

import com.kabindra.tv.iptv.data.model.RefreshTokenDTO
import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.request.RefreshTokenDataRequest
import com.kabindra.tv.iptv.data.source.remote.ApiDataSource
import com.kabindra.tv.iptv.domain.entity.RefreshToken
import com.kabindra.tv.iptv.domain.repository.remote.RefreshTokenRepository
import com.kabindra.tv.iptv.utils.enums.Status
import com.kabindra.tv.iptv.utils.enums.getStatus
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RefreshTokenRepositoryImpl(
    private val apiDataSource: ApiDataSource,
) : RefreshTokenRepository {

    override suspend fun getRefreshToken(refreshTokenDataRequest: RefreshTokenDataRequest): Flow<Result<RefreshToken>> =
        flow {
            emit(Result.Loading)
            try {
                val response: HttpResponse = apiDataSource.getRefreshToken(refreshTokenDataRequest)
                if (response.status.isSuccess()) {
                    val responses: RefreshTokenDTO = response.body()

                    if (getStatus<Status>(responses.status)) {
                        emit(Result.Success(responses.toDomain()))
                    } else {
                        emit(Result.Error(ResultError.parseError(response)))
                    }
                } else {
                    emit(Result.Error(ResultError.parseError(response)))
                }
            } catch (e: Exception) {
                emit(Result.Error(ResultError.parseException(e)))
            }
        }

}