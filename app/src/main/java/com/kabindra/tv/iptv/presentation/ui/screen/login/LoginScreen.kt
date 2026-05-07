package com.kabindra.tv.iptv.presentation.ui.screen.login

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabindra.tv.iptv.BuildConfig
import com.kabindra.tv.iptv.data.request.LoginUserDataRequest
import com.kabindra.tv.iptv.presentation.ui.component.AppIcon
import com.kabindra.tv.iptv.presentation.ui.component.ButtonComponent
import com.kabindra.tv.iptv.presentation.ui.component.ButtonType
import com.kabindra.tv.iptv.presentation.ui.component.InputComponent
import com.kabindra.tv.iptv.presentation.ui.component.LoadingIndicator
import com.kabindra.tv.iptv.presentation.ui.component.TextComponent
import com.kabindra.tv.iptv.presentation.ui.component.TextSize
import com.kabindra.tv.iptv.presentation.ui.component.TextType
import com.kabindra.tv.iptv.utils.Connectivity
import com.kabindra.tv.iptv.utils.error.GlobalErrorDialog
import com.kabindra.tv.iptv.utils.extensions.mainBackground
import com.kabindra.tv.iptv.utils.success.GlobalSuccessDialog
import network.chaintech.sdpcomposemultiplatform.sdp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = koinViewModel(),
    innerPadding: PaddingValues,
    onNavigateDashboard: () -> Unit
) {

    val connectivity = remember { Connectivity() }
    val isConnected by connectivity.isConnectedState.collectAsState()
    val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()

    var serverName by remember {
        mutableStateOf(
            if (BuildConfig.DEBUG) {
                "tv.quierover.xyz"
            } else {
                ""
            }
        )
    }
    var userName by remember {
        mutableStateOf(
            if (BuildConfig.DEBUG) {
                "SAMIR18"
            } else {
                ""
            }
        )
    }
    var password by remember {
        mutableStateOf(
            if (BuildConfig.DEBUG) {
                "Banana18"
            } else {
                ""
            }
        )
    }

    val scrollState = rememberScrollState()

    // Use DisposableEffect to reset states when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            // Reset the relevant states
            loginViewModel.resetStates()
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
                loginViewModel.resetStates()
            },
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBackground()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .width(350.sdp)
                    .padding(16.sdp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                AppIcon(
                    modifier = Modifier
                        .width(70.sdp)
                        .height(70.sdp)
                )

                Spacer(modifier = Modifier.height(4.sdp))

                TextComponent(
                    text = "Login to your account",
                    type = TextType.Headline,
                    size = TextSize.Medium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.sdp))

                InputComponent(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = "Server Name",
                    placeholder = "http://example.com"
                )

                Spacer(modifier = Modifier.height(8.sdp))

                InputComponent(
                    value = userName,
                    onValueChange = { userName = it },
                    label = "Username",
                    placeholder = "Enter your username"
                )

                Spacer(modifier = Modifier.height(8.sdp))

                InputComponent(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Enter your password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(12.sdp))

                ButtonComponent(
                    text = "Login",
                    type = ButtonType.Filled,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loginState.isLoading,
                    onClick = {
                        if (serverName.isNotBlank() && userName.isNotBlank() && password.isNotBlank()) {
                            loginViewModel.onEvent(
                                LoginEvent.GetLogin(
                                    LoginUserDataRequest(
                                        server_name = serverName,
                                        username = userName,
                                        password = password
                                    )
                                )
                            )
                        }
                    }
                )

                if (loginState.isLoading) {
                    Spacer(modifier = Modifier.height(16.sdp))
                    LoadingIndicator(
                        isCircular = true,
                        useExpressive = true
                    )
                }
            }
        }
    }

    // when login is done and isLogged is true then navigate to dashboard
    if (loginState.isLogged == true) {
        onNavigateDashboard()
    }

    if (loginState.isSuccess) {
        GlobalSuccessDialog(
            isVisible = true,
            isAction = true,
            message = loginState.successMessage,
            onDismiss = { })
    }

    if (loginState.isError) {
        GlobalErrorDialog(
            isVisible = true,
            isAction = true,
            statusCode = loginState.errorStatusCode,
            title = loginState.errorTitle,
            message = loginState.errorMessage,
            onDismiss = {
                loginViewModel.onEvent(LoginEvent.GetIsLogged)
            },
            onNavigateLogin = { })
    }
}