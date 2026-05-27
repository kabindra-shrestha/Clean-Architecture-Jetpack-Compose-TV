package com.kabindra.tv.iptv.data.source.xtream

import com.kabindra.tv.iptv.data.model.LiveTVCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveTVDTO
import com.kabindra.tv.iptv.data.model.MovieCategoryDTO
import com.kabindra.tv.iptv.data.model.MovieDTO
import com.kabindra.tv.iptv.data.model.MovieDetailDTO
import io.github.saifullah.xtream.Xtream
import io.ktor.client.request.url

class XtreamService(private val xtream: Xtream) {

    /**
     * Builds the API URL with user credentials
     * @param serverName: Base URL of the server (e.g., "tv.example.com")
     * @param username: User's username
     * @param password: User's password
     * @param action: API action to perform
     * @param additionalParams: Additional URL parameters
     */
    private fun buildApiUrl(
        serverName: String,
        username: String,
        password: String,
        action: String,
        additionalParams: String = ""
    ): String {
        val url =
            "http://$serverName/player_api.php?username=$username&password=$password&action=$action$additionalParams"
        println("XtreamService: $url")
        return url
    }

    suspend fun getLiveTVCategories(
        serverName: String,
        username: String,
        password: String
    ): List<LiveTVCategoryDTO> {
        return xtream.custom.get {
            url(buildApiUrl(serverName, username, password, "get_live_categories"))
        }
    }

    suspend fun getLiveTVChannels(
        serverName: String,
        username: String,
        password: String,
        offset: Int = 0,
        itemsPerPage: Int = 30
    ): List<LiveTVDTO> {
        val params = "&params[offset]=$offset&params[items_per_page]=$itemsPerPage"
        return xtream.custom.get {
            url(buildApiUrl(serverName, username, password, "get_live_streams", params))
        }
    }

    suspend fun getMovieCategories(
        serverName: String,
        username: String,
        password: String
    ): List<MovieCategoryDTO> {
        return xtream.custom.get {
            url(buildApiUrl(serverName, username, password, "get_vod_categories"))
        }
    }

    suspend fun getMovies(
        serverName: String,
        username: String,
        password: String,
        offset: Int? = null,
        itemsPerPage: Int? = null
    ): List<MovieDTO> {
        val params = if (offset != null && itemsPerPage != null) {
            "&params[offset]=$offset&params[items_per_page]=$itemsPerPage"
        } else {
            ""
        }
        return xtream.custom.get {
            url(buildApiUrl(serverName, username, password, "get_vod_streams", params))
        }
    }

    suspend fun getMovieDetail(
        serverName: String,
        username: String,
        password: String,
        streamId: Long
    ): MovieDetailDTO {
        val params = "&vod_id=$streamId"
        return xtream.custom.get {
            url(buildApiUrl(serverName, username, password, "get_vod_info", params))
        }
    }

}
