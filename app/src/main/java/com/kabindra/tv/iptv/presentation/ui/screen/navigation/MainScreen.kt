package com.kabindra.tv.iptv.presentation.ui.screen.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kabindra.tv.iptv.presentation.ui.screen.livetv.LiveTvPlayerScreen
import com.kabindra.tv.iptv.presentation.ui.screen.movies.MovieDetailScreen
import com.kabindra.tv.iptv.presentation.ui.screen.movies.MoviePlayerScreen
import com.kabindra.tv.iptv.presentation.ui.screen.movies.MoviesScreen
import com.kabindra.tv.iptv.presentation.ui.screen.splash.DashboardScreen
import com.kabindra.tv.iptv.presentation.ui.screen.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun MainScreen() {
    val snackBarHostState: SnackbarHostState = koinInject()

    val backStack = rememberNavBackStack(SplashRoute)

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }) { innerPadding ->

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<SplashRoute> {
                    SplashScreen(
                        innerPadding = innerPadding,
                        onNavigateLogin = {
                        },
                        onNavigateDashboard = {
                            backStack.add(DashboardRoute)
                        }
                    )
                }
                entry<DashboardRoute>(
                    metadata = NavDisplay.transitionSpec {
                        // Slide new content up, keeping the old content in place underneath
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(1000)
                        ) togetherWith ExitTransition.KeepUntilTransitionsFinished
                    } + NavDisplay.popTransitionSpec {
                        // Slide old content down, revealing the new content in place underneath
                        EnterTransition.None togetherWith
                                slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(1000)
                                )
                    } + NavDisplay.predictivePopTransitionSpec {
                        // Slide old content down, revealing the new content in place underneath
                        EnterTransition.None togetherWith
                                slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(1000)
                                )
                    }
                ) {
                    DashboardScreen(
                        innerPadding = innerPadding,
                        onNavigateLogin = {
                        },
                        onNavigateLiveTv = {
                            backStack.add(LiveTvPlayerRoute)
                        },
                        onNavigateMovies = {
                            backStack.add(MoviesRoute)
                        }
                    )
                }
                entry<LiveTvPlayerRoute> {
                    LiveTvPlayerScreen(
                        innerPadding = innerPadding,
                        onBack = {
                            backStack.removeLastOrNull()
                        }
                    )
                }
                entry<MoviesRoute> {
                    MoviesScreen(
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
                // Slide in from right when navigating forward
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(1000)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(1000)
                )
            },
            popTransitionSpec = {
                // Slide in from left when navigating back
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(1000)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(1000)
                )
            },
            predictivePopTransitionSpec = {
                // Slide in from left when navigating back
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(1000)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(1000)
                )
            }
        )
    }
}
