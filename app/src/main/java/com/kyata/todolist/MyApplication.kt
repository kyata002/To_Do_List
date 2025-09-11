package com.kyata.todolist

import android.app.Application
import com.kyata.todolist.ui.memory.repository.MemoryWorker

// MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Bắt đầu worker khi app khởi động
        MemoryWorker.schedulePeriodicWork(this)
    }
}