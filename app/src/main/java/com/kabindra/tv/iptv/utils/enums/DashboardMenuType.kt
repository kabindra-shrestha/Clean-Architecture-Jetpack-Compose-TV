package com.kabindra.tv.iptv.utils.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

enum class DashboardMenuType(
    val title: String,
    val icon: ImageVector,
) {
    LiveTV(
        title = "LIVE TV",
        icon = Icons.Default.Tv
    ),
    Movie(
        title = "MOVIES",
        icon = Icons.Default.Movie
    ),
    Profile(
        title = "PROFILE",
        icon = Icons.Default.Person
    ),
}
