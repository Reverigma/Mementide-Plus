package com.reverigma.mementideplus.data.dao

import androidx.room.*
import com.reverigma.mementideplus.data.model.Anniversary
import kotlinx.coroutines.flow.Flow

@Dao
interface AnniversaryDao {

    @Query("SELECT * FROM anniversaries ORDER BY date ASC")
    fun observeAll(): Flow<List<Anniversary>>

    @Query("SELECT * FROM anniversaries")
    suspend fun getAll(): List<Anniversary>

    @Insert
    suspend fun insert(a: Anniversary)

    @Insert
    suspend fun insertAll(list: List<Anniversary>)

    @Update
    suspend fun update(a: Anniversary)

    @Delete
    suspend fun delete(a: Anniversary)

    @Query("DELETE FROM anniversaries")
    suspend fun deleteAll()
}
