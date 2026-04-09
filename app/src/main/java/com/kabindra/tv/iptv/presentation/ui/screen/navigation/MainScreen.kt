package com.kabindra.tv.iptv.presentation.ui.screen.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kabindra.tv.iptv.MainActivity
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.DashboardScreen
import com.kabindra.tv.iptv.presentation.ui.screen.livetv.player.LiveTVPlayerScreen
import com.kabindra.tv.iptv.presentation.ui.screen.movie.content.MovieScreen
import com.kabindra.tv.iptv.presentation.ui.screen.movie.detail.MovieDetailScreen
import com.kabindra.tv.iptv.presentation.ui.screen.movie.player.MoviePlayerScreen
import com.kabindra.tv.iptv.presentation.ui.screen.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun MainScreen(payload: MainActivity.AlertPayload?) {
    val snackBarHostState: SnackbarHostState = koinInject()

    val backStack = rememberNavBackStack(SplashRoute)

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }) { innerPadding ->

        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            entryProvider = entryProvider {
                entry<SplashRoute> {
                    SplashScreen(
                        innerPadding = innerPadding,
                        onNavigateDashboard = {
                            backStack.clear()
                            backStack.add(DashboardRoute)
                        }
                    )
                }
                entry<DashboardRoute> {
                    DashboardScreen(
                        innerPadding = innerPadding,
                        payload = payload,
                        onNavigateLiveTV = {
                            backStack.add(LiveTVPlayerRoute)
                        },
                        onNavigateMovie = {
                            backStack.add(MovieRoute)
                        }
                    )
                }
                entry<LiveTVPlayerRoute> {
                    LiveTVPlayerScreen(
                        innerPadding = innerPadding,
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                entry<MovieRoute> {
                    MovieScreen(
                        innerPadding = innerPadding,
                        onNavigateMovieDetail = { movieId ->
                            backStack.add(MovieDetailRoute(movieId))
                        }
                    )
                }
                entry<MovieDetailRoute> { route ->
                    MovieDetailScreen(
                        movieId = route.movieId,
                        innerPadding = innerPadding,
                        onBack = {
                            backStack.removeLastOrNull()
                        },
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
                        innerPadding = innerPadding,
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
            },
            transitionSpec = {
                EnterTransition.None togetherWith ExitTransition.None
            },
            popTransitionSpec = {
                EnterTransition.None togetherWith ExitTransition.None
            },
            predictivePopTransitionSpec = {
                EnterTransition.None togetherWith ExitTransition.None
            }
        )
    }
}
