package com.kabindra.tv.iptv.domain.usecase.xtream.livetv

import com.kabindra.tv.iptv.domain.entity.LiveTV
import com.kabindra.tv.iptv.domain.entity.LiveTVCategory
import com.kabindra.tv.iptv.domain.repository.xtream.livetv.LiveTVXtreamRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class LiveTVXtreamUseCase(
    private val repository: LiveTVXtreamRepository,
) {
    suspend fun executeGetLiveTVCategories(): Flow<Result<List<LiveTVCategory>>> {
        return repository.getLiveTVCategories()
    }

    suspend fun executeGetLiveTVChannels(): Flow<Result<List<LiveTV>>> {
        return repository.getLiveTVChannels()
    }
}
