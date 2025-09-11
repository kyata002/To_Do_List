package com.kyata.todolist.ui

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyata.todolist.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetailScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    // Thông tin cơ bản từ ActivityManager
    val memoryInfo = remember {
        ActivityManager.MemoryInfo().apply {
            activityManager.getMemoryInfo(this)
        }
    }

    // Thông tin chi tiết từ /proc/meminfo
    val procMemInfo = remember { getMemInfoFromProc() }

    // Bộ nhớ app hiện tại sử dụng
    val appMemUsageKb = remember { getAppMemoryUsage(context) }

    val totalRamMb = memoryInfo.totalMem / 1024 / 1024
    val availRamMb = memoryInfo.availMem / 1024 / 1024
    val usedRamMb = totalRamMb - availRamMb
    val usedPercent = (usedRamMb.toFloat() / totalRamMb * 100)

    val swapTotalMb = procMemInfo["SwapTotal"]?.split(" ")?.get(0)?.toIntOrNull()?.div(1024) ?: 0
    val swapFreeMb = procMemInfo["SwapFree"]?.split(" ")?.get(0)?.toIntOrNull()?.div(1024) ?: 0
    val swapUsedMb = swapTotalMb - swapFreeMb

    val cachedMb = procMemInfo["Cached"]?.split(" ")?.get(0)?.toIntOrNull()?.div(1024) ?: 0
    val buffersMb = procMemInfo["Buffers"]?.split(" ")?.get(0)?.toIntOrNull()?.div(1024) ?: 0

    val memoryDetails = listOf(
        "Total RAM" to "$totalRamMb MB",
        "Used RAM" to "$usedRamMb MB",
        "Available RAM" to "$availRamMb MB",
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
                title = { Text("Memory Details", color = Color.White) },
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
                        text = "Memory Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(memoryDetails) { (key, value) ->
                    MemoryInfoCard(title = key, value = value)
                }

                // Progress bar RAM với hiệu ứng hiện đại
                item {
                    val animatedRamProgress by animateFloatAsState(
                        targetValue = usedPercent / 100f,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        label = "ramProgress"
                    )

                    val ramColor = when {
                        usedPercent < 70 -> Color(0xFF4CAF50) // Xanh lá
                        usedPercent < 85 -> Color(0xFFFFC107) // Vàng
                        else -> Color(0xFFF44336)             // Đỏ
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Header với icon và label
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
                                text = "RAM USAGE",
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

                        // Progress bar với glow effect
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

                            // Glow effect
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

                        // Usage details
                        // Usage details
                        Text(
                            text = "$usedRamMb / $totalRamMb MB",
                            modifier = Modifier.padding(top = 4.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )

                    }
                }

// Progress bar Swap với hiệu ứng hiện đại
                if (swapTotalMb > 0) {
                    item {
                        val animatedSwapProgress by animateFloatAsState(
                            targetValue = swapUsedMb.toFloat() / swapTotalMb,
                            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                            label = "swapProgress"
                        )

                        val swapColor = when {
                            swapUsedMb.toFloat() / swapTotalMb < 0.7 -> Color(0xFF00BCD4) // Xanh cyan
                            else -> Color(0xFFFF9800) // Cam
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // Header với icon và label
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
                                    text = "SWAP USAGE",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${String.format("%.1f", swapUsedMb.toFloat() / swapTotalMb * 100)}%",
                                    color = swapColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Progress bar với glow effect
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

                                // Glow effect
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

                            // Usage details
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
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

fun getAppMemoryUsage(context: Context): Int {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val pid = android.os.Process.myPid()
    val memoryInfoArray = am.getProcessMemoryInfo(intArrayOf(pid))
    return memoryInfoArray[0].totalPss // KB
}
