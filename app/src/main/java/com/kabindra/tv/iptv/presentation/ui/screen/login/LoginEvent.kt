package com.kabindra.tv.iptv.presentation.ui.screen.login

import com.kabindra.tv.iptv.data.request.LoginUserDataRequest

sealed class LoginEvent {

    data class GetLogin(val loginUserDataRequest: LoginUserDataRequest) : LoginEvent()

    data object GetIsLogged : LoginEvent()

    data object GetUser : LoginEvent()

}