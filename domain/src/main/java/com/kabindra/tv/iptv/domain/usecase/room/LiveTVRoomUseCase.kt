package com.kabindra.tv.iptv.domain.usecase.room

import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import com.kabindra.tv.iptv.domain.entity.LiveTVSyncSummary
import com.kabindra.tv.iptv.domain.repository.room.LiveTVRoomRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class LiveTVRoomUseCase(
    private val repository: LiveTVRoomRepository,
) {
    suspend fun hasLiveTVData(): Boolean {
        return repository.hasLiveTVData()
    }

    fun observeLiveTVCategories(): Flow<Result<List<LiveTVCategory>>> {
        return repository.observeLiveTVCategories()
    }

    fun observeLiveTVChannels(): Flow<Result<List<LiveTV>>> {
        return repository.observeLiveTVChannels()
    }

    fun syncLiveTVContent(): Flow<Result<LiveTVSyncSummary>> {
        return repository.syncLiveTVContent()
    }
}
