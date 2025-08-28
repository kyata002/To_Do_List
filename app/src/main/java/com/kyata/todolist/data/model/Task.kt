package com.kyata.todolist.data.model

import androidx.room.PrimaryKey

//@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val startTime: Long? = null,
    val endTime: Long? = null
)
