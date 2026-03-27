package com.kabindra.tv.iptv.domain.repository.remote

import com.kabindra.tv.iptv.data.request.RefreshTokenDataRequest
import com.kabindra.tv.iptv.domain.entity.RefreshToken
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface RefreshTokenRepository {
    suspend fun getRefreshToken(refreshTokenDataRequest: RefreshTokenDataRequest): Flow<Result<RefreshToken>>
}