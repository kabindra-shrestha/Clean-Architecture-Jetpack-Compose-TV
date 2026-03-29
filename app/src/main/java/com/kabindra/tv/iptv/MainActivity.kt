package com.kabindra.tv.iptv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.tv.material3.Surface
import com.kabindra.tv.iptv.presentation.ui.theme.JetpackComposeTVCleanArchitectureTheme
import network.chaintech.sdpcomposemultiplatform.sdp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        enableEdgeToEdge()

        setContent {
            JetpackComposeTVCleanArchitectureTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    App(modifier = Modifier.padding(1.sdp))
                }
            }
        }
    }
}
