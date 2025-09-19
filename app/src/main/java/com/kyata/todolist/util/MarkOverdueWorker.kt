package com.kyata.todolist.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kyata.todolist.data.AppDatabase

class MarkOverdueWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "MarkOverdueWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getLong("taskId", -1)
            if (taskId != -1L) {
                Log.d(TAG, "Marking task $taskId as overdue")

                val db = AppDatabase.getInstance(applicationContext)
                db.taskDao().markOverdue(taskId)

                Log.d(TAG, "Task $taskId marked as overdue successfully")
                Result.success()
            } else {
                Log.w(TAG, "Invalid task ID received")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking task as overdue: ${e.message}", e)
            Result.failure()
        }
    }
}