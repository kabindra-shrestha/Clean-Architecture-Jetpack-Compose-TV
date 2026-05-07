package com.kabindra.tv.iptv.data.source

import com.kabindra.tv.iptv.data.model.UserDTO
import com.kabindra.tv.iptv.data.source.room.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Provider to retrieve and manage user credentials from the database
 * This is used by Xtream services to dynamically fetch API endpoints and credentials
 */
interface UserCredentialsProvider {
    suspend fun getCurrentUser(): UserDTO?
    fun observeCurrentUser(): Flow<UserDTO?>
}

class UserCredentialsProviderImpl(
    private val appDatabase: AppDatabase
) : UserCredentialsProvider {

    override suspend fun getCurrentUser(): UserDTO? {
        val users = appDatabase.userDao.findAll()
        return users.firstOrNull()  // Return the most recently logged in user
    }

    override fun observeCurrentUser(): Flow<UserDTO?> {
        return appDatabase.userDao.getAllUsersFlow().map { users ->
            users.firstOrNull()  // Return the most recently logged in user
        }
    }
}

