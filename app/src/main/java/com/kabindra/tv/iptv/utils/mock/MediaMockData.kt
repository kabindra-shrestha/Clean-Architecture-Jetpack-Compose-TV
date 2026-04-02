package com.kabindra.tv.iptv.utils.mock

import com.kabindra.tv.iptv.data.model.ChannelCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveChannelDTO
import com.kabindra.tv.iptv.data.model.MediaStreamTypeDTO
import com.kabindra.tv.iptv.data.model.MovieCategoryDTO
import com.kabindra.tv.iptv.data.model.MovieDetailDTO
import com.kabindra.tv.iptv.data.model.MovieSummaryDTO

private const val muxHls = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

fun mockLiveTVCategories(): List<ChannelCategoryDTO> {
    return listOf(
        ChannelCategoryDTO(
            id = "featured",
            title = "Featured",
            channels = listOf(
                liveChannel(
                    id = "featured_1",
                    categoryId = "featured",
                    title = "Nature TV",
                    currentProgram = "Wildlife Stories",
                    streamUrl = muxHls,
                    logoLabel = "Nature+TV"
                ),
                liveChannel(
                    id = "featured_2",
                    categoryId = "featured",
                    title = "Action Max",
                    currentProgram = "Night Chase",
                    streamUrl = muxHls,
                    logoLabel = "Action+Max"
                ),
                liveChannel(
                    id = "featured_3",
                    categoryId = "featured",
                    title = "Family Hub",
                    currentProgram = "Weekend Picks",
                    streamUrl = muxHls,
                    logoLabel = "Family+Hub"
                )
            )
        ),
        ChannelCategoryDTO(
            id = "sports",
            title = "Sports",
            channels = listOf(
                liveChannel(
                    id = "sports_1",
                    categoryId = "sports",
                    title = "Arena One",
                    currentProgram = "Premier Highlights",
                    streamUrl = muxHls,
                    logoLabel = "Arena+One"
                ),
                liveChannel(
                    id = "sports_2",
                    categoryId = "sports",
                    title = "Sport Live",
                    currentProgram = "Game Night",
                    streamUrl = muxHls,
                    logoLabel = "Sport+Live"
                ),
                liveChannel(
                    id = "sports_3",
                    categoryId = "sports",
                    title = "Velocity",
                    currentProgram = "Racing Recap",
                    streamUrl = muxHls,
                    logoLabel = "Velocity"
                )
            )
        ),
        ChannelCategoryDTO(
            id = "kids",
            title = "Kids",
            channels = listOf(
                liveChannel(
                    id = "kids_1",
                    categoryId = "kids",
                    title = "Cartoon Time",
                    currentProgram = "Sunny Adventures",
                    streamUrl = muxHls,
                    logoLabel = "Cartoon+Time"
                ),
                liveChannel(
                    id = "kids_2",
                    categoryId = "kids",
                    title = "Junior Play",
                    currentProgram = "Playhouse Party",
                    streamUrl = muxHls,
                    logoLabel = "Junior+Play"
                )
            )
        )
    )
}

fun mockMovieCategories(): List<MovieCategoryDTO> {
    return listOf(
        MovieCategoryDTO(
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
        MovieCategoryDTO(
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
        MovieCategoryDTO(
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

fun mockMovieDetail(movieId: String): MovieDetailDTO {
    val categories = mockMovieCategories()
    val allMovie = categories.flatMap(MovieCategoryDTO::movies)
    val movie = requireNotNull(allMovie.firstOrNull { it.id == movieId }) {
        "Movie not found for id=$movieId"
    }

    return MovieDetailDTO(
        id = movie.id,
        categoryId = movie.categoryId,
        title = movie.title,
        subtitle = movie.subtitle,
        description = movieDescription(movie.id),
        posterUrl = movie.posterUrl,
        backdropUrl = movie.backdropUrl,
        streamUrl = movie.streamUrl,
        streamType = movie.streamType,
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
    logoLabel: String,
): LiveChannelDTO {
    return LiveChannelDTO(
        id = id,
        categoryId = categoryId,
        title = title,
        currentProgram = currentProgram,
        streamUrl = streamUrl,
        streamType = MediaStreamTypeDTO.Hls,
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
): MovieSummaryDTO {
    return MovieSummaryDTO(
        id = id,
        categoryId = categoryId,
        title = title,
        subtitle = subtitle,
        posterUrl = "https://picsum.photos/seed/$posterSeed/400/600",
        backdropUrl = "https://picsum.photos/seed/$backdropSeed/1280/720",
        streamUrl = streamUrl,
        streamType = MediaStreamTypeDTO.Hls
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
