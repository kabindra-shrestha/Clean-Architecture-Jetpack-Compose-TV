package com.kabindra.tv.iptv.domain.usecase.remote

import com.kabindra.tv.iptv.data.request.RefreshTokenDataRequest
import com.kabindra.tv.iptv.domain.entity.RefreshToken
import com.kabindra.tv.iptv.domain.repository.remote.RefreshTokenRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class RefreshTokenUseCase(private val repository: RefreshTokenRepository) {
    suspend fun executeGetRefreshToken(refreshTokenDataRequest: RefreshTokenDataRequest): Flow<Result<RefreshToken>> {
        return repository.getRefreshToken(refreshTokenDataRequest)
    }
}