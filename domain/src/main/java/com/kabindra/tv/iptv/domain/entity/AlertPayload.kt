package com.kabindra.tv.iptv.domain.entity

data class AlertPayload(
    val notifId: String = "",
    val title: String = "Alert",
    val message: String = "",
    val priority: String = "NORMAL",
    val timestamp: Long = System.currentTimeMillis(),
)
