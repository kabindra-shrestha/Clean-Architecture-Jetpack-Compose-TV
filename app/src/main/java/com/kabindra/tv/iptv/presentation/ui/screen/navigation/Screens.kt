package com.kabindra.tv.iptv.presentation.ui.screen.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

enum class Screens(val title: String) {
    Splash(title = "Splash"),
    Dashboard(title = "Dashboard"),
}

@Serializable
data object SplashRoute : NavKey

@Serializable
data object DashboardRoute : NavKey