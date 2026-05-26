package com.kabindra.tv.iptv.domain.repository.xtream.livetv

import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface LiveTVXtreamRepository {
    suspend fun getLiveTVCategories(): Flow<Result<List<LiveTVCategory>>>
    suspend fun getLiveTVChannels(): Flow<Result<List<LiveTV>>>
}