package com.kabindra.mobile.iptv

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabindra.tv.iptv.domain.entity.LoginCredentials
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.DashboardEvent
import com.kabindra.tv.iptv.presentation.ui.screen.dashboard.DashboardViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.livetv.player.LiveTVPlayerViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.login.LoginEvent
import com.kabindra.tv.iptv.presentation.ui.screen.login.LoginViewModel
import com.kabindra.tv.iptv.presentation.ui.screen.movie.content.MovieContentViewModel
import org.koin.compose.viewmodel.koinViewModel

private enum class MobileDestination(
    val label: String,
    val icon: ImageVector,
) {
    Login("Login", Icons.Filled.Person),
    Dashboard("Dashboard", Icons.Filled.Home),
    LiveTv("Live TV", Icons.Filled.LiveTv),
    Movies("Movies", Icons.Filled.LocalMovies),
}

@Composable
fun MobileApp() {
    var destination by rememberSaveable { mutableStateOf(MobileDestination.Login) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = { MobileTopBar(destination.label) },
                bottomBar = {
                    NavigationBar {
                        MobileDestination.entries.forEach { item ->
                            NavigationBarItem(
                                selected = destination == item,
                                onClick = { destination = item },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when (destination) {
                    MobileDestination.Login -> MobileLoginScreen(
                        innerPadding = innerPadding,
                        onLoggedIn = { destination = MobileDestination.Dashboard },
                    )

                    MobileDestination.Dashboard -> MobileDashboardScreen(innerPadding)
                    MobileDestination.LiveTv -> MobileLiveTvScreen(innerPadding)
                    MobileDestination.Movies -> MobileMoviesScreen(innerPadding)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileTopBar(title: String) {
    TopAppBar(title = { Text(title) })
}

@Composable
private fun MobileLoginScreen(
    innerPadding: PaddingValues,
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.loginState.collectAsStateWithLifecycle()
    var serverName by rememberSaveable { mutableStateOf("tv.quierover.xyz") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onEvent(LoginEvent.GetUser)
    }

    LaunchedEffect(state.isLogged) {
        if (state.isLogged == true) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Account", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = serverName,
            onValueChange = { serverName = it },
            label = { Text("Server") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            enabled = !state.isLoading && serverName.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
            onClick = {
                viewModel.onEvent(
                    LoginEvent.GetLogin(
                        LoginCredentials(
                            serverName = serverName,
                            username = username,
                            password = password,
                        )
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save")
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        if (state.errorMessage.isNotBlank()) {
            Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun MobileDashboardScreen(
    innerPadding: PaddingValues,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardEvent.observeService)
        viewModel.onEvent(DashboardEvent.observeLiveTVCache)
        viewModel.onEvent(DashboardEvent.observeMovieCache)
        viewModel.onEvent(DashboardEvent.prepareLiveTVCacheIfNeeded)
        viewModel.onEvent(DashboardEvent.prepareMovieCacheIfNeeded)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Content status", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            MobileStatusCard(
                title = "Live TV",
                message = state.liveTVStatusMessage,
                details = "${state.liveTVCategoryCount} categories, ${state.liveTVChannelCount} channels",
                onRefresh = viewModel::syncLiveTVContent,
            )
        }
        item {
            MobileStatusCard(
                title = "Movies",
                message = state.movieStatusMessage,
                details = "${state.movieCategoryCount} categories, ${state.movieCount} movies",
                onRefresh = viewModel::syncMovieContent,
            )
        }
    }
}

@Composable
private fun MobileLiveTvScreen(
    innerPadding: PaddingValues,
    viewModel: LiveTVPlayerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }
        ?: state.categories.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Live TV", style = MaterialTheme.typography.headlineSmall)
        }
        if (state.isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        if (state.errorMessage.isNotBlank()) {
            item {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
        items(state.categories, key = { it.id }) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectCategory(category.id) }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(category.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("${category.channels.size} channels")
                }
            }
        }
        selectedCategory?.let { category ->
            item {
                Text("Channels", style = MaterialTheme.typography.titleLarge)
            }
            items(category.channels, key = { it.id }) { channel ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectChannel(channel.id) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(channel.title, style = MaterialTheme.typography.titleMedium)
                        if (channel.id == state.selectedChannelId) {
                            Text("Selected", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileMoviesScreen(
    innerPadding: PaddingValues,
    viewModel: MovieContentViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }
        ?: state.categories.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Movies", style = MaterialTheme.typography.headlineSmall)
        }
        if (state.isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        if (state.errorMessage.isNotBlank()) {
            item {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
        items(state.categories, key = { it.id }) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectCategory(category.id) }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(category.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("${category.movies.size} movies")
                }
            }
        }
        selectedCategory?.let { category ->
            item {
                Text("Titles", style = MaterialTheme.typography.titleLarge)
            }
            items(category.movies, key = { it.id }) { movie ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(movie.title, style = MaterialTheme.typography.titleMedium)
                        if (movie.subtitle.isNotBlank()) {
                            Text(movie.subtitle, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileStatusCard(
    title: String,
    message: String,
    details: String,
    onRefresh: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Button(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
            Text(message)
            Text(details, style = MaterialTheme.typography.bodySmall)
        }
    }
}
