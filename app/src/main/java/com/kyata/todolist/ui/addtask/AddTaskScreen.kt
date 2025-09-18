package com.kyata.todolist.ui.addtask

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kyata.todolist.data.model.Task
import com.kyata.todolist.data.model.TaskPriority
import com.kyata.todolist.ui.compose.ModernDateTimePicker
import com.kyata.todolist.ui.compose.formatDateTime
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    viewModel: TaskViewModel
) {
    var title by remember { mutableStateOf("") }
    var descriptionState by remember {
        mutableStateOf(TextFieldValue(text = "- ", selection = TextRange(2)))
    }
    val context = LocalContext.current

    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableStateOf(System.currentTimeMillis() + 60 * 60 * 1000) }

    var expanded by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    // Trạng thái hiển thị dialog
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Thêm công việc") }) }
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
                value = descriptionState,
                onValueChange = { newValue ->
                    val oldValue = descriptionState

                    // Kiểm tra nếu người dùng vừa nhấn Enter
                    if (newValue.text.length > oldValue.text.length &&
                        newValue.selection.start > 0 &&
                        newValue.text[newValue.selection.start - 1] == '\n'
                    ) {
                        val insertPos = newValue.selection.start
                        val updatedText = StringBuilder(newValue.text)
                            .insert(insertPos, "- ")
                            .toString()

                        descriptionState = TextFieldValue(
                            text = updatedText,
                            selection = TextRange(insertPos + 2) // con trỏ sau "- "
                        )
                    } else {
                        // Xử lý trường hợp xóa hết nội dung
                        if (newValue.text.isEmpty()) {
                            descriptionState = TextFieldValue("- ", selection = TextRange(2))
                        } else {
                            descriptionState = newValue
                        }
                    }
                },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )


            Spacer(Modifier.height(16.dp))

            // Chọn thời gian bắt đầu
            Text("Bắt đầu: ${formatDateTime(startTime)}")
            Button(onClick = { showStartPicker = true }) {
                Text("Chọn thời gian bắt đầu")
            }

            if (showStartPicker) {
                ModernDateTimePicker(
                    initialMillis = startTime,
                    onConfirm = { picked ->
                        if (picked > System.currentTimeMillis()) {
                            startTime = picked
                        } else {
                            Toast.makeText(context, "Vui lòng chọn thời gian lớn hơn hiện tại", Toast.LENGTH_SHORT).show()
                        }
                        showStartPicker = false
                    },
                    onDismiss = { showStartPicker = false }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Chọn thời gian kết thúc
            Text("Kết thúc: ${formatDateTime(endTime)}")
            Button(onClick = { showEndPicker = true }) {
                Text("Chọn thời gian kết thúc")
            }

            if (showEndPicker) {
                ModernDateTimePicker(
                    initialMillis = endTime,
                    onConfirm = { picked ->
                        if (picked > startTime) {
                            endTime = picked
                        } else {
                            Toast.makeText(context, "Thời gian kết thúc phải sau thời gian bắt đầu", Toast.LENGTH_SHORT).show()
                        }
                        showEndPicker = false
                    },
                    onDismiss = { showEndPicker = false }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Dropdown chọn độ ưu tiên
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = priority.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Độ ưu tiên") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    TaskPriority.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                priority = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val task = Task(
                            title = title,
                            description = descriptionState.text.takeIf { it.isNotBlank() },
                            isCompleted = false,
                            startTime = startTime,
                            endTime = endTime,
                            priority = priority // ✅ thêm độ ưu tiên
                        )
                        viewModel.addTask(task,context)
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


