package com.kyata.todolist

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.TrafficStats
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.RandomAccessFile

data class SystemInfoState(
    val cpuUsage: Float = 0f,       // <-- đổi từ Int sang Float
    val batteryLevel: Int = 0,
    val memoryUsage: Int = 0,
    val batteryTemp: Float = 0f,
    val netDownloadSpeed: Long = 0L,
    val netUploadSpeed: Long = 0L
)


class SystemInfoViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(SystemInfoState())
    val state: StateFlow<SystemInfoState> = _state

    // lưu lần trước để tính tốc độ mạng
    private var lastRxBytes: Long = TrafficStats.getTotalRxBytes()
    private var lastTxBytes: Long = TrafficStats.getTotalTxBytes()

    init {
        viewModelScope.launch {
            while (true) {
                val cpu = getCpuUsage()
                val battery = getBatteryLevel()
                val mem = getMemoryUsage()
                val temp = getBatteryTemperature()
                val (download, upload) = getNetworkSpeed()

                _state.value = SystemInfoState(
                    cpuUsage = cpu,
                    batteryLevel = battery,
                    memoryUsage = mem,
                    batteryTemp = temp,
                    netDownloadSpeed = download,
                    netUploadSpeed = upload
                )

                delay(2000) // refresh mỗi 2 giây
            }
        }
    }

    private fun getBatteryLevel(): Int {
        val context = getApplication<Application>().applicationContext
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else 0
    }

    private fun getBatteryTemperature(): Float {
        val context = getApplication<Application>().applicationContext
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        return if (temp > 0) temp / 10f else 0f
    }

    private fun getMemoryUsage(): Int {
        val context = getApplication<Application>().applicationContext
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        val used = memInfo.totalMem - memInfo.availMem
        return ((used.toDouble() / memInfo.totalMem) * 100).toInt()
    }

    private fun getCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            val toks = load.split(" +".toRegex()).toTypedArray()
            val idle1 = toks[4].toLong()
            val cpu1 = toks.drop(1).take(7).sumOf { it.toLong() }
            Thread.sleep(360)
            reader.seek(0)
            val load2 = reader.readLine()
            reader.close()
            val toks2 = load2.split(" +".toRegex()).toTypedArray()
            val idle2 = toks2[4].toLong()
            val cpu2 = toks2.drop(1).take(7).sumOf { it.toLong() }
            val idle = idle2 - idle1
            val cpu = cpu2 - cpu1
            ((cpu - idle) * 100f / cpu.toFloat())
        } catch (e: Exception) {
            0f
        }
    }


    private fun getNetworkSpeed(): Pair<Long, Long> {
        val nowRxBytes = TrafficStats.getTotalRxBytes()
        val nowTxBytes = TrafficStats.getTotalTxBytes()

        val download = ((nowRxBytes - lastRxBytes) / 1024) / 2 // KB/s (2s refresh)
        val upload = ((nowTxBytes - lastTxBytes) / 1024) / 2   // KB/s

        lastRxBytes = nowRxBytes
        lastTxBytes = nowTxBytes

        return Pair(download, upload)
    }
}
