package com.kabindra.tv.iptv.data.source.remote.livetv

import com.kabindra.tv.iptv.data.model.media.ChannelCategoryDto
import com.kabindra.tv.iptv.utils.mock.mockLiveTvCategories
import kotlinx.coroutines.delay

interface LiveTvRemoteDataSource {
    suspend fun getLiveTvContent(): List<ChannelCategoryDto>
}

class FakeLiveTvRemoteDataSource : LiveTvRemoteDataSource {
    override suspend fun getLiveTvContent(): List<ChannelCategoryDto> {
        delay(450)
        return mockLiveTvCategories()
    }
}
