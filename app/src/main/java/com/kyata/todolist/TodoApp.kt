package com.kyata.todolist

import android.app.Application
import androidx.room.Room
import com.kyata.todolist.data.AppDatabase
import com.kyata.todolist.data.TaskRepository

class TodoApp : Application() {
    lateinit var repository: TaskRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "task_database"
        ).build()
        repository = TaskRepository(db.taskDao())
    }
}
