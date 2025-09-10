package com.kyata.todolist.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyata.todolist.R
import kotlin.random.Random

private const val MAX_NETWORK_SPEED_KBPS = 230_000L // 1.84 Tbps

// Định nghĩa palette màu mới
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val GalaxyBlue1 = Color(0xFF0D1B2A)
val GalaxyBlue2 = Color(0xFF1B263B)
val GalaxyBlue3 = Color(0xFF415A77)
val GalaxyBlue4 = Color(0xFF778DA9)
val GalaxyBlue5 = Color(0xFFE0E1DD)

val SystemCyan = Color(0xFF03DAC6)
val SystemBlue = Color(0xFF3700B3)
val SystemTeal = Color(0xFF018786)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    cpuUsage: Float,
    batteryLevel: Int,
    memoryUsage: Int,
    batteryTemp: Float,
    netDownloadSpeed: Long,
    netUploadSpeed: Long,
    onCpuClick: () -> Unit,
    onBatteryClick: () -> Unit,
    onMemoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    // Tạo MaterialTheme mới với palette màu tùy chỉnh
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple80,
            secondary = PurpleGrey80,
            tertiary = Pink80,
            background = GalaxyBlue1,
            surface = GalaxyBlue2,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
        )
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "System Monitor",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            DynamicGalaxyBackground {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row 1: CPU và Battery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "CPU Usage",
                            onClick = onCpuClick,
                            modifier = Modifier.weight(1f),
                            accentColor = SystemCyan,
                            content = {
                                CpuUsageItem(
                                    usage = cpuUsage,
                                    accentColor = SystemCyan
                                )
                            }
                        )
                        StatCard(
                            title = "Battery",
                            value = "$batteryLevel%",
                            onClick = onBatteryClick,
                            modifier = Modifier.weight(1f),
                            accentColor = SystemTeal,
                            content = {
                                BatteryUsageItem(
                                    level = batteryLevel,
                                    accentColor = SystemTeal
                                )
                            }
                        )
                    }

                    // Row 2: Memory và Temperature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Memory",
                            onClick = onMemoryClick,
                            modifier = Modifier.weight(1f),
                            accentColor = SystemBlue,
                            content = {
                                MemoryUsageItem(
                                    usage = memoryUsage,
                                    accentColor = SystemBlue
                                )
                            }
                        )
                        StatCard(
                            title = "Battery Temp",
                            onClick = onBatteryClick,
                            modifier = Modifier.weight(1f),
                            accentColor = Color(0xFFFF6B35),
                            content = {
                                TemperatureUsageItem(
                                    temperature = batteryTemp,
                                    accentColor = Color(0xFFFF6B35)
                                )
                            }
                        )
                    }

                    // Row 3: Network
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Download",
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            accentColor = Color(0xFF4FC3F7),
                            content = {
                                val percentD =
                                    (netDownloadSpeed.toFloat() / MAX_NETWORK_SPEED_KBPS) * 100f
                                DownloadUsageItem(
                                    netDownloadSpeed = netDownloadSpeed.toFloat(),
                                    progress = percentD,
                                    accentColor = Color(0xFF4FC3F7)
                                )
                            }
                        )
                        StatCard(
                            title = "Upload",
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            accentColor = Color(0xFFBA68C8),
                            content = {
                                val percentU =
                                    (netUploadSpeed.toFloat() / MAX_NETWORK_SPEED_KBPS) * 100f
                                UploadUsageItem(
                                    netUploadSpeed = netUploadSpeed.toFloat(),
                                    progress = percentU,
                                    accentColor = Color(0xFFBA68C8)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DynamicGalaxyBackground(content: @Composable () -> Unit) {
    var center by remember { mutableStateOf(Offset.Zero) }
    val starCount = 150

    // Tạo hiệu ứng chuyển động cho nền
    val infiniteTransition = rememberInfiniteTransition()
    val backgroundShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val stars = remember {
        List(starCount) {
            Star(
                position = Offset(Random.nextFloat(), Random.nextFloat()),
                radius = Random.nextFloat() * 2.5f + 0.5f,
                alpha = Random.nextFloat() * 0.7f + 0.3f,
                speed = Random.nextFloat() * 0.2f + 0.05f
            )
        }
    }

    // Animation cho các ngôi sao
    val animatedStars = stars.map { star ->
        val alpha by infiniteTransition.animateFloat(
            initialValue = star.alpha,
            targetValue = star.alpha * 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (1000 / star.speed).toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
        star.copy(alpha = alpha)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                val size = layoutCoordinates.size
                center = Offset(size.width / 2f, size.height / 2f)
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Gradient background với hiệu ứng chuyển động nhẹ
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GalaxyBlue3.copy(alpha = 0.6f),
                        GalaxyBlue2,
                        GalaxyBlue1
                    ),
                    center = center + Offset(
                        size.width * 0.2f * backgroundShift,
                        size.height * 0.1f * backgroundShift
                    ),
                    radius = size.width * 1.5f
                ),
                size = size
            )

            // Vẽ các ngôi sao
            animatedStars.forEach { star ->
                drawCircle(
                    color = Color.White.copy(alpha = star.alpha),
                    radius = star.radius,
                    center = Offset(
                        star.position.x * size.width,
                        star.position.y * size.height
                    )
                )
            }

            // Thêm hiệu ứng tinh vân
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Purple80.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = center + Offset(size.width * 0.3f, -size.height * 0.2f),
                    radius = size.width * 0.7f
                ),
                center = center + Offset(size.width * 0.3f, -size.height * 0.2f),
                radius = size.width * 0.7f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SystemTeal.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center + Offset(-size.width * 0.2f, size.height * 0.3f),
                    radius = size.width * 0.5f
                ),
                center = center + Offset(-size.width * 0.2f, size.height * 0.3f),
                radius = size.width * 0.5f
            )
        }

        content()
    }
}

data class Star(
    val position: Offset,
    val radius: Float,
    val alpha: Float,
    val speed: Float
)

@Composable
fun StatCard(
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color,
    content: (@Composable () -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .height(160.dp)  // Tăng chiều cao để có không gian
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = accentColor.copy(alpha = 0.2f),
                spotColor = accentColor.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (isPressed) 0.15f else 0.05f),
                            Color.Transparent
                        ),
                        center = Offset.Unspecified,
                        radius = 300f
                    )
                )
                .padding(16.dp),  // Tăng padding
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (content != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        content()
                    }
                } else if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }
    }
}

@Composable
fun CpuUsageItem(
    usage: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = SystemCyan
) {
    val animatedUsage by animateFloatAsState(
        targetValue = usage.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "cpuAnim"
    )

    Box(
        modifier = modifier.size(100.dp),  // Giảm kích thước để vừa với container
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedUsage / 100f },
            modifier = Modifier.fillMaxSize(),
            color = accentColor,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format("%.1f", animatedUsage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(bottom = 2.5.dp) // Điều chỉnh nếu cần
                )
            }

        }
    }
}

@Composable
fun BatteryUsageItem(
    level: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = SystemTeal
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "batteryAnim"
    )

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedLevel / 100f },
            modifier = Modifier.fillMaxSize(),
            color = accentColor,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animatedLevel.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(bottom = 2.5.dp) // Điều chỉnh nếu cần
                )
            }

        }
    }
}

@Composable
fun MemoryUsageItem(
    usage: Int,
    modifier: Modifier = Modifier,
    accentColor: Color = SystemBlue
) {
    val animatedUsage by animateFloatAsState(
        targetValue = usage.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "memoryAnim"
    )

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedUsage / 100f },
            modifier = Modifier.fillMaxSize(),
            color = accentColor,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${animatedUsage.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(bottom = 2.5.dp) // Điều chỉnh nếu cần
                )
            }

        }
    }
}

@Composable
fun TemperatureUsageItem(
    temperature: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFFFF6B35)
) {
    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Biểu đồ nhiệt độ dạng vòng tròn
        val progress = (temperature.coerceIn(20f, 60f) - 20f) / 40f

        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = accentColor,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = String.format("%.1f", temperature),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = "°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(bottom = 2.5.dp) // Điều chỉnh nếu cần
                )
            }

        }
    }
}

@Composable
fun DownloadUsageItem(
    netDownloadSpeed: Float,
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF4FC3F7)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "downloadAnim"
    )

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress / 100f },
            modifier = Modifier.fillMaxSize(),
            color = accentColor,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_download),
                contentDescription = "Download",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${netDownloadSpeed.toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
                Text(
                    text = " KB/s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(bottom = 2.5.dp) // Điều chỉnh nếu cần // Căn chỉnh baseline
                )
            }
        }
    }
}

@Composable
fun UploadUsageItem(
    netUploadSpeed: Float,
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFFBA68C8)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "uploadAnim"
    )

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress / 100f },
            modifier = Modifier.fillMaxSize(),
            color = accentColor,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_upload),
                contentDescription = "Upload",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${netUploadSpeed.toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
                Text(
                    text = " KB/s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(bottom = 2.5.dp) // Điều chỉnh nếu cần // Căn chỉnh baseline
                )
            }
        }
    }
}
