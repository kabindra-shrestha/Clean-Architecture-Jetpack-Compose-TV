package com.kabindra.tv.iptv.presentation.ui.screen.splash

sealed class SplashEvent {

    data object GetIsLogged : SplashEvent()

    data object GetUser : SplashEvent()

}