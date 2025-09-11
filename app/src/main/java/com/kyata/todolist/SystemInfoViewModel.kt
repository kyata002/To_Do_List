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
    var batteryLevel: Pair<Int, String> = 0 to "Unknown",
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
                val battery = getBatteryHealthInfo()
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
    fun getBatteryHealth(): String {
        val context = getApplication<Application>().applicationContext
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1

        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }
    fun getBatteryHealthPercent(): Int {
        val context = getApplication<Application>().applicationContext
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1

        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 80
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 70
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 50
            BatteryManager.BATTERY_HEALTH_COLD -> 90
            else -> 75 // Unknown
        }
    }

    fun getBatteryHealthInfo(): Pair<Int, String> {
        val context = getApplication<Application>().applicationContext
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1

        val percent = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_COLD -> 90
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 70
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 65
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 45
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            else -> 75 // Unknown
        }

        val healthText = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good (100%)"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold (~90%)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat (~70%)"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage (~65%)"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure (~45%)"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead (0%)"
            else -> "Unknown (~75%)"
        }

        return percent to healthText
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
