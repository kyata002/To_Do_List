package com.kyata.todolist.data.dao
import androidx.room.*
import com.kyata.todolist.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // Tất cả task
    @Query("SELECT * FROM tasks ORDER BY startTime ASC")
    fun getAllTasks(): Flow<List<Task>>

    // Task đang làm: chưa done và chưa overdue
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isOverdue = 0 ORDER BY startTime ASC")
    fun getActiveTasks(): Flow<List<Task>>

    // Task quá hạn: chưa done nhưng đã quá hạn
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isOverdue = 1 ORDER BY startTime ASC")
    fun getOverdueTasks(): Flow<List<Task>>

    // Task đã hoàn thành (Done)
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY startTime ASC")
    fun getCompletedTasks(): Flow<List<Task>> // Thêm method mới

    // Đánh dấu 1 task là overdue
    @Query("UPDATE tasks SET isOverdue = 1 WHERE id = :taskId")
    suspend fun markOverdue(taskId: Long)

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isOverdue = 0 AND endTime < :currentTime")
    suspend fun getTasksToMarkOverdue(currentTime: Long): List<Task>
}
