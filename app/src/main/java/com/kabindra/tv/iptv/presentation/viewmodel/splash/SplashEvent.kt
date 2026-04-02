package com.kabindra.tv.iptv.presentation.viewmodel.splash

sealed class SplashEvent {

    data object GetIsLogged : SplashEvent()

    data object GetUser : SplashEvent()

}