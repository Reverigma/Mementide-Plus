package com.reverigma.habittrack.data.dao

import androidx.room.*
import com.reverigma.habittrack.data.model.Anniversary
import kotlinx.coroutines.flow.Flow

@Dao
interface AnniversaryDao {

    @Query("SELECT * FROM anniversaries ORDER BY date ASC")
    fun observeAll(): Flow<List<Anniversary>>

    @Insert
    suspend fun insert(a: Anniversary)

    @Update
    suspend fun update(a: Anniversary)

    @Delete
    suspend fun delete(a: Anniversary)
}
