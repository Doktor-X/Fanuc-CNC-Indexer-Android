package com.cncindex.data.local.dao

import androidx.room.*
import com.cncindex.data.local.entity.Tool
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tools: List<Tool>)

    @Query("DELETE FROM tools")
    suspend fun deleteAll()

    @Query("SELECT * FROM tools WHERE tool_number IN (:ids) ORDER BY tool_number ASC")
    suspend fun getToolsByIds(ids: List<Int>): List<Tool>

    @Query("SELECT * FROM tools ORDER BY tool_number ASC")
    fun getAll(): Flow<List<Tool>>

    @Query("SELECT * FROM tools ORDER BY tool_number ASC")
    suspend fun getAllList(): List<Tool>

    @Query("SELECT COUNT(*) FROM tools")
    suspend fun count(): Int
}
