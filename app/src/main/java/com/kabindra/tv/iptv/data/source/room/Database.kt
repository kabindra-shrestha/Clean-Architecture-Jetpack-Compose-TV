package com.kabindra.tv.iptv.data.source.room

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kabindra.tv.iptv.utils.appContext
import kotlinx.coroutines.Dispatchers

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `live_tv_categories` (
                `category_id` TEXT NOT NULL,
                `category_name` TEXT,
                `parent_id` INTEGER,
                PRIMARY KEY(`category_id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `live_tv_channels` (
                `num` INTEGER,
                `name` TEXT,
                `stream_type` TEXT,
                `stream_id` INTEGER NOT NULL,
                `stream_icon` TEXT,
                `epg_channel_id` TEXT,
                `added` TEXT,
                `custom_sid` TEXT,
                `tv_archive` INTEGER,
                `direct_source` TEXT,
                `tv_archive_duration` INTEGER,
                `category_id` TEXT,
                `category_ids` TEXT,
                `thumbnail` TEXT,
                PRIMARY KEY(`stream_id`)
            )
            """.trimIndent()
        )
    }
}

fun getDatabaseBuilder(): AppDatabase {
    val dbFile = appContext!!.getDatabasePath("jetpackComposeTVCleanArchitecture.db")
    return Room.databaseBuilder<AppDatabase>(context = appContext!!, name = dbFile.absolutePath)
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2)
        // Note: Remove allowMainThreadQueries() for production use.
        // Temporarily allow for debugging
        .allowMainThreadQueries()
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()
}
