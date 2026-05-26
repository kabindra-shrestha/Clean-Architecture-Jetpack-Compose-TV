package com.kabindra.tv.iptv.data.source.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers

fun getDatabaseBuilder(context: Context): AppDatabase {
    val dbFile = context.getDatabasePath("jetpackComposeTVCleanArchitecture.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
        .setQueryCoroutineContext(Dispatchers.IO)
        // Note: Remove allowMainThreadQueries() for production use.
        // Temporarily allow for debugging
        .allowMainThreadQueries()
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .build()
}
