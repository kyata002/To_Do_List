package com.kyata.todolist.ui

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyata.todolist.AppDatabase
import com.kyata.todolist.R
import com.kyata.todolist.ui.memory.model.MemoryUsage
import com.kyata.todolist.ui.memory.repository.MemoryRepository
import com.kyata.todolist.ui.memory.repository.MemoryViewModelFactory
import com.kyata.todolist.ui.memory.repository.MemoryWorker
import com.kyata.todolist.ui.memory.viewmodel.MemoryViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).memoryUsageDao() }
    val repo = remember { MemoryRepository(dao, context) }
    val factory = remember { MemoryViewModelFactory(repo) }

    val viewModel: MemoryViewModel = viewModel(factory = factory)
    val usageData by viewModel.usageData.collectAsState()
    val realtimeMemoryInfo by viewModel.realtimeMemoryInfo.collectAsState()
    val realtimeProcMemInfo by viewModel.realtimeProcMemInfo.collectAsState()
    val historicalData by viewModel.historicalData.collectAsState()
    val availableDays by viewModel.availableDays.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()

    // Bắt đầu cập nhật realtime khi screen được hiển thị
    LaunchedEffect(Unit) {
        MemoryWorker.schedulePeriodicWork(context)
        viewModel.loadAvailableDays()
        viewModel.startRealtimeUpdates(1000)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRealtimeUpdates()
        }
    }

    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = remember { ActivityManager.MemoryInfo().apply { activityManager.getMemoryInfo(this) } }

    // Sử dụng realtime data thay vì static data
    val totalRamMb = realtimeMemoryInfo?.totalRamMb?.toLong() ?: (memoryInfo.totalMem / 1024 / 1024)
    val usedRamMb = realtimeMemoryInfo?.usedRamMb?.toLong() ?: (totalRamMb - memoryInfo.availMem / 1024 / 1024)
    val usedPercent = realtimeMemoryInfo?.usedPercent ?: (usedRamMb.toFloat() / totalRamMb * 100)

    val swapTotalKb = realtimeProcMemInfo["SwapTotal"]?.split(" ")?.get(0)?.toIntOrNull() ?: 0
    val swapFreeKb = realtimeProcMemInfo["SwapFree"]?.split(" ")?.get(0)?.toIntOrNull() ?: 0
    val swapTotalMb = swapTotalKb / 1024
    val swapFreeMb = swapFreeKb / 1024
    val swapUsedMb = swapTotalMb - swapFreeMb

    val cachedKb = realtimeProcMemInfo["Cached"]?.split(" ")?.get(0)?.toIntOrNull() ?: 0
    val cachedMb = cachedKb / 1024
    val buffersKb = realtimeProcMemInfo["Buffers"]?.split(" ")?.get(0)?.toIntOrNull() ?: 0
    val buffersMb = buffersKb / 1024

    val appMemUsageKb = remember { getAppMemoryUsage(context) }

    val memoryDetails = listOf(
        "Total RAM" to "$totalRamMb MB",
        "Used RAM" to "$usedRamMb MB",
        "Available RAM" to "${totalRamMb - usedRamMb} MB",
        "Usage" to "${String.format("%.1f", usedPercent)} %",
        "App Memory Usage" to "${appMemUsageKb / 1024} MB",
        "Cached" to "${cachedMb} MB",
        "Buffers" to "${buffersMb} MB",
        "Swap Total" to "${swapTotalMb} MB",
        "Swap Used" to "${swapUsedMb} MB",
        "Swap Free" to "${swapFreeMb} MB",
        "Low Memory?" to memoryInfo.lowMemory.toString(),
        "Threshold" to "${memoryInfo.threshold / 1024 / 1024} MB",
        "Android Version" to Build.VERSION.RELEASE,
        "SDK Level" to Build.VERSION.SDK_INT.toString(),
        "Device Model" to Build.MODEL,
        "Manufacturer" to Build.MANUFACTURER
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Memory Details", color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Indicator realtime luôn hiển thị
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Dropdown chọn ngày
                    if (availableDays.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }

                        Box {
                            TextButton(
                                onClick = { expanded = true },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text(selectedDay ?: "Select day")
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select day"
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                availableDays.forEach { day ->
                                    DropdownMenuItem(
                                        text = { Text(day) },
                                        onClick = {
                                            viewModel.loadHistoricalDataForDay(day)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        DynamicGalaxyBackground {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Memory Information (Live)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    Text(
                        text = "Memory History - ${selectedDay ?: "Today"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    MemoryHistoryChart(
                        historicalData = historicalData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Text(
                        text = "Each column represents memory usage at 30-minute intervals",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Hiển thị historical data với animation
                items(usageData) { usage ->
                    val animatedHeight by animateFloatAsState(
                        targetValue = usage.usedPercent / 100f,
                        animationSpec = tween(800)
                    )
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .width(20.dp)
                            .background(Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(animatedHeight)
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color(0xFF4CAF50))
                        )
                    }
                }

                items(memoryDetails) { (key, value) ->
                    MemoryInfoCard(title = key, value = value)
                }

                // Progress bar RAM với hiệu ứng realtime
                item {
                    val animatedRamProgress by animateFloatAsState(
                        targetValue = usedPercent / 100f,
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                        label = "ramProgress"
                    )

                    val ramColor = when {
                        usedPercent < 70 -> Color(0xFF4CAF50)
                        usedPercent < 85 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_memory),
                                contentDescription = "RAM",
                                tint = ramColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RAM USAGE • LIVE",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${String.format("%.1f", usedPercent)}%",
                                color = ramColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedRamProgress)
                                    .height(6.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                ramColor,
                                                ramColor.copy(alpha = 0.8f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )

                            Canvas(modifier = Modifier.matchParentSize()) {
                                if (animatedRamProgress > 0) {
                                    drawRect(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                ramColor.copy(alpha = 0.4f),
                                                Color.Transparent
                                            ),
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width * animatedRamProgress, 0f)
                                        ),
                                        topLeft = Offset(0f, 0f),
                                        size = Size(size.width * animatedRamProgress, size.height)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "$usedRamMb / $totalRamMb MB",
                            modifier = Modifier.padding(top = 4.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Progress bar Swap với hiệu ứng realtime
                if (swapTotalMb > 0) {
                    item {
                        val swapUsagePercent = if (swapTotalMb > 0) swapUsedMb.toFloat() / swapTotalMb * 100 else 0f
                        val animatedSwapProgress by animateFloatAsState(
                            targetValue = swapUsagePercent / 100f,
                            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                            label = "swapProgress"
                        )

                        val swapColor = when {
                            swapUsagePercent < 70 -> Color(0xFF00BCD4)
                            else -> Color(0xFFFF9800)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_swap),
                                    contentDescription = "Swap",
                                    tint = swapColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SWAP USAGE • LIVE",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${String.format("%.1f", swapUsagePercent)}%",
                                    color = swapColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(
                                        color = Color.Gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedSwapProgress)
                                        .height(6.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    swapColor,
                                                    swapColor.copy(alpha = 0.8f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(3.dp)
                                        )
                                )

                                Canvas(modifier = Modifier.matchParentSize()) {
                                    if (animatedSwapProgress > 0) {
                                        drawRect(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    swapColor.copy(alpha = 0.4f),
                                                    Color.Transparent
                                                ),
                                                start = Offset(0f, 0f),
                                                end = Offset(size.width * animatedSwapProgress, 0f)
                                            ),
                                            topLeft = Offset(0f, 0f),
                                            size = Size(size.width * animatedSwapProgress, size.height)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "$swapUsedMb / $swapTotalMb MB",
                                modifier = Modifier.padding(top = 4.dp),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryInfoCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getAppMemoryUsage(context: Context): Int {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val pid = android.os.Process.myPid()
    val memoryInfoArray = am.getProcessMemoryInfo(intArrayOf(pid))
    return memoryInfoArray[0].totalPss // KB
}

/* ==== Helper functions ==== */
fun getMemInfoFromProc(): Map<String, String> {
    val memInfo = mutableMapOf<String, String>()
    try {
        val reader = File("/proc/meminfo").bufferedReader()
        reader.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(Regex("\\s+"))
                if (parts.size >= 2) {
                    val key = parts[0].removeSuffix(":")
                    val value = parts[1] + " kB"
                    memInfo[key] = value
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return memInfo
}
// MemoryHistoryChart.kt
@Composable
fun MemoryHistoryChart(
    historicalData: List<MemoryUsage>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Cuộn đến cuối khi có dữ liệu mới
    LaunchedEffect(historicalData.size) {
        if (historicalData.isNotEmpty()) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        if (historicalData.isEmpty()) {
            Text(
                text = "No data available",
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                historicalData.forEach { data ->
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(data.timestamp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.width(40.dp)
                    ) {
                        // Hiển thị phần trăm
                        Text(
                            text = "${data.usedPercent.toInt()}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // Cột biểu đồ
                        val barHeight by animateFloatAsState(
                            targetValue = data.usedPercent / 100f,
                            animationSpec = tween(durationMillis = 500),
                            label = "barAnimation"
                        )

                        val barColor = when {
                            data.usedPercent < 70 -> Color(0xFF4CAF50)
                            data.usedPercent < 85 -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }

                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(barHeight)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(barColor, barColor.copy(alpha = 0.8f))
                                    ),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )

                        // Hiển thị thời gian
                        Text(
                            text = time,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Thêm trục Y (phần trăm)
            Canvas(modifier = Modifier.matchParentSize()) {
                // Vẽ trục Y
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2f
                )

                // Vẽ các mốc phần trăm
                val percentages = listOf(0f, 25f, 50f, 75f, 100f)
                percentages.forEach { percent ->
                    val y = size.height * (1 - percent / 100f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "${percent.toInt()}%",
                            5f,
                            y - 5f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 20f
                                alpha = 128
                            }
                        )
                    }
                }
            }
        }
    }
}

