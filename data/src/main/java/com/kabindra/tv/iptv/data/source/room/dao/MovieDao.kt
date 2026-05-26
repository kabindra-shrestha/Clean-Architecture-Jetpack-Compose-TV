package com.kabindra.tv.iptv.data.source.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kabindra.tv.iptv.data.model.MovieCategoryDTO
import com.kabindra.tv.iptv.data.model.MovieDTO
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movie_categories ORDER BY rowid ASC")
    fun observeCategories(): Flow<List<MovieCategoryDTO>>

    @Query("SELECT * FROM movies ORDER BY num ASC")
    fun observeMovies(): Flow<List<MovieDTO>>

    @Query(
        """
        SELECT CASE
            WHEN (SELECT COUNT(*) FROM movie_categories) > 0
             AND (SELECT COUNT(*) FROM movies) > 0
            THEN 1 ELSE 0
        END
        """
    )
    suspend fun hasMovieData(): Boolean

    @Query("DELETE FROM movies")
    suspend fun deleteMovies()

    @Query("DELETE FROM movie_categories")
    suspend fun deleteCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<MovieCategoryDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieDTO>)

    @Transaction
    suspend fun replaceAll(
        categories: List<MovieCategoryDTO>,
        movies: List<MovieDTO>,
    ) {
        deleteMovies()
        deleteCategories()
        insertCategories(categories)
        insertMovies(movies)
    }
}
