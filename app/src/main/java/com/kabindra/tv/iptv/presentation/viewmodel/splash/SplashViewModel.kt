package com.kabindra.tv.iptv.presentation.viewmodel.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class SplashViewModel(
) : ViewModel() {
    private val _splashState = MutableStateFlow(SplashState())

    val splashState = _splashState
        .onStart { }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SplashState()
        )

    fun onEvent(event: SplashEvent) {
        when (event) {
            is SplashEvent.GetIsLogged -> {
            }

            is SplashEvent.GetUser -> {
            }
        }
    }

    fun resetStates() {
        _splashState.value = SplashState()
    }
}