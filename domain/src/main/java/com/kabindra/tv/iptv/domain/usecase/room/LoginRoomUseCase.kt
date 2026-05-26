package com.kabindra.tv.iptv.domain.usecase.room

import com.kabindra.tv.iptv.domain.entity.LoginCredentials
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.domain.repository.room.LoginRoomRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class LoginRoomUseCase(private val repository: LoginRoomRepository) {
    suspend fun executeGetLoginUser(loginCredentials: LoginCredentials): Flow<Result<User>> {
        return repository.getLoginUser(loginCredentials)
    }

    suspend fun executeGetUser(): Flow<Result<User>> {
        return repository.getUser()
    }
}
