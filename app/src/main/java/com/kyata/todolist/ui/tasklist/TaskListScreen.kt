package com.kyata.todolist.ui.tasklist

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyata.todolist.data.model.Task
import com.kyata.todolist.ui.addtask.TaskViewModel
import com.kyata.todolist.ui.compose.TaskItem
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(1) } // 0=done, 1=active, 2=overdue

    // Chỉ gọi checkOverdueTasks một lần khi màn hình được khởi tạo
    LaunchedEffect(Unit) {
        viewModel.checkOverdueTasks()
    }

    // Sửa lại cách lấy dữ liệu
    val completedTasks by viewModel.completedTasks.collectAsState(initial = emptyList())
    val activeTasks by viewModel.activeTasks.collectAsState(initial = emptyList())
    val overdueTasks by viewModel.overdueTasks.collectAsState(initial = emptyList())

    // Thêm logging để debug
    LaunchedEffect(activeTasks.size, overdueTasks.size) {
        Log.d("TaskListScreen", "Active tasks: ${activeTasks.size}, Overdue tasks: ${overdueTasks.size}")
    }

    val tasks = when (selectedTab) {
        0 -> completedTasks // Tab Done - lấy từ database
        1 -> activeTasks    // Tab Đang Làm
        2 -> overdueTasks   // Tab Quá Hạn
        else -> activeTasks
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kyata ToDo") },
                navigationIcon = {
                    IconButton(onClick = { /* app icon */ }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "App Icon")
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Done") },
                    label = { Text("Done") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Active") },
                    label = { Text("Đang Làm") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Overdue") },
                    label = { Text("Quá Hạn") }
                )
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "Không có công việc đã hoàn thành"
                        1 -> "Không có công việc đang làm"
                        2 -> "Không có công việc quá hạn"
                        else -> "Không có công việc nào"
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onClick = { onTaskClick(task.id.toString()) }
                    )
                }
            }
        }
    }
}





