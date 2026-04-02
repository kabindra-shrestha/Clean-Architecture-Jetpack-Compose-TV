package com.kabindra.tv.iptv.presentation.ui.screen.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.MaterialTheme
import com.kabindra.tv.iptv.presentation.ui.component.AppIcon
import com.kabindra.tv.iptv.presentation.ui.component.LoadingIndicator
import com.kabindra.tv.iptv.presentation.ui.component.TextComponent
import com.kabindra.tv.iptv.utils.Connectivity
import com.kabindra.tv.iptv.utils.error.GlobalErrorDialog
import com.kabindra.tv.iptv.utils.extensions.mainBackground
import com.kabindra.tv.iptv.utils.getPlatform
import com.kabindra.tv.iptv.utils.success.GlobalSuccessDialog
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    onNavigateDashboard: () -> Unit
) {

    val connectivity = remember { Connectivity() }
    val isConnected by connectivity.isConnectedState.collectAsState()
    val splashState by splashViewModel.splashState.collectAsStateWithLifecycle()

    // Use DisposableEffect to reset states when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            // Reset the relevant states
            splashViewModel.resetStates()
        }
    }

    println("isConnected: $isConnected")
    if (!isConnected) {
        GlobalErrorDialog(
            isVisible = true,
            statusCode = -1,
            title = "No Network Connection",
            message = "Please check you internet connection.\nPlease try again.",
            onDismiss = {
                splashViewModel.resetStates()
            },
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
            .padding(innerPadding)
    ) {
        AppIcon(
            modifier = Modifier
                .width(120.sdp)
                .height(120.sdp)
                .align(Alignment.Center)
        )

        if (!splashState.isLoading) {
            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 75.sdp),
                isCircular = true,
                useExpressive = true
            )
        }

        TextComponent(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = (-25).sdp),
            text = "Version: ${getPlatform().appVersion}",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        /*AppBrandIcon(
            modifier = Modifier.wrapContentWidth().wrapContentHeight().align(Alignment.BottomCenter)
        )*/
    }

    if (splashState.isLogged == true) {
        splashViewModel.onEvent(SplashEvent.GetUser)
    } else {
        onNavigateDashboard()
    }

    if (splashState.isSuccess) {
        GlobalSuccessDialog(
            isVisible = true,
            isAction = true,
            message = splashState.successMessage,
            onDismiss = { })
    }

    if (splashState.isError) {
        GlobalErrorDialog(
            isVisible = true,
            isAction = true,
            statusCode = splashState.errorStatusCode,
            title = splashState.errorTitle,
            message = splashState.errorMessage,
            onDismiss = {
                splashViewModel.onEvent(SplashEvent.GetIsLogged)
            },
            onNavigateLogin = { })
    }
}