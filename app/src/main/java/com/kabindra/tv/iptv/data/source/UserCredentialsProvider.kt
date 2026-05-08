package com.kabindra.tv.iptv.data.source

import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.source.room.AppDatabase
import com.kabindra.tv.iptv.domain.entity.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Provider to retrieve and manage user credentials from the database
 * This is used by Xtream services to dynamically fetch API endpoints and credentials
 */
interface UserCredentialsProvider {
    suspend fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>
}

class UserCredentialsProviderImpl(
    private val appDatabase: AppDatabase
) : UserCredentialsProvider {

    override suspend fun getCurrentUser(): User? {
        val users = appDatabase.userDao.findAll()
        return users.firstOrNull()?.toDomain()  // Return the most recently logged in user
    }

    override fun observeCurrentUser(): Flow<User?> {
        return appDatabase.userDao.getAllUsersFlow().map { users ->
            users.firstOrNull()?.toDomain()  // Return the most recently logged in user
        }
    }
}

