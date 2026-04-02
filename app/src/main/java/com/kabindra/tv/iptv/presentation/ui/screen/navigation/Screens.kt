package com.kabindra.tv.iptv.presentation.ui.screen.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

enum class Screens(val title: String) {
    Splash(title = "Splash"),
    Dashboard(title = "Dashboard"),
    LiveTvPlayer(title = "Live TV Player"),
    Movies(title = "Movies"),
    MovieDetail(title = "Movie Detail"),
    MoviePlayer(title = "Movie Player"),
}

@Serializable
data object SplashRoute : NavKey

@Serializable
data object DashboardRoute : NavKey

@Serializable
data object LiveTvPlayerRoute : NavKey

@Serializable
data object MoviesRoute : NavKey

@Serializable
data class MovieDetailRoute(val movieId: String) : NavKey

@Serializable
data class MoviePlayerRoute(val movieId: String) : NavKey
