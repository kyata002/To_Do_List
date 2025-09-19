// File: ui/tasklist/TaskListViewModel.kt
package com.kyata.todolist.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyata.todolist.data.model.Task
import com.kyata.todolist.data.model.TaskPriority
import com.kyata.todolist.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    // State flows
    private val _selectedTab = MutableStateFlow(1) // 0=done, 1=active, 2=overdue
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _completedTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _activeTasks = MutableStateFlow<List<Task>>(emptyList())
    private val _overdueTasks = MutableStateFlow<List<Task>>(emptyList())

    val completedTasks: StateFlow<List<Task>> = _completedTasks.asStateFlow()
    val activeTasks: StateFlow<List<Task>> = _activeTasks.asStateFlow()
    val overdueTasks: StateFlow<List<Task>> = _overdueTasks.asStateFlow()

    private val _currentTasks = MutableStateFlow<List<Task>>(emptyList())
    val currentTasks: StateFlow<List<Task>> = _currentTasks.asStateFlow()

    // Comparator để sắp xếp tasks theo priority và thời gian
    private val taskComparator = Comparator<Task> { task1, task2 ->
        // So sánh theo priority (HIGH > MEDIUM > LOW)
        val priorityOrder = mapOf(
            TaskPriority.HIGH to 2,
            TaskPriority.MEDIUM to 1,
            TaskPriority.LOW to 0,
        )

        val priority1 = priorityOrder[task1.priority] ?: 3
        val priority2 = priorityOrder[task2.priority] ?: 3

        // So sánh priority (số nhỏ hơn = priority cao hơn)
        val priorityCompare = priority1.compareTo(priority2)

        if (priorityCompare != 0) {
            // Trả về -priorityCompare để HIGH lên đầu
            -priorityCompare
        } else {
            // Nếu cùng priority, sắp xếp theo thời gian (sớm nhất trước)
            task1.endTime.compareTo(task2.endTime)
        }
    }

    init {
        loadAllTasks()
        checkOverdueTasks()
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
        updateCurrentTasks()
    }

    private fun loadAllTasks() {
        viewModelScope.launch {
            taskRepository.getAllTasks().collect { allTasks ->
                // Sắp xếp tasks theo priority và thời gian
                val sortedTasks = allTasks.sortedWith(taskComparator)

                // Phân loại tasks
                _completedTasks.value = sortedTasks.filter { it.isCompleted }
                _activeTasks.value = sortedTasks.filter { !it.isCompleted && !it.isOverdue }
                _overdueTasks.value = sortedTasks.filter { it.isOverdue }
                updateCurrentTasks()
            }
        }
    }

    private fun updateCurrentTasks() {
        _currentTasks.value = when (_selectedTab.value) {
            0 -> _completedTasks.value
            1 -> _activeTasks.value
            2 -> _overdueTasks.value
            else -> _activeTasks.value
        }
    }

    fun checkOverdueTasks() {
        viewModelScope.launch {
            val now = Date().time
            val activeTasks = _activeTasks.value

            activeTasks.forEach { task ->
                if (task.endTime < now && !task.isCompleted && !task.isOverdue) {
                    // Cập nhật task thành quá hạn
                    val updatedTask = task.copy(isOverdue = true)
                    taskRepository.updateTask(updatedTask)
                }
            }

            // Reload tasks sau khi cập nhật
            loadAllTasks()
        }
    }

    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            task?.let {
                val updatedTask = it.copy(isCompleted = !it.isCompleted)
                taskRepository.updateTask(updatedTask)
                loadAllTasks() // Reload để cập nhật UI
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId)
            task?.let {
                taskRepository.deleteTask(it)
                loadAllTasks() // Reload để cập nhật UI
            }
        }
    }

    fun refreshTasks() {
        loadAllTasks()
        checkOverdueTasks()
    }
}