package com.kabindra.tv.iptv.data.source.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

class ApiService(private val client: HttpClient) {

    suspend fun getLiveTVContent(): HttpResponse {
        return client.get(ApiEndpoints.API_LIVE_TV_CONTENT) {
        }
    }

}