package com.cncindex.data.local.dao

import androidx.room.*
import com.cncindex.data.local.entity.CncProgram
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<CncProgram>)

    @Query("DELETE FROM programs")
    suspend fun deleteAll()

    @Query("SELECT * FROM programs WHERE id = :id LIMIT 1")
    suspend fun getProgramById(id: Long): CncProgram?

    @Query("SELECT * FROM programs ORDER BY program_number ASC")
    fun getAll(): Flow<List<CncProgram>>

    @Query("SELECT * FROM programs ORDER BY program_number ASC")
    suspend fun getAllList(): List<CncProgram>

    @Query("SELECT COUNT(*) FROM programs")
    suspend fun count(): Int
}
