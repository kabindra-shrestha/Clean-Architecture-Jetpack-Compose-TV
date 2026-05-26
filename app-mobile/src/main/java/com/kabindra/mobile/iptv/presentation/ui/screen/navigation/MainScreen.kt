package com.kabindra.mobile.iptv.presentation.ui.screen.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileAdaptiveContent
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileNavigationType
import com.kabindra.mobile.iptv.presentation.ui.screen.dashboard.DashboardScreen
import com.kabindra.mobile.iptv.presentation.ui.screen.livetv.player.LiveTVPlayerScreen
import com.kabindra.mobile.iptv.presentation.ui.screen.login.LoginScreen
import com.kabindra.mobile.iptv.presentation.ui.screen.movie.content.MovieContentScreen
import com.kabindra.mobile.iptv.presentation.ui.screen.movie.detail.MovieDetailScreen
import com.kabindra.mobile.iptv.presentation.ui.screen.movie.player.MoviePlayerScreen
import com.kabindra.mobile.iptv.presentation.ui.screen.profile.ProfileScreen
import com.kabindra.mobile.iptv.presentation.ui.screen.splash.SplashScreen
import com.kabindra.tv.iptv.domain.entity.AlertPayload
import org.koin.compose.koinInject

private enum class MobileTopLevelDestination(
    val label: String,
    val route: NavKey,
    val icon: ImageVector,
) {
    Home("Home", DashboardRoute, Icons.Default.Home),
    Live("Live", LiveTVPlayerRoute, Icons.Default.LiveTv),
    Movies("Movies", MovieRoute, Icons.Default.LocalMovies),
    Profile("Profile", ProfileRoute, Icons.Default.Person),
}

@Composable
fun MainScreen(
    payload: AlertPayload?,
    onPayloadConsumed: () -> Unit,
) {
    val snackBarHostState: SnackbarHostState = koinInject()
    val backStack = rememberNavBackStack(SplashRoute)
    var isPlayerImmersive by remember { mutableStateOf(false) }
    val currentRoute = backStack.lastOrNull()
    val selectedDestination = currentRoute.toTopLevelDestination()
    val showNavigation = selectedDestination != null &&
            currentRoute !is MoviePlayerRoute &&
            !isPlayerImmersive

    MobileAdaptiveContent {
        if (showNavigation && it.navigationType == MobileNavigationType.NavigationRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                MobileNavigationRail(
                    selectedDestination = selectedDestination,
                    onDestinationSelected = { destination ->
                        backStack.navigateTopLevel(destination.route)
                    },
                )
                MainNavHostScaffold(
                    modifier = Modifier.weight(1f),
                    snackBarHostState = snackBarHostState,
                    backStack = backStack,
                    payload = payload,
                    onPayloadConsumed = onPayloadConsumed,
                    onPlayerImmersiveChanged = { isPlayerImmersive = it },
                )
            }
        } else {
            MainNavHostScaffold(
                snackBarHostState = snackBarHostState,
                backStack = backStack,
                payload = payload,
                onPayloadConsumed = onPayloadConsumed,
                onPlayerImmersiveChanged = { isPlayerImmersive = it },
                bottomBar = {
                    if (showNavigation) {
                        MobileNavigationBar(
                            selectedDestination = selectedDestination,
                            onDestinationSelected = { destination ->
                                backStack.navigateTopLevel(destination.route)
                            },
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun MainNavHostScaffold(
    snackBarHostState: SnackbarHostState,
    backStack: NavBackStack<NavKey>,
    payload: AlertPayload?,
    onPayloadConsumed: () -> Unit,
    onPlayerImmersiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        bottomBar = bottomBar,
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onBack = {
                when {
                    backStack.size > 1 -> backStack.removeLastOrNull()
                    backStack.lastOrNull() !is DashboardRoute -> backStack.navigateTopLevel(
                        DashboardRoute
                    )
                }
            },
            entryProvider = entryProvider {
                entry<SplashRoute> {
                    PlayerImmersiveEffect(false, onPlayerImmersiveChanged)
                    SplashScreen(
                        innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                        onNavigateDashboard = {
                            backStack.navigateTopLevel(DashboardRoute)
                        }
                    )
                }
                entry<LoginRoute> {
                    PlayerImmersiveEffect(false, onPlayerImmersiveChanged)
                    LoginScreen(
                        innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                        onNavigateDashboard = {
                            backStack.navigateTopLevel(DashboardRoute)
                        }
                    )
                }
                entry<DashboardRoute> {
                    PlayerImmersiveEffect(false, onPlayerImmersiveChanged)
                    DashboardScreen(
                        innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                        payload = payload,
                        onPayloadConsumed = onPayloadConsumed,
                        onNavigateLogin = { backStack.add(LoginRoute) },
                        onNavigateLiveTV = { backStack.navigateTopLevel(LiveTVPlayerRoute) },
                        onNavigateMovie = { backStack.navigateTopLevel(MovieRoute) },
                        onNavigateProfile = { backStack.navigateTopLevel(ProfileRoute) },
                    )
                }
                entry<LiveTVPlayerRoute> {
                    LiveTVPlayerScreen(
                        innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                        onBack = {
                            backStack.navigateTopLevel(DashboardRoute)
                        },
                        onImmersiveChanged = onPlayerImmersiveChanged,
                    )
                }
                entry<MovieRoute> {
                    PlayerImmersiveEffect(false, onPlayerImmersiveChanged)
                    MovieContentScreen(
                        innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                        onNavigateMovieDetail = { movieId ->
                            backStack.add(MovieDetailRoute(movieId))
                        }
                    )
                }
                entry<ProfileRoute> {
                    PlayerImmersiveEffect(false, onPlayerImmersiveChanged)
                    ProfileScreen(
                        innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                        onNavigateLogin = { backStack.add(LoginRoute) },
                    )
                }
                entry<MovieDetailRoute> { route ->
                    PlayerImmersiveEffect(false, onPlayerImmersiveChanged)
                    MovieDetailScreen(
                        movieId = route.movieId,
                        innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateMoviePlayer = { movieId ->
                            backStack.add(MoviePlayerRoute(movieId))
                        },
                        onNavigateMovieDetail = { movieId ->
                            backStack.removeLastOrNull()
                            backStack.add(MovieDetailRoute(movieId))
                        }
                    )
                }
                entry<MoviePlayerRoute> { route ->
                    MoviePlayerScreen(
                        movieId = route.movieId,
                        innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                        onBack = { backStack.removeLastOrNull() },
                        onImmersiveChanged = onPlayerImmersiveChanged,
                    )
                }
            },
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(260)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(260)
                )
            },
            popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(220)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(220)
                )
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(220)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(220)
                )
            }
        )
    }
}

@Composable
private fun PlayerImmersiveEffect(
    isImmersive: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(isImmersive) {
        onChanged(isImmersive)
    }
}

@Composable
private fun MobileNavigationBar(
    selectedDestination: MobileTopLevelDestination?,
    onDestinationSelected: (MobileTopLevelDestination) -> Unit,
) {
    NavigationBar {
        MobileTopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}

@Composable
private fun MobileNavigationRail(
    selectedDestination: MobileTopLevelDestination?,
    onDestinationSelected: (MobileTopLevelDestination) -> Unit,
) {
    NavigationRail {
        MobileTopLevelDestination.entries.forEach { destination ->
            NavigationRailItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}

private fun NavKey?.toTopLevelDestination(): MobileTopLevelDestination? {
    return when (this) {
        DashboardRoute -> MobileTopLevelDestination.Home
        LiveTVPlayerRoute -> MobileTopLevelDestination.Live
        MovieRoute -> MobileTopLevelDestination.Movies
        ProfileRoute -> MobileTopLevelDestination.Profile
        else -> null
    }
}

private fun NavBackStack<NavKey>.navigateTopLevel(route: NavKey) {
    clear()
    add(route)
}
