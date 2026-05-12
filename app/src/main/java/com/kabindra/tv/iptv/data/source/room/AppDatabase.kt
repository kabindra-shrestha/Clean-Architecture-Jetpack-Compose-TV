package com.kabindra.tv.iptv.data.source.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kabindra.tv.iptv.data.model.LiveTVCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveTVDTO
import com.kabindra.tv.iptv.data.model.MovieCategoryDTO
import com.kabindra.tv.iptv.data.model.MovieDTO
import com.kabindra.tv.iptv.data.model.UserDTO
import com.kabindra.tv.iptv.data.source.room.converter.LiveTVConverters
import com.kabindra.tv.iptv.data.source.room.dao.LiveTVDao
import com.kabindra.tv.iptv.data.source.room.dao.MovieDao
import com.kabindra.tv.iptv.data.source.room.dao.UserDao

@Database(
    entities = [
        UserDTO::class,
        LiveTVCategoryDTO::class,
        LiveTVDTO::class,
        MovieCategoryDTO::class,
        MovieDTO::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)

@TypeConverters(LiveTVConverters::class)

abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val liveTVDao: LiveTVDao
    abstract val movieDao: MovieDao
}
