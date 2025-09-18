package com.kyata.todolist.ui.tasklist

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kyata.todolist.data.AppDatabase

class CheckOverdueWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("CheckOverdueWorker", "Checking for overdue tasks")

            val db = AppDatabase.getInstance(applicationContext)
            val currentTime = System.currentTimeMillis()

            // Lấy các task cần đánh dấu quá hạn
            val tasksToMark = db.taskDao().getTasksToMarkOverdue(currentTime)

            Log.d("CheckOverdueWorker", "Found ${tasksToMark.size} tasks to mark overdue")

            tasksToMark.forEach { task ->
                Log.d("CheckOverdueWorker", "Marking task ${task.id} as overdue")
                db.taskDao().markOverdue(task.id)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("CheckOverdueWorker", "Error checking overdue tasks", e)
            Result.failure()
        }
    }
}