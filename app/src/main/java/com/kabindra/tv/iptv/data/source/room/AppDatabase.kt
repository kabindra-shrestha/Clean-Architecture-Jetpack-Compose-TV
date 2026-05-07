package com.kabindra.tv.iptv.data.source.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kabindra.tv.iptv.data.model.UserDTO
import com.kabindra.tv.iptv.data.source.room.dao.UserDao

@Database(
    entities = [UserDTO::class],
    version = 1,
    exportSchema = true
)

@TypeConverters()

abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
}
