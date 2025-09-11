package com.kyata.todolist.ui.battery

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.kyata.todolist.ui.battery.model.BatteryHistory

class BatteryHistoryManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("battery_history", Context.MODE_PRIVATE)
    private val historyList = mutableStateListOf<BatteryHistory>()

    init { loadHistory() }

    fun addHistory(level: Int, isCharging: Boolean) {
        val currentTime = System.currentTimeMillis()
        val newHistory = BatteryHistory(currentTime, level, isCharging)

        // Giữ chỉ 24 điểm dữ liệu (mỗi giờ một điểm)
        if (historyList.size >= 24) {
            historyList.removeAt(0)
        }

        historyList.add(newHistory)
        saveHistory()
    }
    fun getHistory(): List<BatteryHistory> {
        return historyList.toList()
    }
    private fun saveHistory() {
        val editor = sharedPreferences.edit()
        editor.putInt("history_size", historyList.size)

        historyList.forEachIndexed { index, history ->
            editor.putLong("history_${index}_time", history.time)
            editor.putInt("history_${index}_level", history.level)
            editor.putBoolean("history_${index}_charging", history.isCharging)
        }

        editor.apply()
    }

    private fun loadHistory() {
        val size = sharedPreferences.getInt("history_size", 0)
        historyList.clear()

        for (i in 0 until size) {
            val time = sharedPreferences.getLong("history_${i}_time", 0)
            val level = sharedPreferences.getInt("history_${i}_level", 0)
            val isCharging = sharedPreferences.getBoolean("history_${i}_charging", false)

            if (time > 0) {
                historyList.add(BatteryHistory(time, level, isCharging))
            }
        }
    }
}
