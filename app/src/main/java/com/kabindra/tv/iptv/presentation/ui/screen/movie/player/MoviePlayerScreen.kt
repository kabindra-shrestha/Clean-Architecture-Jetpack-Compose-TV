package com.kabindra.tv.iptv.presentation.ui.screen.movie.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import com.kabindra.player.component.Media3PlayerComponent
import com.kabindra.player.model.PlayerMediaItem
import com.kabindra.player.model.PlayerStreamType
import com.kabindra.tv.iptv.domain.entity.MediaStreamType
import com.kabindra.tv.iptv.domain.entity.MovieDetail
import com.kabindra.tv.iptv.presentation.ui.component.ButtonComponent
import com.kabindra.tv.iptv.presentation.ui.component.LoadingIndicator
import com.kabindra.tv.iptv.presentation.ui.component.TextComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextSize
import com.kabindra.tv.iptv.presentation.ui.component.TextType
import com.kabindra.tv.iptv.utils.extensions.mainBackground
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MoviePlayerScreen(
    viewModel: MoviePlayerViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    movieId: String,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    BackHandler(onBack = onBack)

    LaunchedEffect(movieId) {
        viewModel.loadMovie(movieId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        state.movie?.let { movie ->
            Media3PlayerComponent(
                items = listOf(movie.toPlayerMediaItem()),
                selectedIndex = 0,
                onSelectedIndexChange = { },
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.88f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 22.sdp, vertical = 18.sdp)
            ) {
                TextComponent(
                    text = movie.title,
                    type = TextType.Title,
                    size = TextSize.Large,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextComponent(
                    text = movie.subtitle,
                    type = TextType.Body,
                    size = TextSize.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f)
                )
            }
        }

        when {
            state.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    isCircular = true,
                    useExpressive = true
                )
            }

            state.errorMessage.isNotBlank() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextComponent(
                        text = state.errorMessage,
                        type = TextType.Body,
                        size = TextSize.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ButtonComponent(
                        modifier = Modifier.padding(top = 10.sdp),
                        text = "Back",
                        onClick = onBack
                    )
                }
            }
        }
    }
}

private fun MovieDetail.toPlayerMediaItem(): PlayerMediaItem {
    return PlayerMediaItem(
        id = id,
        title = title,
        streamUrl = streamUrl,
        streamType = streamType.toPlayerStreamType(),
        posterUrl = posterUrl
    )
}

private fun MediaStreamType.toPlayerStreamType(): PlayerStreamType {
    return when (this) {
        MediaStreamType.Hls -> PlayerStreamType.Hls
        MediaStreamType.Progressive -> PlayerStreamType.Progressive
    }
}
