package com.kabindra.mobile.iptv.presentation.ui.adaptive

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun LockLandscapeOrientation(enabled: Boolean) {
    val activity = LocalContext.current.findActivity() ?: return

    DisposableEffect(activity, enabled) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }

        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            activity.requestedOrientation = previousOrientation
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
