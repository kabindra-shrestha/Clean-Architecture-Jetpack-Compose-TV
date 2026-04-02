package com.kabindra.tv.iptv.domain.repository.media

import com.kabindra.tv.iptv.domain.entity.media.ChannelCategory
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface LiveTvRepository {
    suspend fun getLiveTvContent(): Flow<Result<List<ChannelCategory>>>
}
