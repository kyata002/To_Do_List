package com.kyata.todolist.ui.memory.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_usage")
data class MemoryUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val usedPercent: Float,
    val totalRamMb: Int,
    val usedRamMb: Int
)
