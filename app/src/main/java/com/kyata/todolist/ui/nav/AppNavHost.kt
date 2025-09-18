package com.kyata.todolist.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kyata.todolist.ui.tasklist.TaskListScreen
import com.kyata.todolist.ui.addtask.AddTaskScreen
import com.kyata.todolist.ui.addtask.TaskViewModel
import com.kyata.todolist.ui.taskdetail.TaskDetailScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    taskViewModel: TaskViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen(
                viewModel = taskViewModel,
                onAddTaskClick = { navController.navigate("add_task") },
                onTaskClick = { taskId -> navController.navigate("task_detail/$taskId") },
                onSettingsClick = {}
            )
        }
        composable("add_task") {
            AddTaskScreen(
                onBack = { navController.popBackStack() },
                viewModel = taskViewModel
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

