package com.kabindra.mobile.iptv.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

// Data class to hold timer state and actions
data class TimerState(
    val initialTime: Int,
    val currentTime: Int,
    val isRunning: Boolean,
    val start: () -> Unit,
    val pause: () -> Unit,
    val reset: () -> Unit
)

// Timer logic as a reusable function with start, pause, and reset functionality
@Composable
fun timer(initialTime: Int): TimerState {
    var time by remember { mutableIntStateOf(initialTime) }
    var isRunning by remember { mutableStateOf(false) }

    // Coroutine to handle the timer countdown
    LaunchedEffect(isRunning) {
        if (isRunning && time > 0) {
            while (time > 0) {
                delay(1000L)
                time--
            }
            isRunning = false // Stop when time reaches 0
        }
    }

    // Return a TimerState object that contains time and control functions
    return TimerState(
        initialTime = initialTime,
        currentTime = time,
        isRunning = isRunning,
        start = { isRunning = true },   // Start the timer
        pause = { isRunning = false },  // Pause the timer
        reset = {                       // Reset the timer to the initial time
            isRunning = false
            time = initialTime
        }
    )
}


// Function to convert seconds to HH:mm format
fun formatTime(seconds: Int): String {
    // val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    // val formattedHours = if (hours < 10) "0$hours" else "$hours"
    val formattedMinutes = if (minutes < 10) "0$minutes" else "$minutes"
    val formattedSeconds = if (remainingSeconds < 10) "0$remainingSeconds" else "$remainingSeconds"

    return "($formattedMinutes:$formattedSeconds)"
}