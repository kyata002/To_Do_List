package com.kyata.todolist.ui.memory.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MemoryUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memoryUsage: MemoryUsage)

    @Query("SELECT * FROM memory_usage WHERE date(timestamp/1000, 'unixepoch', 'localtime') = date(:day/1000, 'unixepoch', 'localtime')")
    suspend fun getByDay(day: Long): List<MemoryUsage>

    // Add this for better data management
    @Query("DELETE FROM memory_usage WHERE timestamp < :threshold")
    suspend fun deleteOldData(threshold: Long)

    @Query("SELECT * FROM memory_usage WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getByTimeRange(startTime: Long, endTime: Long): List<MemoryUsage>

    @Query("SELECT DISTINCT date(timestamp/1000, 'unixepoch', 'localtime') as day FROM memory_usage ORDER BY day DESC")
    suspend fun getAvailableDays(): List<String>
}