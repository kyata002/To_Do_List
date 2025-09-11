package com.kyata.todolist.ui.battery.model

data class BatteryHistory(
    val time: Long,
    val level: Int,
    val isCharging: Boolean
)
