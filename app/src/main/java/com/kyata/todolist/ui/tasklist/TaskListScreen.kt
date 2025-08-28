package com.kyata.todolist.ui.tasklist

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    // 🔥 Demo data
    val sampleTasks = listOf(
        Task(
            id = 1,
            title = "Học Jetpack Compose",
            description = "Xem lại State & Navigation",
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + 60 * 60 * 1000 // +1h
        ),
        Task(
            id = 2,
            title = "Đi tập gym",
            description = "Tập ngực tay trước",
            isCompleted = false,
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + 90 * 60 * 1000 // +1.5h
        ),
        Task(
            id = 3,
            title = "Code ToDo App",
            description = "Thêm Hilt + Room",
            isCompleted = true,
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + 120 * 60 * 1000 // +2h
        )
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kyata ToDo") },
                navigationIcon = {
                    IconButton(onClick = { /* App icon click action */ }) {
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(sampleTasks) { task ->
                TaskItem(
                    task = task,
                    onClick = { onTaskClick(task.id.toString()) }
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Hàng trên: Tiêu đề + Tag trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                TaskStatusTag(task)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Thời gian bắt đầu + hoàn thành
            task.startTime?.let { start ->
                Text(
                    text = "Bắt đầu: ${formatDateTime(start)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                task.endTime?.let { end ->
                    val duration = formatDuration(end - start)
                    Text(
                        text = "Hoàn thành: ${formatDateTime(end)} ($duration)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Description 1 dòng
            if (!task.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TaskStatusTag(task: Task) {
    val statusText: String
    val statusColor: Color

    when {
        task.isCompleted -> {
            statusText = "Hoàn thành"
            statusColor = MaterialTheme.colorScheme.tertiary
        }
        task.endTime != null && task.endTime < System.currentTimeMillis() -> {
            statusText = "Quá hạn"
            statusColor = MaterialTheme.colorScheme.error
        }
        else -> {
            statusText = "Đang làm"
            statusColor = MaterialTheme.colorScheme.primary
        }
    }

    AssistChip(
        onClick = { /* có thể trigger filter hoặc action khác */ },
        label = {
            Text(
                text = statusText,
                color = statusColor,
                style = MaterialTheme.typography.labelSmall
            )
        }
    )
}
// Format ngày giờ
fun formatDateTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

// Format duration (ms -> ngày, giờ, phút)
fun formatDuration(durationMillis: Long): String {
    val seconds = durationMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h"
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}


