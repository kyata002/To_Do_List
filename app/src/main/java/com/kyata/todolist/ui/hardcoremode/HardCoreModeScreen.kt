package com.kyata.todolist.ui.hardcoremode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyata.todolist.data.model.Task
import com.kyata.todolist.ui.hardcoremode.formatElapsedTime
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardCoreModeScreen(
    tasks: List<Task>,
    onBack: () -> Unit,
    onTaskCompleted: (Long) -> Unit
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var completedTasks by remember { mutableStateOf(emptySet<Long>()) }

    // Cập nhật thời gian mỗi giây
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chế độ Hard Core",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Hiển thị thời gian
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Red.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Thời gian đã trôi qua",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                    Text(
                        text = formatElapsedTime(tasks.minByOrNull { it.startTime }?.startTime?.let { currentTime - it }
                            ?: currentTime),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }

            // Thống kê tiến độ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Đã hoàn thành",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${completedTasks.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Còn lại",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${tasks.size - completedTasks.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Danh sách task
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(tasks) { task ->
                    HardCoreTaskItem(
                        task = task,
                        isCompleted = completedTasks.contains(task.id),
                        onCompletedChange = { completed ->
                            if (completed) {
                                completedTasks = completedTasks + task.id
                                onTaskCompleted(task.id)
                            } else {
                                completedTasks = completedTasks - task.id
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HardCoreTaskItem(
    task: Task,
    isCompleted: Boolean,
    onCompletedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color.Green.copy(alpha = 0.1f)
            else Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                contentDescription = if (isCompleted) "Đã hoàn thành" else "Chưa hoàn thành",
                tint = if (isCompleted) Color.Green else Color.Red,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color.Green else MaterialTheme.colorScheme.onSurface
                )

                task.description?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCompleted) Color.Green.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Checkbox(
                checked = isCompleted,
                onCheckedChange = onCompletedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Green,
                    uncheckedColor = Color.Red
                )
            )
        }
    }
}

// Hàm định dạng thời gian
fun formatElapsedTime(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}