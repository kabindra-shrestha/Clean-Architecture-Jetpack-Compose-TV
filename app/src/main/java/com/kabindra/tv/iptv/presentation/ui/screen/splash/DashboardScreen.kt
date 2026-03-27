package com.kabindra.tv.iptv.presentation.ui.screen.splash

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kabindra.tv.iptv.utils.Connectivity

@Composable
fun DashboardScreen(
    innerPadding: PaddingValues,
    onNavigateLogin: () -> Unit,
) {
    val connectivity = remember { Connectivity() }
    val isConnected by connectivity.isConnectedState.collectAsState()

}
