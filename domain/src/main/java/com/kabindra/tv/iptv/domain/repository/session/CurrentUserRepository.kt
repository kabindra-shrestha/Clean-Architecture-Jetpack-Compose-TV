package com.kabindra.tv.iptv.domain.repository.session

import com.kabindra.tv.iptv.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface CurrentUserRepository {
    suspend fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>
}
