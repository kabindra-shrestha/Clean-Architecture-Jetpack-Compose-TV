package com.kabindra.tv.iptv.data.repository.room

import com.kabindra.tv.iptv.data.model.UserDTO
import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.request.LoginUserDataRequest
import com.kabindra.tv.iptv.data.source.room.AppDatabase
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.domain.repository.room.LoginRoomRepository
import com.kabindra.tv.iptv.utils.ktor.Result
import com.kabindra.tv.iptv.utils.ktor.ResultError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginRoomRepositoryImpl(
    private val appDatabase: AppDatabase,
) : LoginRoomRepository {

    override suspend fun getLoginUser(loginUserDataRequest: LoginUserDataRequest): Flow<Result<User>> =
        flow {
            emit(Result.Loading)
            try {
                val responses: UserDTO = UserDTO(
                    server_name = loginUserDataRequest.server_name,
                    username = loginUserDataRequest.username,
                    password = loginUserDataRequest.password
                )

                appDatabase.userDao.deleteAll()

                appDatabase.userDao.add(responses)

                emit(Result.Success(responses.toDomain()))
            } catch (e: Exception) {
                emit(Result.Error(ResultError.parseException(e)))
            }
        }

    override suspend fun getUser(): Flow<Result<User>> =
        flow {
            emit(Result.Loading)
            try {
                val responses: List<UserDTO> = appDatabase.userDao.findAll()

                if (responses.isNotEmpty()) {
                    emit(Result.Success(responses[0].toDomain()))
                } else {
                    emit(Result.Error(ResultError.parseException(Exception("No user found"))))
                }
            } catch (e: Exception) {
                emit(Result.Error(ResultError.parseException(e)))
            }
        }

}