package com.kabindra.tv.iptv.data.source.room

import androidx.room.Room
import androidx.room.RoomDatabase
import com.kabindra.tv.iptv.utils.appContext
import kotlinx.coroutines.Dispatchers

fun getDatabaseBuilder(): AppDatabase {
    val dbFile = appContext!!.getDatabasePath("jetpackComposeTVCleanArchitecture.db")
    return Room.databaseBuilder<AppDatabase>(context = appContext!!, name = dbFile.absolutePath)
        .setQueryCoroutineContext(Dispatchers.IO)
        // Note: Remove allowMainThreadQueries() for production use.
        // Temporarily allow for debugging
        .allowMainThreadQueries()
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()
}