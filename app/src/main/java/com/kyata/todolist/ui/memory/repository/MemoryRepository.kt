package com.kyata.todolist.ui.memory.repository

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.kyata.todolist.ui.memory.model.MemoryUsage
import com.kyata.todolist.ui.memory.model.MemoryUsageDao
import java.io.File
import kotlin.sequences.forEach

class MemoryRepository(private val dao: MemoryUsageDao, private val context: Context) {


    suspend fun getDataByDay(day: Long): List<MemoryUsage> = dao.getByDay(day)

    private fun getCurrentMemoryInfo(context: Context): MemoryUsage {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo().apply { am.getMemoryInfo(this) }

        val totalRamMb = (memoryInfo.totalMem / 1024 / 1024).toInt()
        val availRamMb = (memoryInfo.availMem / 1024 / 1024).toInt()
        val usedRamMb = totalRamMb - availRamMb
        val usedPercent = usedRamMb.toFloat() / totalRamMb * 100

        return MemoryUsage(
            timestamp = System.currentTimeMillis(),
            usedPercent = usedPercent,
            totalRamMb = totalRamMb,
            usedRamMb = usedRamMb
        )
    }
    fun getRealtimeMemoryInfo(): MemoryUsage {
        return getCurrentMemoryInfo(context)
    }

    // Hàm mới để lấy thông tin memory từ /proc/meminfo
    fun getRealtimeProcMemInfo(): Map<String, String> {
        return getMemInfoFromProc()
    }
    fun getMemInfoFromProc(): Map<String, String> {
        val memInfo = mutableMapOf<String, String>()
        try {
            val reader = File("/proc/meminfo").bufferedReader()
            reader.useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(Regex("\\s+"))
                    if (parts.size >= 2) {
                        val key = parts[0].removeSuffix(":")
                        val value = parts[1] + " kB"
                        memInfo[key] = value
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return memInfo
    }


    suspend fun getAvailableDays(): List<String> {
        return dao.getAvailableDays()
    }
    suspend fun recordMemoryUsage() {
        try {
            val info = getCurrentMemoryInfo(context)
            dao.insert(info)
            Log.d("MemoryRepository", "Recorded memory usage: ${info.usedPercent}%")

            // Clean up old data (keep only 30 days)
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
            dao.deleteOldData(thirtyDaysAgo)
        } catch (e: Exception) {
            Log.e("MemoryRepository", "Error recording memory usage", e)
        }
    }

    suspend fun getDataByTimeRange(startTime: Long, endTime: Long): List<MemoryUsage> {
        return try {
            dao.getByTimeRange(startTime, endTime).also {
                Log.d("MemoryRepository", "Loaded ${it.size} records from $startTime to $endTime")
            }
        } catch (e: Exception) {
            Log.e("MemoryRepository", "Error loading data by time range", e)
            emptyList()
        }
    }
}
