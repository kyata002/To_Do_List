package com.kyata.todolist.ui.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter

object BatteryUtils {
    fun getBatteryStatusIntent(context: Context): Intent? {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        return context.registerReceiver(null, filter)
    }
}
