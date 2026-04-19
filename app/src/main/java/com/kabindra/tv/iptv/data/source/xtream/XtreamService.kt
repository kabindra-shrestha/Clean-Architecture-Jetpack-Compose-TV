package com.kabindra.tv.iptv.data.source.xtream

import com.kabindra.tv.iptv.data.model.LiveTVCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveTVDTO
import io.github.saifullah.xtream.Xtream
import io.github.saifullah.xtream.model.XtreamCategory
import io.github.saifullah.xtream.model.XtreamMovie
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
            url("http://tv.quierover.xyz/player_api.php?username=SAMIR18&password=Banana18&action=get_live_streams&params[offset]=10&params[items_per_page]=2")
        }
    }

    suspend fun getMovies(): List<XtreamMovie> {
        return xtream.movie.getMovies()
    }

    suspend fun getMovieCategories(): List<XtreamCategory> {
        return xtream.movie.getMovieCategories()
    }

    suspend fun getMovieDetail(streamId: Long): XtreamMovieDetail {
        return xtream.movie.getMovieDetail(streamId = streamId)
    }

}