package com.kyata.todolist.ui.battery.model


data class BatteryStatus(
    val percentage: Int,
    val status: String,
    val isCharging: Boolean,
    val plugged: String,
    val temperature: Float,
    val voltage: Int,
    val technology: String
)
