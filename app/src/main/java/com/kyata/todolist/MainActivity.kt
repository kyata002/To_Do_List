package com.kyata.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.kyata.todolist.ui.addtask.AddTaskViewModel
import com.kyata.todolist.ui.nav.AppNavHost
import com.kyata.todolist.ui.tasklist.TaskListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as TodoApp).repository
        val addTaskViewModel = AddTaskViewModel(repository)
        val taskListViewModel = TaskListViewModel(repository)

        setContent {
            TodoFocusApp(
                addTaskViewModel = addTaskViewModel,
                taskListViewModel = taskListViewModel
            )
        }
    }
}

@Composable
fun TodoFocusApp(
    addTaskViewModel: AddTaskViewModel,
    taskListViewModel: TaskListViewModel
) {
    val navController = rememberNavController()
    AppNavHost(
        navController = navController,
        taskListViewModel = taskListViewModel,
        addTaskViewModel = addTaskViewModel
    )
}