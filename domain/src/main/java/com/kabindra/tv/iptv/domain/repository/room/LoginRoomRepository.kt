package com.kabindra.tv.iptv.domain.repository.room

import com.kabindra.tv.iptv.domain.entity.LoginCredentials
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface LoginRoomRepository {
    suspend fun getLoginUser(loginCredentials: LoginCredentials): Flow<Result<User>>
    suspend fun getUser(): Flow<Result<User>>
}
