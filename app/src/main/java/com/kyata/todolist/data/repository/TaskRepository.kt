package com.kyata.todolist.data.repository

import android.util.Log
import com.kyata.todolist.data.dao.TaskDao
import com.kyata.todolist.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    // ✅ lấy tất cả task (bao gồm active + overdue)
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    // ✅ chỉ task đang hoạt động (tab All)
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()

    // ✅ chỉ task overdue (tab Overdue)
    fun getOverdueTasks(): Flow<List<Task>> = taskDao.getOverdueTasks()

    // ✅ chỉ task đã hoàn thành (tab Done)
    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()

    // ✅ lấy task trong khoảng thời gian
    fun getTasksInDateRange(startTime: Long, endTime: Long): Flow<List<Task>> =
        taskDao.getTasksInDateRange(startTime, endTime)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun markOverdue(taskId: Long) = taskDao.markOverdue(taskId)

    suspend fun getTaskById(taskId: Long) = taskDao.getTaskById(taskId)

    // ✅ Cập nhật chế độ Hard Core cho task
    suspend fun updateTaskHardCoreMode(taskId: Long, isHardCore: Boolean) {
        taskDao.updateTaskHardCoreMode(taskId, isHardCore)
    }

    // ✅ Cập nhật chế độ Hard Core cho nhiều task trong khoảng thời gian
    suspend fun updateHardCoreModeForDateRange(startTime: Long, endTime: Long, isHardCore: Boolean) {
        taskDao.updateHardCoreModeForDateRange(startTime, endTime, isHardCore)
    }

    // ✅ Kiểm tra xem có task Hard Core nào trong khoảng thời gian không
    suspend fun hasHardCoreTasksInRange(startTime: Long, endTime: Long): Boolean {
        return taskDao.hasHardCoreTasksInRange(startTime, endTime)
    }

    suspend fun checkAndMarkOverdueTasks() {
        val currentTime = System.currentTimeMillis()
        val tasksToMark = taskDao.getTasksToMarkOverdue(currentTime)

        tasksToMark.forEach { task ->
            taskDao.markOverdue(task.id)
            Log.d("OverdueCheck", "Marked task ${task.id} as overdue")
        }
    }

    // ✅ Đánh dấu task là đã hoàn thành
    suspend fun markTaskAsCompleted(taskId: Long) {
        taskDao.markTaskAsCompleted(taskId)
    }
}