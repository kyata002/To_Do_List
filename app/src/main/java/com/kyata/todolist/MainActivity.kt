package com.kyata.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kyata.todolist.ui.tasklist.TaskListScreen
import com.kyata.todolist.ui.addtask.AddTaskScreen
import com.kyata.todolist.ui.taskdetail.TaskDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoFocusApp()
        }
    }
}

@Composable
fun TodoFocusApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen(
                onAddTaskClick = { navController.navigate("add_task") },
                onTaskClick = { taskId -> navController.navigate("task_detail/$taskId") },
                onSettingsClick = {}
            )
        }
        composable("add_task") {
            AddTaskScreen(
                onBack = { navController.popBackStack() },
                onSave = { task ->
                    // TODO: gọi ViewModel để insert DB
                    println("Task mới: $task")
                }
            )
        }


        composable("task_detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
