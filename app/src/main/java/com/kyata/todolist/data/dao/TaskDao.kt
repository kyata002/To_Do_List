package com.kyata.todolist.data.dao
import androidx.room.*
import com.kyata.todolist.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks ORDER BY startTime ASC")
    fun getAllTasks(): Flow<List<Task>>
}