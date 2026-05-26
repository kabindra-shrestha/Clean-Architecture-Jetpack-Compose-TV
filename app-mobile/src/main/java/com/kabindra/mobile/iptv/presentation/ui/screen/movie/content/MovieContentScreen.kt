package com.kabindra.mobile.iptv.presentation.ui.screen.movie.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileAdaptiveContent
import com.kabindra.mobile.iptv.presentation.ui.adaptive.plus
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileCategoryChips
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileEmptyState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileErrorState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileLoadingState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobilePosterCard
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileScreenHeader
import com.kabindra.mobile.iptv.utils.extensions.mainBackground
import com.kabindra.tv.iptv.presentation.ui.screen.movie.content.MovieContentViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MovieContentScreen(
    viewModel: MovieContentViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    onNavigateMovieDetail: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }
        ?: state.categories.firstOrNull()
    val movies = selectedCategory?.movies.orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        MobileAdaptiveContent {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(it.gridMinCellSize),
                contentPadding = innerPadding.plus(
                    horizontal = it.horizontalPadding,
                    vertical = it.verticalPadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "header") {
                    MobileScreenHeader(
                        title = "Movies",
                        subtitle = "Browse cached VOD titles with phone, tablet, and foldable friendly grids.",
                    )
                }

                if (state.categories.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "categories") {
                        MobileCategoryChips(
                            items = state.categories,
                            selectedItem = selectedCategory,
                            label = { category -> category.title },
                            key = { category -> category.id },
                            onSelected = { category -> viewModel.selectCategory(category.id) },
                        )
                    }
                }

                when {
                    state.isLoading -> {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "loading") {
                            MobileLoadingState(
                                message = "Loading movies...",
                                modifier = Modifier.height(320.dp),
                            )
                        }
                    }

                    state.errorMessage.isNotBlank() -> {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "error") {
                            MobileErrorState(
                                message = state.errorMessage,
                                modifier = Modifier.height(320.dp),
                                onActionClick = viewModel::observeMovieContent,
                            )
                        }
                    }

                    state.isEmpty || movies.isEmpty() -> {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                            MobileEmptyState(
                                message = "Movie data is being prepared.",
                                modifier = Modifier.height(320.dp),
                            )
                        }
                    }

                    else -> {
                        items(
                            count = movies.size,
                            key = { index -> movies[index].id },
                            contentType = { "movie_poster" },
                        ) { index ->
                            val movie = movies[index]
                            MobilePosterCard(
                                title = movie.title,
                                subtitle = movie.subtitle,
                                posterUrl = movie.posterUrl,
                                onClick = { onNavigateMovieDetail(movie.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
