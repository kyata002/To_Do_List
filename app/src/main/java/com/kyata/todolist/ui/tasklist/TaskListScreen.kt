// File: ui/tasklist/TaskListScreen.kt
package com.kyata.todolist.ui.tasklist

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyata.todolist.data.model.Task
import com.kyata.todolist.ui.components.DisabledHardCoreAnimation
import com.kyata.todolist.ui.components.HardCoreAnimation
import com.kyata.todolist.ui.compose.DateHeader
import com.kyata.todolist.ui.compose.TaskItem
import com.kyata.todolist.ui.compose.groupTasksByDate
import com.kyata.todolist.ui.hardcoremode.HardCoreModeDialog
import com.kyata.todolist.ui.hardcoremode.HardCoreModeScreen
import kotlin.math.cos
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(1) } // 0=done, 1=active, 2=overdue
    var showHardCoreDialog by remember { mutableStateOf(false) }
    var hardCoreTasks by remember { mutableStateOf(emptyList<Task>()) }
    var showHardCoreScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkOverdueTasks()
    }

    val completedTasks by viewModel.completedTasks.collectAsState(initial = emptyList())
    val activeTasks by viewModel.activeTasks.collectAsState(initial = emptyList())
    val overdueTasks by viewModel.overdueTasks.collectAsState(initial = emptyList())

    LaunchedEffect(activeTasks.size, overdueTasks.size) {
        Log.d(
            "TaskListScreen",
            "Active tasks: ${activeTasks.size}, Overdue tasks: ${overdueTasks.size}"
        )
    }

    val tasks = when (selectedTab) {
        0 -> completedTasks
        1 -> activeTasks
        2 -> overdueTasks
        else -> activeTasks
    }

    val groupedTasks = remember(tasks) {
        groupTasksByDate(tasks)
    }

    // Animation values for background - Enhanced with more dynamic effects
    val hasActiveTasks = activeTasks.isNotEmpty() && selectedTab == 1
    val transition = updateTransition(hasActiveTasks, label = "backgroundTransition")

    // More vibrant warm colors for active state
    val backgroundColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 1200, easing = FastOutSlowInEasing) },
        label = "backgroundColor"
    ) { hasTasks ->
        if (hasTasks) {
            // More vibrant warm colors when there are active tasks
            Color(0xFFFFF5E1) // Warmer light amber
        } else {
            // Cool, calm color when no active tasks
            Color(0xFFF0F8FF) // Light blue for calm state
        }
    }

    val backgroundGradient by transition.animateColor(
        transitionSpec = { tween(durationMillis = 1200, easing = FastOutSlowInEasing) },
        label = "backgroundGradient"
    ) { hasTasks ->
        if (hasTasks) {
            // More vibrant warm gradient color
            Color(0xFFFFE0B2) // Vibrant amber
        } else {
            // Cool gradient color
            Color(0xFFE3F2FD) // Light blue gradient
        }
    }

    // Enhanced pulsing animation with multiple effects
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseEffect"
    )

    // Additional animation for floating particles when active
    val particleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "particleRotation"
    )

    if (showHardCoreScreen) {
        HardCoreModeScreen(
            tasks = hardCoreTasks,
            onBack = { showHardCoreScreen = false },
            onTaskCompleted = { taskId ->
                viewModel.markTaskAsCompleted(taskId)
            }
        )
        return
    }

    if (showHardCoreDialog) {
        HardCoreModeDialog(
            todayTasks = activeTasks,
            onDismiss = { showHardCoreDialog = false },
            onStartHardCore = { selectedTasks ->
                hardCoreTasks = selectedTasks
                showHardCoreDialog = false
                showHardCoreScreen = true
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kyata ToDo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (hasActiveTasks) {
                            Color(0xFFE65100) // Dark orange for active state
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* app icon */ }) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "App Icon",
                            tint = if (hasActiveTasks) {
                                Color(0xFFFF6D00) // Amber for active state
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (activeTasks.isNotEmpty()) {
                                showHardCoreDialog = true
                            }
                        },
                        enabled = activeTasks.isNotEmpty()
                    ) {
                        if (activeTasks.isNotEmpty()) {
                            HardCoreAnimation(
                                isActive = true,
                                modifier = Modifier.size(28.dp), // Slightly larger for emphasis
                            )
                        } else {
                            DisabledHardCoreAnimation(
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = if (hasActiveTasks) {
                                Color(0xFFE65100) // Dark orange for active state
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (hasActiveTasks) {
                        Color(0xFFFFF3E0).copy(alpha = 0.95f) // Warm background for active state
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    },
                    scrolledContainerColor = if (hasActiveTasks) {
                        Color(0xFFFFF3E0).copy(alpha = 0.95f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    },
                )
            )
        },
        floatingActionButton = {
            // Enhanced FAB with pulse effect when active
            val fabPulse by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "fabPulse"
            )

            FloatingActionButton(
                onClick = onAddTaskClick,
                modifier = Modifier
                    .size(if (hasActiveTasks) 64.dp else 56.dp)
                    .graphicsLayer {
                        scaleX = if (hasActiveTasks) fabPulse else 1f
                        scaleY = if (hasActiveTasks) fabPulse else 1f
                    },
                containerColor = if (hasActiveTasks) {
                    Color(0xFFFF6D00) // Vibrant orange for active state
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(if (hasActiveTasks) 32.dp else 24.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (hasActiveTasks) {
                    Color(0xFFFFF3E0).copy(alpha = 0.95f) // Warm background for active state
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                }
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Done",
                            tint = if (selectedTab == 0) {
                                if (hasActiveTasks) Color(0xFFFF6D00) else MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            "Hoàn thành",
                            color = if (selectedTab == 0) {
                                if (hasActiveTasks) Color(0xFFFF6D00) else MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Active",
                            tint = if (selectedTab == 1) {
                                if (hasActiveTasks) Color(0xFFFF3D00) else MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            "Đang làm",
                            color = if (selectedTab == 1) {
                                if (hasActiveTasks) Color(0xFFFF3D00) else MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Overdue",
                            tint = if (selectedTab == 2) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            "Quá hạn",
                            color = if (selectedTab == 2) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .drawWithCache {
                    // Draw dynamic background with particles when active
                    onDrawWithContent {
                        drawContent()
                        if (hasActiveTasks) {
                            drawParticles(particleRotation, pulseAlpha)
                        }
                    }
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(backgroundColor, backgroundGradient),
                        startY = 0f,
                        endY = 1500f
                    )
                )
        ) {
            // Enhanced pulsing overlay when there are active tasks
            if (hasActiveTasks) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x40FFA000).copy(alpha = pulseAlpha * 0.4f),
                                    Color(0x00FFA000)
                                ),
//                                center = Offset(size.width / 4, size.height / 4),
//                                radius = size.width * 0.8f
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Enhanced notification banner for active tasks
                if (activeTasks.isNotEmpty() && selectedTab == 1) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = if (hasActiveTasks) {
                            Color(0xFFFFECB3).copy(alpha = 0.95f) // Warm amber for active state
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        },
                        border = BorderStroke(
                            1.5.dp,
                            if (hasActiveTasks) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        ),
                        shadowElevation = if (hasActiveTasks) 8.dp else 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (activeTasks.isNotEmpty()) {
                                HardCoreAnimation(
                                    isActive = true,
                                    modifier = Modifier.size(28.dp),
                                )
                            } else {
                                DisabledHardCoreAnimation(
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Bấm vào biểu tượng trái tim để bắt đầu chế độ Hard Core",
                                color = if (hasActiveTasks) {
                                    Color(0xFFE65100) // Dark orange text for active state
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val (icon, text) = when (selectedTab) {
                                0 -> Pair(
                                    Icons.Filled.CheckCircle,
                                    "Chưa có công việc nào được hoàn thành"
                                )
                                1 -> Pair(
                                    Icons.Outlined.Add,
                                    "Chưa có công việc nào trong hôm nay"
                                )
                                2 -> Pair(
                                    Icons.Outlined.Add,
                                    "Tuyệt vời! Không có công việc nào quá hạn"
                                )
                                else -> Pair(Icons.Outlined.Add, "Không có công việc nào")
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 16.dp),
                                tint = if (hasActiveTasks) {
                                    Color(0xFFFFB300).copy(alpha = 0.7f) // Warm tint for active state
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (hasActiveTasks) {
                                    Color(0xFFE65100).copy(alpha = 0.8f) // Warm text color for active state
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        groupedTasks.forEach { (dateLabel, tasksForDate) ->
                            item {
                                DateHeader(
                                    dateLabel = dateLabel,
                                    textColor = if (hasActiveTasks) Color(0xFFE65100) else MaterialTheme.colorScheme.primary
                                )
                            }
                            items(tasksForDate) { task ->
                                TaskItem(
                                    task = task,
                                    onClick = { onTaskClick(task.id.toString()) }
                                )
                                Divider(
                                    modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Function to draw animated particles in the background
private fun DrawScope.drawParticles(rotation: Float, alpha: Float) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = size.width.coerceAtLeast(size.height) * 0.8f

    for (i in 0 until 15) {
        val angle = Math.toRadians((i * 24 + rotation).toDouble())
        val radius = maxRadius * (0.2f + 0.6f * (i % 5) / 5f)

        val x = centerX + (radius * cos(angle)).toFloat()
        val y = centerY + (radius * sin(angle)).toFloat()

        val size = 2f + 4f * (i % 3)
        val particleAlpha = alpha * (0.3f + 0.7f * (i % 4) / 4f)

        rotate(rotation, Offset(x, y)) {
            drawCircle(
                color = Color(0xFFFFB300).copy(alpha = particleAlpha),
                radius = size,
                center = Offset(x, y)
            )
        }
    }
}

// Add this extension to DateHeader composable
@Composable
fun DateHeader(dateLabel: String, textColor: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text = dateLabel,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
}