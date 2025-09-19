package com.kyata.todolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Đổi từ Int sang Long
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val startTime: Long,
    val endTime: Long,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isOverdue: Boolean = false,
    val isHardCore: Boolean = false

)
enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}