package com.kyata.todolist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kyata.todolist.data.dao.TaskDao
import com.kyata.todolist.data.model.Task

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}