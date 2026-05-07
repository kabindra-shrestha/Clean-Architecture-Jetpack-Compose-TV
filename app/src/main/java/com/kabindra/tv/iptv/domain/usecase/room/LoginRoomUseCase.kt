package com.kabindra.tv.iptv.domain.usecase.room

import com.kabindra.tv.iptv.data.request.LoginUserDataRequest
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.domain.repository.room.LoginRoomRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

class LoginRoomUseCase(private val repository: LoginRoomRepository) {
    suspend fun executeGetLoginUser(loginCheckDataRequest: LoginUserDataRequest): Flow<Result<User>> {
        return repository.getLoginUser(loginCheckDataRequest)
    }
}
