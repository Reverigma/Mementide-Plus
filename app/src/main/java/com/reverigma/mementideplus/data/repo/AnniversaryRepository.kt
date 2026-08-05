package com.reverigma.mementideplus.data.repo

import com.reverigma.mementideplus.data.dao.AnniversaryDao
import com.reverigma.mementideplus.data.model.Anniversary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnniversaryRepository @Inject constructor(
    private val dao: AnniversaryDao
) {
    fun anniversaries(): Flow<List<Anniversary>> = dao.observeAll()
    suspend fun add(a: Anniversary) = dao.insert(a)
    suspend fun update(a: Anniversary) = dao.update(a)
    suspend fun delete(a: Anniversary) = dao.delete(a)
}
