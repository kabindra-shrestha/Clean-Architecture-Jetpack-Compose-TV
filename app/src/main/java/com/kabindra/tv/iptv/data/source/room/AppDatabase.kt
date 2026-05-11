package com.kabindra.tv.iptv.data.source.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kabindra.tv.iptv.data.model.LiveTVCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveTVDTO
import com.kabindra.tv.iptv.data.model.UserDTO
import com.kabindra.tv.iptv.data.source.room.converter.LiveTVConverters
import com.kabindra.tv.iptv.data.source.room.dao.LiveTVDao
import com.kabindra.tv.iptv.data.source.room.dao.UserDao

@Database(
    entities = [
        UserDTO::class,
        LiveTVCategoryDTO::class,
        LiveTVDTO::class,
    ],
    version = 2,
    exportSchema = true
)

@TypeConverters(LiveTVConverters::class)

abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val liveTVDao: LiveTVDao
}
