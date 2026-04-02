package com.kabindra.tv.iptv.domain.usecase.media

import com.kabindra.tv.iptv.domain.entity.media.ChannelCategory
import com.kabindra.tv.iptv.domain.repository.media.LiveTvRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class LiveTvUseCase(
    private val repository: LiveTvRepository,
) {
    suspend fun executeGetLiveTvContent(): Flow<Result<List<ChannelCategory>>> {
        return repository.getLiveTvContent()
    }
}
