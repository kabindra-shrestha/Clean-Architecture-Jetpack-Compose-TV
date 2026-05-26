package com.kabindra.tv.iptv.data.source.xtream.livetv

import com.kabindra.tv.iptv.data.model.LiveTVCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveTVDTO
import com.kabindra.tv.iptv.data.source.xtream.XtreamService
import com.kabindra.tv.iptv.domain.repository.session.CurrentUserRepository

interface LiveTVXtreamDataSource {
    suspend fun getLiveTVCategories(): List<LiveTVCategoryDTO>
    suspend fun getLiveTVChannels(
        offset: Int = 0,
        itemsPerPage: Int = 30,
    ): List<LiveTVDTO>
}

class LiveTVXtreamDataSourceImpl(
    private val xtreamService: XtreamService,
    private val userCredentialsProvider: CurrentUserRepository
) : LiveTVXtreamDataSource {

    override suspend fun getLiveTVCategories(): List<LiveTVCategoryDTO> {
        val user = userCredentialsProvider.getCurrentUser()
            ?: throw IllegalStateException("User not logged in")

        return xtreamService.getLiveTVCategories(
            serverName = user.server_name ?: throw IllegalStateException("Server name not found"),
            username = user.username ?: throw IllegalStateException("Username not found"),
            password = user.password ?: throw IllegalStateException("Password not found")
        )
    }

    override suspend fun getLiveTVChannels(
        offset: Int,
        itemsPerPage: Int,
    ): List<LiveTVDTO> {
        val user = userCredentialsProvider.getCurrentUser()
            ?: throw IllegalStateException("User not logged in")

        return xtreamService.getLiveTVChannels(
            serverName = user.server_name ?: throw IllegalStateException("Server name not found"),
            username = user.username ?: throw IllegalStateException("Username not found"),
            password = user.password ?: throw IllegalStateException("Password not found"),
            offset = offset,
            itemsPerPage = itemsPerPage,
        )
    }
}
