package com.kyata.todolist.ui.battery

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import com.kyata.todolist.ui.battery.model.BatteryHistory
import com.kyata.todolist.ui.battery.model.BatteryStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val _batteryStatus = MutableStateFlow<BatteryStatus?>(null)
    val batteryStatus: StateFlow<BatteryStatus?> = _batteryStatus

    private val _batteryHistory = MutableStateFlow<List<BatteryHistory>>(emptyList())
    val batteryHistory: StateFlow<List<BatteryHistory>> = _batteryHistory

    private val historyManager = BatteryHistoryManager(application)
    private var receiver: BroadcastReceiver? = null

    fun startListening() {
        val context = getApplication<Application>()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        receiver = object : BroadcastReceiver() {

            override fun onReceive(p0: Context?, p1: Intent?) {
                p1?.let { updateStatus(it) }

            }
        }
        context.registerReceiver(receiver, filter)
    }

    fun stopListening() {
        val context = getApplication<Application>()
        receiver?.let { context.unregisterReceiver(it) }
        receiver = null
    }

    private fun updateStatus(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else 0

        val status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }

        val plugged = when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Unplugged"
        }

        val temperature = (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        val isCharging = status == "Charging" || status == "Full"

        _batteryStatus.value = BatteryStatus(
            percentage = pct,
            status = status,
            isCharging = isCharging,
            plugged = plugged,
            temperature = temperature,
            voltage = voltage,
            technology = technology
        )

        // lưu history mỗi giờ
        val last = historyManager.getHistory().lastOrNull()
        val now = System.currentTimeMillis()
        if (last == null || now - last.time >= 1800000) {
            historyManager.addHistory(pct, isCharging)
            _batteryHistory.value = historyManager.getHistory()
        }
    }
}
