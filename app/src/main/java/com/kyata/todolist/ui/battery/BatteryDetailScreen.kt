package com.kyata.todolist.ui.battery

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyata.todolist.ui.battery.model.BatteryHistory
import kotlin.math.sin


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryDetailScreen(onBack: () -> Unit, viewModel: BatteryViewModel = viewModel()) {

    val context = LocalContext.current
    val batteryManager = remember {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val history by viewModel.batteryHistory.collectAsState()

    LaunchedEffect(Unit) { viewModel.startListening() }

    val capacityMah = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
    } else 0

    val batteryDetails = listOf(
        "Battery Level" to "${batteryStatus?.percentage ?: 0}%",
        "Status" to (batteryStatus?.status ?: "Unknown"),
        "Power Source" to (batteryStatus?.plugged ?: "Unplugged"),
        "Temperature" to "${batteryStatus?.temperature ?: 0f} °C",
        "Voltage" to if ((batteryStatus?.voltage ?: -1) > 0) "${batteryStatus?.voltage} mV" else "N/A",
        "Technology" to (batteryStatus?.technology ?: "Unknown"),
        "Capacity (approx)" to if (capacityMah > 0) "$capacityMah mAh" else "N/A",
        "Android Version" to Build.VERSION.RELEASE,
        "SDK Level" to Build.VERSION.SDK_INT.toString(),
        "Device Model" to Build.MODEL,
        "Manufacturer" to Build.MANUFACTURER
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Battery Details", color = Color.White) },
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
        _root_ide_package_.com.kyata.todolist.ui.DynamicGalaxyBackground {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Battery Information",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    BatteryWaveChart(
                        batteryPercentage = (batteryStatus?.percentage ?: 0) / 100f,
                        isCharging = batteryStatus?.isCharging ?: false,
                        history = history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                items(batteryDetails) { (key, value) ->
                    BatteryInfoCard(title = key, value = value)
                }
            }
        }
    }
}

@Composable
fun BatteryWaveChart(
    batteryPercentage: Float,
    isCharging: Boolean,
    history: List<BatteryHistory>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val batteryColor = when {
        batteryPercentage > 0.7f -> Color(0xFF4CAF50)
        batteryPercentage > 0.3f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Box(
        modifier = modifier
            .background(
                color = Color(0x1AFFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Vẽ grid
            drawGrid(width, height)

            // Vẽ lịch sử pin
            if (history.isNotEmpty()) {
                drawBatteryHistory(width, height, history)
            }

            if (isCharging) {
                // Khi sạc: vẽ sóng động và điểm hiện tại
                drawAnimatedWave(width, height, wavePhase, batteryColor.copy(alpha = 0.6f))
                drawCurrentPoint(width, height, batteryPercentage, batteryColor, wavePhase)
            } else {
                // Khi không sạc: chỉ vẽ đường mức pin tĩnh
                drawBatteryLevelLine(width, height, batteryPercentage, batteryColor)
            }
        }


        // Hiển thị giá trị phần trăm
        Text(
            text = "${(batteryPercentage * 100).toInt()}%",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )

        // Hiển thị trạng thái sạc
        Text(
            text = if (isCharging) "Charging" else "Not Charging",
            color = if (isCharging) Color.Green else Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}

private fun DrawScope.drawGrid(width: Float, height: Float) {
    // Vẽ grid lines
    for (i in 0..10) {
        val y = height * i / 10f
        drawLine(
            color = Color.White.copy(alpha = 0.1f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Vẽ trục
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(0f, height),
        end = Offset(width, height),
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawBatteryLevelLine(
    width: Float,
    height: Float,
    batteryPercentage: Float,
    color: Color
) {
    val levelY = height * (1 - batteryPercentage)

    drawLine(
        color = color.copy(alpha = 0.8f),
        start = Offset(0f, levelY),
        end = Offset(width, levelY),
        strokeWidth = 3.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
    )
}

private fun DrawScope.drawAnimatedWave(
    width: Float,
    height: Float,
    phase: Float,
    color: Color
) {
    val path = Path()
    val points = 50
    val amplitude = height * 0.15f

    path.moveTo(0f, height / 2)

    for (i in 0..points) {
        val x = width * i / points.toFloat()
        val y = height / 2 + sin(x / width * 4 * Math.PI + phase.toDouble()).toFloat() * amplitude

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.quadraticBezierTo(
                x1 = width * (i - 0.5f) / points,
                y1 = height / 2 + sin(width * (i - 0.5f) / width * 4 * Math.PI + phase.toDouble()).toFloat() * amplitude,
                x2 = x,
                y2 = y
            )
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawCurrentPoint(
    width: Float,
    height: Float,
    batteryPercentage: Float,
    color: Color,
    phase: Float
) {
    val currentX = width * 0.7f // Vị trí điểm hiện tại
    val waveY = height / 2 + sin(currentX / width * 4 * Math.PI + phase.toDouble()).toFloat() * height * 0.15f
    val levelY = height * (1 - batteryPercentage)

    // Vẽ điểm giao nhau
    val pointRadius = 8.dp.toPx()
    drawCircle(
        color = color,
        center = Offset(currentX, waveY),
        radius = pointRadius
    )

    // Hiệu ứng glow xung quanh điểm
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.5f), Color.Transparent),
            center = Offset(currentX, waveY),
            radius = pointRadius * 3
        ),
        center = Offset(currentX, waveY),
        radius = pointRadius * 3
    )
}

private fun DrawScope.drawBatteryHistory(
    width: Float,
    height: Float,
    history: List<BatteryHistory>
) {
    if (history.size < 2) return

    val path = Path()
    val maxTime = history.maxOf { it.time }
    val minTime = history.minOf { it.time }
    val timeRange = maxTime - minTime

    // Vẽ đường lịch sử pin
    for (i in 0 until history.size - 1) {
        val current = history[i]
        val next = history[i + 1]

        val x1 = width * (current.time - minTime).toFloat() / timeRange
        val y1 = height * (1 - current.level / 100f)

        val x2 = width * (next.time - minTime).toFloat() / timeRange
        val y2 = height * (1 - next.level / 100f)

        if (i == 0) {
            path.moveTo(x1, y1)
        }

        path.lineTo(x2, y2)
    }

    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.6f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Vẽ các điểm lịch sử
    for (point in history) {
        val x = width * (point.time - minTime).toFloat() / timeRange
        val y = height * (1 - point.level / 100f)

        drawCircle(
            color = if (point.isCharging) Color.Green else Color.White,
            center = Offset(x, y),
            radius = 3.dp.toPx()
        )
    }
}

@Composable
fun BatteryInfoCard(title: String, value: String) {
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

