package com.kyata.todolist.data

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

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun markOverdue(taskId: Long) = taskDao.markOverdue(taskId)

    suspend fun checkAndMarkOverdueTasks() {
        val currentTime = System.currentTimeMillis()
        val tasksToMark = taskDao.getTasksToMarkOverdue(currentTime)

        tasksToMark.forEach { task ->
            taskDao.markOverdue(task.id)
            Log.d("OverdueCheck", "Marked task ${task.id} as overdue")
        }
    }

}
