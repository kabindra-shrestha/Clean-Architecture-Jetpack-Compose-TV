package com.kabindra.tv.iptv.data.source.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kabindra.tv.iptv.data.model.UserDTO
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    suspend fun findAll(): List<UserDTO>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserDTO>>

    @Upsert
    suspend fun add(user: UserDTO)

    @Query("DELETE FROM users")
    suspend fun deleteAll()

}