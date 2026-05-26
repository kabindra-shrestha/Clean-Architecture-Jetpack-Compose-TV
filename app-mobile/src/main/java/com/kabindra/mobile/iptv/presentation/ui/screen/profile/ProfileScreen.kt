package com.kabindra.mobile.iptv.presentation.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabindra.mobile.iptv.presentation.ui.adaptive.MobileAdaptiveContent
import com.kabindra.mobile.iptv.presentation.ui.adaptive.plus
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileScreenHeader
import com.kabindra.mobile.iptv.presentation.ui.component.mobile.MobileStatusCard
import com.kabindra.mobile.iptv.utils.extensions.mainBackground
import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    onNavigateLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MobileAdaptiveContent(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding.plus(
                horizontal = it.horizontalPadding,
                vertical = it.verticalPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                MobileScreenHeader(
                    title = "Profile",
                    subtitle = "Manage account access, sync state, and notification history.",
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Guest profile",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Sign in again to refresh IPTV credentials or switch accounts.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        Button(onClick = onNavigateLogin) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                            Text("Account setup", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            item {
                MobileStatusCard(
                    title = "Notification stream",
                    body = uiState.connectionState.profileLabel(),
                    metric = "${uiState.notifications.size} stored alerts",
                    icon = Icons.Default.CloudDone,
                )
            }

            item {
                MobileStatusCard(
                    title = "Live TV cache",
                    body = uiState.liveTVStatusMessage,
                    metric = "${uiState.liveTVCategoryCount} categories, ${uiState.liveTVChannelCount} channels",
                    icon = Icons.Default.LiveTv,
                    isLoading = uiState.isLiveTVSyncing,
                    actionLabel = "Sync",
                    onActionClick = viewModel::syncLiveTVContent,
                )
            }

            item {
                MobileStatusCard(
                    title = "Movie cache",
                    body = uiState.movieStatusMessage,
                    metric = "${uiState.movieCategoryCount} categories, ${uiState.movieCount} movies",
                    icon = Icons.Default.LocalMovies,
                    isLoading = uiState.isMovieSyncing,
                    actionLabel = "Sync",
                    onActionClick = viewModel::syncMovieContent,
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.Notifications, contentDescription = null)
                            },
                            headlineContent = {
                                Text(
                                    text = "Recent alerts",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            supportingContent = {
                                Text("${uiState.notifications.size} notifications in memory")
                            },
                        )
                        OutlinedButton(
                            onClick = viewModel::clearNotifications,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                            enabled = uiState.notifications.isNotEmpty(),
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            Text("Clear alerts", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun ConnectionState.profileLabel(): String {
    return when (this) {
        ConnectionState.Connected -> "Connected and ready to receive foreground alerts."
        ConnectionState.Connecting -> "Connecting to the notification service."
        ConnectionState.Disconnected -> "Disconnected. Alerts may arrive when the service reconnects."
        is ConnectionState.Error -> message.ifBlank { "The notification service reported an error." }
        is ConnectionState.Reconnecting -> "Reconnecting, attempt $attempt."
    }
}
