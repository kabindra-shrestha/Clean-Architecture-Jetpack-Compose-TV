package com.kabindra.tv.iptv.data.source.xtream.livetv

import com.kabindra.tv.iptv.data.model.LiveTVCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveTVDTO
import com.kabindra.tv.iptv.data.source.xtream.XtreamService

interface LiveTVXtreamDataSource {
    suspend fun getLiveTVCategories(): List<LiveTVCategoryDTO>
    suspend fun getLiveTVChannels(): List<LiveTVDTO>
}

class LiveTVXtreamDataSourceImpl(private val xtreamService: XtreamService) :
    LiveTVXtreamDataSource {
    override suspend fun getLiveTVCategories(): List<LiveTVCategoryDTO> {
        return xtreamService.getLiveTVCategories()
    }

    override suspend fun getLiveTVChannels(): List<LiveTVDTO> {
        return xtreamService.getLiveTVChannels()
    }
}
