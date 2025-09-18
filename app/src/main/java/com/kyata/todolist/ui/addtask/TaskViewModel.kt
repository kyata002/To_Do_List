package com.kyata.todolist.ui.addtask

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kyata.todolist.data.TaskRepository
import com.kyata.todolist.data.model.Task
import com.kyata.todolist.ui.tasklist.CheckOverdueWorker
import com.kyata.todolist.ui.tasklist.MarkOverdueWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val allTasks: Flow<List<Task>> = repository.getAllTasks()
    val activeTasks: Flow<List<Task>> = repository.getActiveTasks()
    val overdueTasks: Flow<List<Task>> = repository.getOverdueTasks()
    val completedTasks: Flow<List<Task>> = repository.getCompletedTasks()

    init {
        // Kiểm tra mỗi 6 giây
        viewModelScope.launch {
            while (true) {
                checkOverdueTasks()
                delay(1000) // 6 giây
            }
        }
    }
    fun checkOverdueTasks() {
        viewModelScope.launch {
            repository.checkAndMarkOverdueTasks()
        }
    }

    fun addTask(task: Task, context: Context) {
        viewModelScope.launch {
            val id = repository.insertTask(task)
            val newTask = task.copy(id = id)

            // Schedule work cho task cụ thể này
            scheduleOverdueWork(context, newTask)

            // Và schedule periodic work để kiểm tra tổng thể
            schedulePeriodicOverdueCheck(context)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    private fun scheduleOverdueWork(context: Context, task: Task) {
        task.endTime.let { endTime ->
            val delay = endTime - System.currentTimeMillis()
            Log.d("ScheduleWork", "Scheduling task ${task.id} in $delay ms")

            if (delay > 0) {
                val workRequest = OneTimeWorkRequestBuilder<MarkOverdueWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf("taskId" to task.id))
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
                Log.d("ScheduleWork", "Scheduled one-time work for task ${task.id}")
            } else {
                // Nếu task đã quá hạn ngay khi tạo, mark overdue luôn
                Log.d("ScheduleWork", "Task ${task.id} is already overdue, marking now")
                viewModelScope.launch {
                    repository.markOverdue(task.id)
                }
            }
        }
    }

    fun schedulePeriodicOverdueCheck(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<CheckOverdueWorker>(
            1/60, TimeUnit.MINUTES // Kiểm tra mỗi 15 phút
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "overdueCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        Log.d("ScheduleWork", "Scheduled periodic overdue check")
    }
}