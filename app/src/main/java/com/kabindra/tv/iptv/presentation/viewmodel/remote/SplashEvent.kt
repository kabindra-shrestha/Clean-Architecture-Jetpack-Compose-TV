package com.kabindra.tv.iptv.presentation.viewmodel.remote

import com.kabindra.tv.iptv.data.request.RefreshTokenDataRequest

sealed class SplashEvent {
    data class GetLoginRefreshUserDetails(
        val refreshTokenDataRequest: RefreshTokenDataRequest
    ) : SplashEvent()

    data object GetIsLogged : SplashEvent()

    data object GetUser : SplashEvent()
}