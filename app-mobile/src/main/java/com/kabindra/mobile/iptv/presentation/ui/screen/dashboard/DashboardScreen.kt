package com.kabindra.mobile.iptv.presentation.ui.screen.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileAdaptiveContent
import com.kabindra.mobile.iptv.presentation.ui.adaptive.plus
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileScreenHeader
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileSectionHeader
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileStatusCard
import com.kabindra.mobile.iptv.presentation.ui.component.notification.ForegroundAlertOverlay
import com.kabindra.mobile.iptv.utils.extensions.mainBackground
import com.kabindra.tv.iptv.domain.entity.AlertPayload
import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.DashboardEvent
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.DashboardUiState
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.DashboardViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.MediaCacheStatus
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    payload: AlertPayload?,
    onPayloadConsumed: () -> Unit,
    onNavigateLogin: () -> Unit,
    onNavigateLiveTV: () -> Unit,
    onNavigateMovie: () -> Unit,
    onNavigateProfile: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clockText by rememberDashboardClockText()

    LaunchedEffect(payload) {
        val safePayload = payload ?: return@LaunchedEffect
        if (safePayload.notifId.isBlank()) return@LaunchedEffect
        viewModel.updatePayloadAlert(safePayload)
        onPayloadConsumed()
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardEvent.observeService)
        viewModel.onEvent(DashboardEvent.observeLiveTVCache)
        viewModel.onEvent(DashboardEvent.observeMovieCache)
        viewModel.onEvent(DashboardEvent.prepareLiveTVCacheIfNeeded)
        viewModel.onEvent(DashboardEvent.prepareMovieCacheIfNeeded)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        MobileAdaptiveContent {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(it.dashboardCardMinSize),
                contentPadding = innerPadding.plus(
                    horizontal = it.horizontalPadding,
                    vertical = it.verticalPadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "header") {
                    MobileScreenHeader(
                        title = "Home",
                        subtitle = "Welcome back. Pick up live channels, movies, and account tools.",
                        action = {
                            AssistChip(
                                onClick = {},
                                label = { Text(clockText) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                    )
                                },
                            )
                        },
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }, key = "service") {
                    ServiceSummaryCard(uiState = uiState)
                }

                item(key = "live_tv") {
                    MobileStatusCard(
                        title = "Live TV",
                        body = uiState.liveTVStatusMessage,
                        metric = "${uiState.liveTVChannelCount} channels",
                        icon = Icons.Default.LiveTv,
                        isLoading = uiState.isLiveTVSyncing,
                        isError = uiState.liveTVCacheStatus == MediaCacheStatus.Error,
                        actionLabel = if (uiState.hasLiveTVData) "Sync" else "Set up",
                        onActionClick = {
                            if (uiState.hasLiveTVData) {
                                viewModel.syncLiveTVContent()
                            } else {
                                onNavigateLogin()
                            }
                        },
                        onClick = {
                            if (uiState.hasLiveTVData) onNavigateLiveTV() else onNavigateLogin()
                        },
                    )
                }

                item(key = "movies") {
                    MobileStatusCard(
                        title = "Movies",
                        body = uiState.movieStatusMessage,
                        metric = "${uiState.movieCount} titles",
                        icon = Icons.Default.LocalMovies,
                        isLoading = uiState.isMovieSyncing,
                        isError = uiState.movieCacheStatus == MediaCacheStatus.Error,
                        actionLabel = if (uiState.hasMovieData) "Sync" else "Set up",
                        onActionClick = {
                            if (uiState.hasMovieData) {
                                viewModel.syncMovieContent()
                            } else {
                                onNavigateLogin()
                            }
                        },
                        onClick = {
                            if (uiState.hasMovieData) onNavigateMovie() else onNavigateLogin()
                        },
                    )
                }

                item(key = "profile") {
                    MobileStatusCard(
                        title = "Profile",
                        body = "Account, connection status, and notification controls.",
                        metric = "${uiState.notifications.size} alerts",
                        icon = Icons.Default.Person,
                        onClick = onNavigateProfile,
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }, key = "notifications") {
                    NotificationsPreviewCard(
                        uiState = uiState,
                        onNavigateProfile = onNavigateProfile,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.showAlertDialog && uiState.activeAlert != null,
            enter = fadeIn() + slideInVertically { -it / 3 },
            exit = fadeOut() + slideOutVertically { -it / 3 },
        ) {
            uiState.activeAlert?.let { message ->
                ForegroundAlertOverlay(
                    message = message,
                    onDismiss = { viewModel.dismissActiveAlert() },
                )
            }
        }
    }
}

@Composable
private fun ServiceSummaryCard(uiState: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Platform status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = uiState.connectionState.label(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    label = "${uiState.liveTVCategoryCount} live categories",
                    icon = Icons.Default.LiveTv,
                )
                StatusPill(
                    label = "${uiState.movieCategoryCount} movie categories",
                    icon = Icons.Default.LocalMovies,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    icon: ImageVector,
) {
    AssistChip(
        onClick = {},
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

@Composable
private fun NotificationsPreviewCard(
    uiState: DashboardUiState,
    onNavigateProfile: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MobileSectionHeader(
                title = "Recent alerts",
                subtitle = if (uiState.notifications.isEmpty()) "No recent alerts" else "${uiState.notifications.size} received",
                actionLabel = "Profile",
                onActionClick = onNavigateProfile,
            )

            if (uiState.notifications.isEmpty()) {
                Text(
                    text = "System alerts and foreground messages will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                uiState.notifications.take(3).forEach { notification ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = notification.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun ConnectionState.label(): String {
    return when (this) {
        ConnectionState.Connected -> "Notification stream connected"
        ConnectionState.Connecting -> "Connecting to notification stream"
        ConnectionState.Disconnected -> "Notification stream disconnected"
        is ConnectionState.Error -> message.ifBlank { "Notification stream error" }
        is ConnectionState.Reconnecting -> "Reconnecting notification stream, attempt $attempt"
    }
}

@Composable
private fun rememberDashboardClockText() = produceState(
    initialValue = currentDashboardTime()
) {
    while (true) {
        value = currentDashboardTime()
        delay(1_000)
    }
}

private fun currentDashboardTime(): String {
    return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
}
