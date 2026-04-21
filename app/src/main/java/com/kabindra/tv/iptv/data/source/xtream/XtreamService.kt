package com.kabindra.tv.iptv.data.source.xtream

import com.kabindra.tv.iptv.data.model.LiveTVCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveTVDTO
import com.kabindra.tv.iptv.data.model.MovieCategoryDTO
import com.kabindra.tv.iptv.data.model.MovieDTO
import io.github.saifullah.xtream.Xtream
import io.github.saifullah.xtream.model.XtreamMovieDetail
import io.ktor.client.request.url

class XtreamService(private val xtream: Xtream) {

    suspend fun getLiveTVCategories(): List<LiveTVCategoryDTO> {
        return xtream.custom.get {
            url("http://tv.quierover.xyz/player_api.php?username=SAMIR18&password=Banana18&action=get_live_categories")
        }
    }

    suspend fun getLiveTVChannels(): List<LiveTVDTO> {
        return xtream.custom.get {
            url("http://tv.quierover.xyz/player_api.php?username=SAMIR18&password=Banana18&action=get_live_streams&params[offset]=10&params[items_per_page]=3")
        }
    }

    suspend fun getMovieCategories(): List<MovieCategoryDTO> {
        return xtream.custom.get {
            url("http://tv.quierover.xyz/player_api.php?username=SAMIR18&password=Banana18&action=get_vod_categories")
        }
    }

    suspend fun getMovies(): List<MovieDTO> {
        return xtream.custom.get {
            url("http://tv.quierover.xyz/player_api.php?username=SAMIR18&password=Banana18&action=get_vod_streams&params[offset]=10&params[items_per_page]=2")
        }
    }

    suspend fun getMovieDetail(streamId: Long): XtreamMovieDetail {
        return xtream.custom.get {
            url("http://tv.quierover.xyz/player_api.php?username=SAMIR18&password=Banana18&action=get_vod_info&vod_id=$streamId")
        }
    }

}