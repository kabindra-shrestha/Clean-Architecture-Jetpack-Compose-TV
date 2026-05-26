package com.kabindra.tv.iptv.domain.repository.notification

import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import kotlinx.coroutines.flow.Flow

interface NotificationStreamRepository {
    val connectionState: Flow<ConnectionState>
    val notifications: Flow<NotificationMessage>
}
