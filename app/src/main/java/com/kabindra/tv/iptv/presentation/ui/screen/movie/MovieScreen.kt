package com.kabindra.tv.iptv.presentation.ui.screen.movie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazy
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyLayout
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyOrientation
import com.kabindra.tv.iptv.presentation.ui.component.BaseLazyPlatform
import com.kabindra.tv.iptv.presentation.ui.component.ButtonComponent
import com.kabindra.tv.iptv.presentation.ui.component.LoadingIndicator
import com.kabindra.tv.iptv.presentation.ui.component.PosterCardComponent
import com.kabindra.tv.iptv.presentation.ui.component.TabsComponent
import com.kabindra.tv.iptv.presentation.ui.component.TabsIndicatorStyle
import com.kabindra.tv.iptv.presentation.ui.component.TextComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextSize
import com.kabindra.tv.iptv.presentation.ui.component.TextType
import com.kabindra.tv.iptv.presentation.ui.component.TvLazyConfig
import com.kabindra.tv.iptv.presentation.ui.component.rememberBaseLazyState
import com.kabindra.tv.iptv.presentation.viewmodel.media.MovieViewModel
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

private object MovieScreenTokens {
    const val horizontalPadding = 32
    const val topPadding = 26
    const val headerSpacing = 18
    const val gridTopPadding = 18
    const val gridBottomPadding = 28
    const val gridSpacing = 16
    const val gridSpanCount = 5
}

@Composable
fun MovieScreen(
    innerPadding: PaddingValues,
    onNavigateMovieDetail: (String) -> Unit,
    viewModel: MovieViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val selectedCategoryIndex = state.categories.indexOfFirst { it.id == state.selectedCategoryId }
        .takeIf { it >= 0 }
        ?: 0
    val selectedCategory = state.categories.getOrNull(selectedCategoryIndex)
    val gridState = key(state.selectedCategoryId) { rememberBaseLazyState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .moviesBackground()
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(
                    start = MovieScreenTokens.horizontalPadding.sdp,
                    end = MovieScreenTokens.horizontalPadding.sdp,
                    top = MovieScreenTokens.topPadding.sdp
                ),
                verticalArrangement = Arrangement.spacedBy(MovieScreenTokens.headerSpacing.sdp)
            ) {
                TextComponent(
                    text = "Movie",
                    type = TextType.Display,
                    size = TextSize.Small,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (state.categories.isNotEmpty()) {
                    TabsComponent(
                        tabs = state.categories.map { it.title },
                        selectedIndex = selectedCategoryIndex,
                        indicatorStyle = TabsIndicatorStyle.Underlined,
                        onSelectedIndexChange = { index ->
                            state.categories.getOrNull(index)?.let { category ->
                                viewModel.selectCategory(category.id)
                            }
                        }
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(
                            isCircular = true,
                            useExpressive = true
                        )
                    }
                }

                !state.errorMessage.isNullOrBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.sdp)
                        ) {
                            TextComponent(
                                text = state.errorMessage ?: "Unable to load movies.",
                                type = TextType.Body,
                                size = TextSize.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            ButtonComponent(
                                text = "Retry",
                                onClick = viewModel::loadContent
                            )
                        }
                    }
                }

                else -> {
                    BaseLazy(
                        items = selectedCategory?.movies.orEmpty(),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        layout = BaseLazyLayout.Grid,
                        orientation = BaseLazyOrientation.Vertical,
                        platform = BaseLazyPlatform.AndroidTv,
                        tvConfig = TvLazyConfig(
                            initialFocusedIndex = 0,
                            autoScrollOnFocus = true,
                        ),
                        contentPadding = PaddingValues(
                            start = MovieScreenTokens.horizontalPadding.sdp,
                            top = MovieScreenTokens.gridTopPadding.sdp,
                            end = MovieScreenTokens.horizontalPadding.sdp,
                            bottom = MovieScreenTokens.gridBottomPadding.sdp
                        ),
                        arrangement = Arrangement.spacedBy(MovieScreenTokens.gridSpacing.sdp),
                        userScrollEnabled = true,
                        spanCount = MovieScreenTokens.gridSpanCount,
                        key = { _, item -> item.id },
                        contentType = { _, _ -> "movie_poster" }
                    ) { _, item, itemModifier ->
                        PosterCardComponent(
                            modifier = itemModifier,
                            title = item.title,
                            subtitle = item.subtitle,
                            posterUrl = item.posterUrl,
                            onClick = { onNavigateMovieDetail(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.moviesBackground(): Modifier {
    return background(
        brush = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.background
            )
        )
    ).background(
        brush = Brush.radialGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
                Color.Transparent
            )
        )
    )
}
