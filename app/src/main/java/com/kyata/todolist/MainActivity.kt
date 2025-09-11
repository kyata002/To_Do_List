package com.kyata.systemmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kyata.todolist.ui.DashboardScreen
import com.kyata.todolist.SystemInfoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.kyata.todolist.ui.battery.BatteryDetailScreen
import com.kyata.todolist.ui.cpu.CpuDetailScreen
import com.kyata.todolist.ui.MemoryDetailScreen
import com.kyata.todolist.ui.memory.repository.MemoryWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            MemoryWorker.schedulePeriodicWork(this)
        setContent {
            SystemMonitorApp()
        }
    }
}

@Composable
fun SystemMonitorApp() {
    val navController = rememberNavController()
    val systemInfoViewModel: SystemInfoViewModel = viewModel()
    val state = systemInfoViewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                cpuUsage = state.value.cpuUsage,
                batteryLevel = state.value.batteryLevel,
                memoryUsage = state.value.memoryUsage,
                batteryTemp = state.value.batteryTemp,
                netDownloadSpeed = state.value.netDownloadSpeed,
                netUploadSpeed = state.value.netUploadSpeed,
                onCpuClick = { navController.navigate("cpu_detail") },
                onBatteryClick = { navController.navigate("battery_detail") },
                onMemoryClick = { navController.navigate("memory_detail") },
                onSettingsClick = {}
            )
        }

        composable("cpu_detail") {
            CpuDetailScreen(onBack = { navController.popBackStack() })
        }

        composable("battery_detail") {
            BatteryDetailScreen(onBack = { navController.popBackStack() })
        }

        composable("memory_detail") {
            MemoryDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

