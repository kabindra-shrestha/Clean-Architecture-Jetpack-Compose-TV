package com.kabindra.tv.iptv.data.source.remote

import com.kabindra.tv.iptv.data.request.RefreshTokenDataRequest
import io.ktor.client.statement.HttpResponse

class ApiDataSource(private val apiService: ApiService) {

    suspend fun getRefreshToken(refreshTokenDataRequest: RefreshTokenDataRequest): HttpResponse {
        return apiService.getRefreshToken(
            refreshTokenDataRequest.refreshToken
        )
    }

}