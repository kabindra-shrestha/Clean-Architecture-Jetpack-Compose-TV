package com.kabindra.tv.iptv.data.source.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kabindra.tv.iptv.data.model.LiveTVCategoryDTO
import com.kabindra.tv.iptv.data.model.LiveTVDTO
import kotlinx.coroutines.flow.Flow

@Dao
interface LiveTVDao {

    @Query("SELECT * FROM live_tv_categories ORDER BY rowid ASC")
    fun observeCategories(): Flow<List<LiveTVCategoryDTO>>

    @Query("SELECT * FROM live_tv_channels ORDER BY num ASC")
    fun observeChannels(): Flow<List<LiveTVDTO>>

    @Query(
        """
        SELECT CASE
            WHEN (SELECT COUNT(*) FROM live_tv_categories) > 0
             AND (SELECT COUNT(*) FROM live_tv_channels) > 0
            THEN 1 ELSE 0
        END
        """
    )
    suspend fun hasLiveTVData(): Boolean

    @Query("DELETE FROM live_tv_channels")
    suspend fun deleteChannels()

    @Query("DELETE FROM live_tv_categories")
    suspend fun deleteCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<LiveTVCategoryDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<LiveTVDTO>)

    @Transaction
    suspend fun replaceAll(
        categories: List<LiveTVCategoryDTO>,
        channels: List<LiveTVDTO>,
    ) {
        deleteChannels()
        deleteCategories()
        insertCategories(categories)
        insertChannels(channels)
    }
}
