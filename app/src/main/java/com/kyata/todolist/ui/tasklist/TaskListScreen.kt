// File: ui/tasklist/TaskListScreen.kt
package com.kyata.todolist.ui.tasklist

import android.util.Log
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
import com.kyata.todolist.ui.compose.DateHeader
import com.kyata.todolist.ui.compose.TaskItem
import com.kyata.todolist.ui.compose.groupTasksByDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(1) } // 0=done, 1=active, 2=overdue

    LaunchedEffect(Unit) {
        viewModel.checkOverdueTasks()
    }

    val completedTasks by viewModel.completedTasks.collectAsState(initial = emptyList())
    val activeTasks by viewModel.activeTasks.collectAsState(initial = emptyList())
    val overdueTasks by viewModel.overdueTasks.collectAsState(initial = emptyList())

    LaunchedEffect(activeTasks.size, overdueTasks.size) {
        Log.d("TaskListScreen", "Active tasks: ${activeTasks.size}, Overdue tasks: ${overdueTasks.size}")
    }

    val tasks = when (selectedTab) {
        0 -> completedTasks
        1 -> activeTasks
        2 -> overdueTasks
        else -> activeTasks
    }

    // Nhóm task theo ngày (đã được sắp xếp theo priority)
    val groupedTasks = remember(tasks) {
        groupTasksByDate(tasks)
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
                groupedTasks.forEach { (dateLabel, tasksForDate) ->
                    item {
                        DateHeader(dateLabel)
                    }
                    items(tasksForDate) { task ->
                        TaskItem(
                            task = task,
                            onClick = { onTaskClick(task.id.toString()) }
                        )
                    }
                }
            }
        }
    }
}