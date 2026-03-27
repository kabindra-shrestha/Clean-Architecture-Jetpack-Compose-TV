package com.kabindra.tv.iptv.utils.constants

sealed class ConfirmationType {
    data object None : ConfirmationType()
    data object Logout : ConfirmationType()
}