package com.kabindra.mobile.iptv.socket

import com.kabindra.mobile.iptv.service.SocketForegroundService
import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import com.kabindra.tv.iptv.domain.repository.notification.NotificationStreamRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

private const val SOCKET_CLIENT_POLL_INTERVAL_MS = 500L

class SocketNotificationStreamRepository : NotificationStreamRepository {
    override val connectionState: Flow<ConnectionState> = flow {
        emitAll(awaitSocketClient().connectionState)
    }

    override val notifications: Flow<NotificationMessage> = flow {
        emitAll(awaitSocketClient().notifications)
    }

    private suspend fun awaitSocketClient(): KtorSocketClient {
        while (true) {
            SocketForegroundService.socketClient?.let { return it }
            delay(SOCKET_CLIENT_POLL_INTERVAL_MS)
        }
    }
}
