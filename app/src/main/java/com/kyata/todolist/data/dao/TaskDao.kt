package com.kyata.todolist.data.dao

import androidx.room.*
import com.kyata.todolist.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // ✅ lấy tất cả task (bao gồm active + overdue)
    @Query("SELECT * FROM tasks ORDER BY startTime DESC")
    fun getAllTasks(): Flow<List<Task>>

    // ✅ chỉ task đang hoạt động (tab All)
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isOverdue = 0 ORDER BY priority DESC, startTime DESC")
    fun getActiveTasks(): Flow<List<Task>>

    // ✅ chỉ task overdue (tab Overdue)
    @Query("SELECT * FROM tasks WHERE isOverdue = 1 ORDER BY endTime ASC")
    fun getOverdueTasks(): Flow<List<Task>>

    // ✅ chỉ task đã hoàn thành (tab Done)
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY endTime DESC")
    fun getCompletedTasks(): Flow<List<Task>>

    // ✅ lấy task trong khoảng thời gian
    @Query("SELECT * FROM tasks WHERE startTime >= :startTime AND startTime < :endTime ORDER BY startTime DESC")
    fun getTasksInDateRange(startTime: Long, endTime: Long): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // ✅ Đánh dấu task là quá hạn
    @Query("UPDATE tasks SET isOverdue = 1 WHERE id = :taskId")
    suspend fun markOverdue(taskId: Long)

    // ✅ Đánh dấu task là đã hoàn thành
    @Query("UPDATE tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Long)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    // ✅ Lấy task cần đánh dấu là quá hạn
    @Query("SELECT * FROM tasks WHERE endTime < :currentTime AND isCompleted = 0 AND isOverdue = 0")
    suspend fun getTasksToMarkOverdue(currentTime: Long): List<Task>

    // ✅ Cập nhật chế độ Hard Core cho task
    @Query("UPDATE tasks SET isHardCore = :isHardCore WHERE id = :taskId")
    suspend fun updateTaskHardCoreMode(taskId: Long, isHardCore: Boolean)

    // ✅ Cập nhật chế độ Hard Core cho nhiều task trong khoảng thời gian
    @Query("UPDATE tasks SET isHardCore = :isHardCore WHERE startTime >= :startTime AND startTime < :endTime")
    suspend fun updateHardCoreModeForDateRange(startTime: Long, endTime: Long, isHardCore: Boolean)

    // ✅ Kiểm tra xem có task Hard Core nào trong khoảng thời gian không
    @Query("SELECT COUNT(*) FROM tasks WHERE isHardCore = 1 AND startTime >= :startTime AND startTime < :endTime")
    suspend fun hasHardCoreTasksInRange(startTime: Long, endTime: Long): Boolean
}