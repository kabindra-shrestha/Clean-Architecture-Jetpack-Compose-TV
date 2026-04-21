package com.kabindra.tv.iptv.utils.mock

import com.kabindra.tv.iptv.data.model.ChannelCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveChannelDTO
import com.kabindra.tv.iptv.data.model.MediaPlaybackTypeDTO
import com.kabindra.tv.iptv.data.model.MediaStreamTypeDTO
import com.kabindra.tv.iptv.data.model.VODCategoryDTO
import com.kabindra.tv.iptv.data.model.VODDetailDTO
import com.kabindra.tv.iptv.data.model.VODSummaryDTO

private const val muxHls = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

fun mockLiveTVCategories(): List<ChannelCategoryDTO> {
    return listOf(
        ChannelCategoryDTO(
            id = "live",
            title = "Live",
            channels = listOf(
                liveChannel(
                    id = "live_1",
                    categoryId = "live",
                    title = "Nature TV",
                    currentProgram = "Wildlife Stories",
                    streamUrl = muxHls,
                    playbackType = MediaPlaybackTypeDTO.Live,
                    logoLabel = "Nature+TV"
                ),
                liveChannel(
                    id = "live_2",
                    categoryId = "live",
                    title = "Action Max",
                    currentProgram = "Night Chase",
                    streamUrl = muxHls,
                    playbackType = MediaPlaybackTypeDTO.Live,
                    logoLabel = "Action+Max"
                )
            )
        ),
        ChannelCategoryDTO(
            id = "dvr",
            title = "DVR",
            channels = listOf(
                liveChannel(
                    id = "dvr_1",
                    categoryId = "dvr",
                    title = "Arena One",
                    currentProgram = "Premier Highlights",
                    streamUrl = muxHls,
                    playbackType = MediaPlaybackTypeDTO.Dvr,
                    logoLabel = "Arena+One"
                ),
                liveChannel(
                    id = "dvr_2",
                    categoryId = "dvr",
                    title = "Sport Live",
                    currentProgram = "Game Night",
                    streamUrl = muxHls,
                    playbackType = MediaPlaybackTypeDTO.Dvr,
                    logoLabel = "Sport+Live"
                )
            )
        )
    )
}

fun mockMovieCategories(): List<VODCategoryDTO> {
    return listOf(
        VODCategoryDTO(
            id = "action",
            title = "Action",
            movies = listOf(
                movie(
                    id = "action_1",
                    categoryId = "action",
                    title = "Cosmos Archive",
                    subtitle = "Action • 1h 42m",
                    posterSeed = "cosmos-archive-poster",
                    backdropSeed = "cosmos-archive-backdrop",
                    streamUrl = muxHls
                ),
                movie(
                    id = "action_2",
                    categoryId = "action",
                    title = "Iron Signal",
                    subtitle = "Action • 2h 04m",
                    posterSeed = "iron-signal-poster",
                    backdropSeed = "iron-signal-backdrop",
                    streamUrl = muxHls
                ),
                movie(
                    id = "action_3",
                    categoryId = "action",
                    title = "Pulse Run",
                    subtitle = "Action • 1h 37m",
                    posterSeed = "pulse-run-poster",
                    backdropSeed = "pulse-run-backdrop",
                    streamUrl = muxHls
                )
            )
        ),
        VODCategoryDTO(
            id = "drama",
            title = "Drama",
            movies = listOf(
                movie(
                    id = "drama_1",
                    categoryId = "drama",
                    title = "Quiet Harbor",
                    subtitle = "Drama • 1h 54m",
                    posterSeed = "quiet-harbor-poster",
                    backdropSeed = "quiet-harbor-backdrop",
                    streamUrl = muxHls
                ),
                movie(
                    id = "drama_2",
                    categoryId = "drama",
                    title = "After Rain",
                    subtitle = "Drama • 1h 46m",
                    posterSeed = "after-rain-poster",
                    backdropSeed = "after-rain-backdrop",
                    streamUrl = muxHls
                ),
                movie(
                    id = "drama_3",
                    categoryId = "drama",
                    title = "Blue Horizon",
                    subtitle = "Drama • 1h 39m",
                    posterSeed = "blue-horizon-poster",
                    backdropSeed = "blue-horizon-backdrop",
                    streamUrl = muxHls
                )
            )
        ),
        VODCategoryDTO(
            id = "documentary",
            title = "Documentary",
            movies = listOf(
                movie(
                    id = "doc_1",
                    categoryId = "documentary",
                    title = "Ocean Atlas",
                    subtitle = "Documentary • 58m",
                    posterSeed = "ocean-atlas-poster",
                    backdropSeed = "ocean-atlas-backdrop",
                    streamUrl = muxHls
                ),
                movie(
                    id = "doc_2",
                    categoryId = "documentary",
                    title = "Skyline Earth",
                    subtitle = "Documentary • 1h 12m",
                    posterSeed = "skyline-earth-poster",
                    backdropSeed = "skyline-earth-backdrop",
                    streamUrl = muxHls
                ),
                movie(
                    id = "doc_3",
                    categoryId = "documentary",
                    title = "Deep Forest",
                    subtitle = "Documentary • 49m",
                    posterSeed = "deep-forest-poster",
                    backdropSeed = "deep-forest-backdrop",
                    streamUrl = muxHls
                )
            )
        )
    )
}

fun mockMovieDetail(movieId: String): VODDetailDTO {
    val categories = mockMovieCategories()
    val allMovie = categories.flatMap(VODCategoryDTO::movies)
    val movie = requireNotNull(allMovie.firstOrNull { it.id == movieId }) {
        "Movie not found for id=$movieId"
    }

    return VODDetailDTO(
        id = movie.id,
        categoryId = movie.categoryId,
        title = movie.title,
        subtitle = movie.subtitle,
        description = movieDescription(movie.id),
        posterUrl = movie.posterUrl,
        backdropUrl = movie.backdropUrl,
        streamUrl = movie.streamUrl,
        streamType = movie.streamType,
        playbackType = movie.playbackType,
        alsoWatch = allMovie
            .filterNot { it.id == movieId }
            .take(5)
    )
}

private fun liveChannel(
    id: String,
    categoryId: String,
    title: String,
    currentProgram: String,
    streamUrl: String,
    playbackType: MediaPlaybackTypeDTO,
    logoLabel: String,
): LiveChannelDTO {
    return LiveChannelDTO(
        id = id,
        categoryId = categoryId,
        title = title,
        currentProgram = currentProgram,
        streamUrl = streamUrl,
        streamType = MediaStreamTypeDTO.Hls,
        playbackType = playbackType,
        logoUrl = "https://placehold.co/240x135/FFFFFF/1A1029.png?text=$logoLabel"
    )
}

private fun movie(
    id: String,
    categoryId: String,
    title: String,
    subtitle: String,
    posterSeed: String,
    backdropSeed: String,
    streamUrl: String,
): VODSummaryDTO {
    return VODSummaryDTO(
        id = id,
        categoryId = categoryId,
        title = title,
        subtitle = subtitle,
        posterUrl = "https://picsum.photos/seed/$posterSeed/400/600.jpg",
        backdropUrl = "https://picsum.photos/seed/$backdropSeed/1280/720.jpg",
        streamUrl = streamUrl,
        streamType = MediaStreamTypeDTO.Hls,
        playbackType = MediaPlaybackTypeDTO.Movie
    )
}

private fun movieDescription(movieId: String): String {
    return when (movieId) {
        "action_1" -> "A deep-space recovery crew stumbles on a signal that should have stayed buried, and every answer pulls them closer to a war-sized secret."
        "action_2" -> "When a surveillance engineer disappears, a retired pilot is pulled back into the city and into a chase driven by corrupted memory archives."
        "action_3" -> "A courier with one final job has one night to outrun the syndicate that built the route, the city, and the trap waiting at the finish."
        "drama_1" -> "A harbor town changes after a long absence forces three siblings to reopen the family hotel and the quiet grief each of them left behind."
        "drama_2" -> "A violin teacher and a storm chaser find a fragile connection while rebuilding two very different lives in the same restless city."
        "drama_3" -> "An architect returns home to care for her father and discovers the blueprints he kept hidden say more about their family than he ever could."
        "doc_1" -> "An underwater journey across reef systems, migratory routes, and the communities working to understand a changing ocean."
        "doc_2" -> "A high-altitude portrait of the cities, satellites, and weather systems that make the planet feel both enormous and tightly connected."
        "doc_3" -> "Filmed through four seasons, this forest chronicle follows the subtle patterns of species adapting in one of the oldest ecosystems on earth."
        else -> "A curated sample title used to shape the movie detail experience until the live API arrives."
    }
}
