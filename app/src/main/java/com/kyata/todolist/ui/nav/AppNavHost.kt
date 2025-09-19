package com.kyata.todolist.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kyata.todolist.ui.tasklist.TaskListScreen
import com.kyata.todolist.ui.addtask.AddTaskScreen
import com.kyata.todolist.ui.addtask.AddTaskViewModel
import com.kyata.todolist.ui.taskdetail.TaskDetailScreen
import com.kyata.todolist.ui.tasklist.TaskListViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    taskListViewModel: TaskListViewModel,
    addTaskViewModel: AddTaskViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen(
                viewModel = taskListViewModel, // Pass the correct ViewModel
                onAddTaskClick = { navController.navigate("add_task") },
                onTaskClick = { taskId -> navController.navigate("task_detail/${taskId.toString()}") },
                onSettingsClick = {}
            )
        }
        composable("add_task") {
            AddTaskScreen(
                onBack = { navController.popBackStack() },
                viewModel = addTaskViewModel
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

