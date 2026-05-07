package com.kabindra.tv.iptv.domain.repository.room

import com.kabindra.tv.iptv.data.request.LoginUserDataRequest
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.utils.ktor.Result
import kotlinx.coroutines.flow.Flow

interface LoginRoomRepository {
    suspend fun getLoginUser(loginUserDataRequest: LoginUserDataRequest): Flow<Result<User>>
}