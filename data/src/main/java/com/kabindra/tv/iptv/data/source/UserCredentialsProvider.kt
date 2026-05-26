package com.kabindra.tv.iptv.data.source

import com.kabindra.tv.iptv.data.model.toDomain
import com.kabindra.tv.iptv.data.source.room.AppDatabase
import com.kabindra.tv.iptv.domain.entity.User
import com.kabindra.tv.iptv.domain.repository.session.CurrentUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Data implementation for the domain current-user contract.
 * Xtream data sources use it to dynamically fetch API endpoints and credentials.
 */
class UserCredentialsProviderImpl(
    private val appDatabase: AppDatabase
) : CurrentUserRepository {

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
