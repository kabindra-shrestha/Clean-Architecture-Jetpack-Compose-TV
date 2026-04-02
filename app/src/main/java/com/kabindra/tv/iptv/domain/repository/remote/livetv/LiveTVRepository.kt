package com.kabindra.tv.iptv.domain.repository.remote.livetv

import com.kabindra.tv.iptv.domain.entity.ChannelCategory
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface LiveTVRepository {
    suspend fun getLiveTVContent(): Flow<Result<List<ChannelCategory>>>
}
