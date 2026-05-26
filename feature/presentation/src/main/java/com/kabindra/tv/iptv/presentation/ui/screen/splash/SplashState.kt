package com.kabindra.tv.iptv.presentation.ui.screen.splash

import com.kabindra.tv.iptv.utils.constants.ConfirmationType
import com.kabindra.tv.iptv.utils.constants.ResponseType

data class SplashState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isError: Boolean = false,
    val isEmpty: Boolean = false,
    val successType: ResponseType = ResponseType.None,
    val successMessage: String = "",
    val errorType: ResponseType = ResponseType.None,
    val errorStatusCode: Int = -1,
    val errorTitle: String = "",
    val errorMessage: String = "",
    val confirmationType: ConfirmationType = ConfirmationType.None,
    val confirmationMessage: String = "",
    val isLogged: Boolean? = false,
)
