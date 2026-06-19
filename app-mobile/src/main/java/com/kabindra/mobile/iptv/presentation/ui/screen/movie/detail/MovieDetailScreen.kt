package com.kabindra.mobile.iptv.presentation.ui.screen.movie.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileAdaptiveContent
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileWindowSizeClass
import com.kabindra.mobile.iptv.presentation.ui.adaptive.plus
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileErrorState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileLoadingState
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobilePosterCard
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileSectionHeader
import com.kabindra.mobile.iptv.utils.extensions.mainBackground
import com.kabindra.tv.iptv.domain.entity.VODDetail
import com.kabindra.tv.iptv.domain.entity.VODSummary
import com.kabindra.tv.iptv.presentation.ui.screen.movie.detail.MovieDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    movieId: String,
    onBack: () -> Unit,
    onNavigateMoviePlayer: (String) -> Unit,
    onNavigateMovieDetail: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    BackHandler(onBack = onBack)

    LaunchedEffect(movieId) {
        viewModel.getMovieDetail(movieId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        when {
            state.isLoading && state.movie == null -> {
                MobileLoadingState(message = "Loading movie details...")
            }

            state.errorMessage.isNotBlank() && state.movie == null -> {
                MobileErrorState(
                    message = state.errorMessage,
                    actionLabel = "Back",
                    onActionClick = onBack,
                )
            }

            state.movie != null -> {
                val movie = state.movie ?: return@Box
                val recommendations = state.recommendedMovies.ifEmpty { movie.alsoWatch }

                MobileAdaptiveContent {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = innerPadding.plus(
                            horizontal = it.horizontalPadding,
                            vertical = it.verticalPadding,
                        ),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        item(key = "hero") {
                            MovieHero(
                                movie = movie,
                                compact = it.widthClass == MobileWindowSizeClass.Compact,
                                onBack = onBack,
                                onPlay = { onNavigateMoviePlayer(movie.id) },
                            )
                        }

                        if (recommendations.isNotEmpty()) {
                            item(key = "also_watch_header") {
                                MobileSectionHeader(
                                    title = "Also watch",
                                    subtitle = "More titles from your library",
                                )
                            }
                            item(key = "also_watch_rail") {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(
                                        items = recommendations,
                                        key = VODSummary::id,
                                    ) { item ->
                                        MobilePosterCard(
                                            title = item.title,
                                            subtitle = item.subtitle,
                                            posterUrl = item.posterUrl,
                                            modifier = Modifier.width(148.dp),
                                            onClick = { onNavigateMovieDetail(item.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieHero(
    movie: VODDetail,
    compact: Boolean,
    onBack: () -> Unit,
    onPlay: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = movie.backdropUrl.ifBlank { movie.posterUrl },
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .blur(28.dp),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.12f),
                                Color.Black.copy(alpha = 0.68f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            )
                        )
                    )
            )

            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }

            if (compact) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, top = 58.dp, end = 18.dp, bottom = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PosterThumbnail(
                        movie = movie,
                        modifier = Modifier.width(168.dp),
                    )
                    MovieTitleBlock(movie = movie, centered = true)
                    PlayButton(onClick = onPlay)
                    MovieDescription(movie = movie, centered = true)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 26.dp, top = 72.dp, end = 26.dp, bottom = 26.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PosterThumbnail(
                        movie = movie,
                        modifier = Modifier.width(210.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        MovieTitleBlock(movie = movie, centered = false)
                        MovieDescription(movie = movie, centered = false)
                        PlayButton(onClick = onPlay)
                    }
                }
            }
        }
    }
}

@Composable
private fun PosterThumbnail(
    movie: VODDetail,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = movie.posterUrl.ifBlank { movie.backdropUrl },
        contentDescription = movie.title,
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun MovieTitleBlock(
    movie: VODDetail,
    centered: Boolean,
) {
    Column(
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        )
        movie.subtitle.takeIf(String::isNotBlank)?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            )
        }
    }
}

@Composable
private fun MovieDescription(
    movie: VODDetail,
    centered: Boolean,
) {
    Text(
        text = movie.description.ifBlank { "No description available." },
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.86f),
        maxLines = 8,
        overflow = TextOverflow.Ellipsis,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
    )
}

@Composable
private fun PlayButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Text("Play", modifier = Modifier.padding(start = 8.dp))
    }
}
