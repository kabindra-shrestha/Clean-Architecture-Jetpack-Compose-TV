package com.kabindra.tv.iptv.presentation.ui.screen.login

import com.kabindra.tv.iptv.domain.entity.LoginCredentials

sealed class LoginEvent {

    data class GetLogin(val loginCredentials: LoginCredentials) : LoginEvent()

    data object GetIsLogged : LoginEvent()

    data object GetUser : LoginEvent()

}
