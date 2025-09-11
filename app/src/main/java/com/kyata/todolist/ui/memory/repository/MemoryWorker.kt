package com.kyata.todolist.ui.memory.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kyata.todolist.AppDatabase
import java.util.concurrent.TimeUnit

// MemoryWorker.kt
class MemoryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val repo = MemoryRepository(
            AppDatabase.getInstance(applicationContext).memoryUsageDao(),
            applicationContext
        )
        repo.recordMemoryUsage()
        return Result.success()
    }

    companion object {
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<MemoryWorker>(
                30, TimeUnit.MINUTES // Thu thập mỗi 30 phút
            ).setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "memory_usage_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }
}