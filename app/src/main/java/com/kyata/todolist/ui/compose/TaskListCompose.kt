package com.kyata.todolist.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kyata.todolist.data.model.Task
import com.kyata.todolist.data.model.TaskPriority


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
                Row {
                    PriorityTag(task.priority)
                    Spacer(Modifier.width(8.dp))
                    TaskStatusTag(task)
                }
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
@Composable
fun PriorityTag(priority: TaskPriority) {
    val (text, color) = when (priority) {
        TaskPriority.HIGH -> "Cao" to MaterialTheme.colorScheme.error
        TaskPriority.MEDIUM -> "Trung bình" to MaterialTheme.colorScheme.tertiary
        TaskPriority.LOW -> "Thấp" to MaterialTheme.colorScheme.primary
    }
    AssistChip(
        onClick = {},
        label = { Text(text, color = color) }
    )
}

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
