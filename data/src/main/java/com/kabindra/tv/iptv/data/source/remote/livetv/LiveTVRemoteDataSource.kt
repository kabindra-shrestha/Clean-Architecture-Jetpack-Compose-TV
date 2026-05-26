package com.kabindra.tv.iptv.data.source.remote.livetv

import com.kabindra.tv.iptv.data.model.ChannelCategoryDTO
import com.kabindra.tv.iptv.utils.mock.mockLiveTVCategories
import kotlinx.coroutines.delay

interface LiveTVRemoteDataSource {
    suspend fun getLiveTVContent(): List<ChannelCategoryDTO>
}

class FakeLiveTVRemoteDataSource : LiveTVRemoteDataSource {
    override suspend fun getLiveTVContent(): List<ChannelCategoryDTO> {
        delay(450)
        return mockLiveTVCategories()
    }
}
