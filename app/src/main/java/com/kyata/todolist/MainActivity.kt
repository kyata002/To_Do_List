package com.kyata.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.kyata.todolist.ui.addtask.TaskViewModel
import com.kyata.todolist.ui.nav.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as TodoApp).repository
        val taskViewModel = TaskViewModel(repository)

        setContent {
            TodoFocusApp(taskViewModel)
        }
    }
}

@Composable
fun TodoFocusApp(taskViewModel: TaskViewModel) {
    val navController = rememberNavController()
    AppNavHost(navController = navController, taskViewModel = taskViewModel)
}
