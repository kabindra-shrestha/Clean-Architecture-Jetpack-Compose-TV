package com.kabindra.tv.iptv.utils.handler

import org.koin.core.component.KoinComponent

class AuthenticationHandler : KoinComponent {

    fun logout(onNavigateLogin: () -> Unit) {
        onNavigateLogin()
    }

}