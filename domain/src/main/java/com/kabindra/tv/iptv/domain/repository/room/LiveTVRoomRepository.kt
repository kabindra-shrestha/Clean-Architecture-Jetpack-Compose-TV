package com.kabindra.tv.iptv.domain.repository.room

import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import com.kabindra.tv.iptv.domain.entity.LiveTVSyncSummary
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface LiveTVRoomRepository {
    suspend fun hasLiveTVData(): Boolean
    fun observeLiveTVCategories(): Flow<Result<List<LiveTVCategory>>>
    fun observeLiveTVChannels(): Flow<Result<List<LiveTV>>>
    fun syncLiveTVContent(): Flow<Result<LiveTVSyncSummary>>
}
