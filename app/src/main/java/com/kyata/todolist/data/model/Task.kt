package com.kyata.todolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val startTime: Long,
    val endTime: Long,
    val priority: TaskPriority = TaskPriority.MEDIUM
)
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}