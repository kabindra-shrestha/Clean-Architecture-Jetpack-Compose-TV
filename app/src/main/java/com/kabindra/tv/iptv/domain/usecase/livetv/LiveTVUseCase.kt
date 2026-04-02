package com.kabindra.tv.iptv.domain.usecase.livetv

import com.kabindra.tv.iptv.domain.entity.ChannelCategory
import com.kabindra.tv.iptv.domain.repository.livetv.LiveTVRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class LiveTVUseCase(
    private val repository: LiveTVRepository,
) {
    suspend fun executeGetLiveTVContent(): Flow<Result<List<ChannelCategory>>> {
        return repository.getLiveTVContent()
    }
}
