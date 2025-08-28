package com.kyata.todolist.ui.addtask

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kyata.todolist.data.model.Task
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableStateOf(System.currentTimeMillis() + 60 * 60 * 1000) }

    val calendar = Calendar.getInstance()

    fun pickDateTime(onResult: (Long) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(year, month, dayOfMonth, hour, minute, 0)
                        onResult(calendar.timeInMillis)
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Thêm công việc") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tên công việc") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(Modifier.height(16.dp))

            // Chọn thời gian bắt đầu
            Text("Bắt đầu: ${formatDateTime(startTime)}")
            Button(
                onClick = { pickDateTime { startTime = it } },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Chọn thời gian bắt đầu")
            }

            Spacer(Modifier.height(12.dp))

            // Chọn thời gian kết thúc
            Text("Kết thúc: ${formatDateTime(endTime)}")
            Button(
                onClick = { pickDateTime { endTime = it } },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Chọn thời gian kết thúc")
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val task = Task(
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            isCompleted = false,
                            startTime = startTime,
                            endTime = endTime
                        )
                        onSave(task)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Lưu")
            }
        }
    }
}

fun formatDateTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
