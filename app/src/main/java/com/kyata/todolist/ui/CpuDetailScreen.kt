package com.kyata.todolist.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuDetailScreen(
    onBack: () -> Unit
) {
    val cpuInfo = remember { getDetailedCpuInfo() }
    val groupedInfo = remember(cpuInfo) { groupCpuInfo(cpuInfo) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CPU Details",
                        color = Color.White // màu text
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = Color.White, // màu text mặc định
                    navigationIconContentColor = Color.White // màu icon
                ),
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White // màu icon
                        )
                    }
                }
            )

        },
        content = { paddingValues ->
            DynamicGalaxyBackground {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedInfo.forEach { (category, items) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(items) { (key, value) ->
                            CpuInfoCard(title = key, value = value)
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

        }
    )
}

@Composable
fun CpuInfoCard(title: String, value: String) {
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

// Group thông tin CPU theo category
private fun groupCpuInfo(info: List<Pair<String, String>>): Map<String, List<Pair<String, String>>> {
    val grouped = mutableMapOf<String, MutableList<Pair<String, String>>>()

    info.forEach { (key, value) ->
        val category = when {
            key.contains("processor", ignoreCase = true) || key.contains("Core", ignoreCase = true) -> "Processor Cores"
            key.contains("model", ignoreCase = true) || key.contains("name", ignoreCase = true) || key.contains("Features", ignoreCase = true) -> "CPU Details"
            key.contains("bogo", ignoreCase = true) || key.contains("MHz", ignoreCase = true) || key.contains("Governor", ignoreCase = true) -> "Performance"
            key.contains("Cache", ignoreCase = true) -> "Cache Information"
            else -> "Other Information"
        }

        if (!grouped.containsKey(category)) {
            grouped[category] = mutableListOf()
        }
        grouped[category]?.add(key to value)
    }

    return grouped
}
fun getDetailedCpuInfo(): List<Pair<String, String>> {
    val info = mutableListOf<Pair<String, String>>()

    try {
        val processorSections = mutableListOf<String>()
        var currentSection = StringBuilder()

        File("/proc/cpuinfo").forEachLine { line ->
            if (line.trim().isEmpty()) {
                if (currentSection.isNotEmpty()) {
                    processorSections.add(currentSection.toString())
                    currentSection = StringBuilder()
                }
            } else {
                currentSection.appendLine(line)
            }
        }

        // Thông tin core đầu tiên
        if (processorSections.isNotEmpty()) {
            val lines = processorSections[0].lines()
            lines.forEach { line ->
                if (line.contains(":")) {
                    val parts = line.split(":").map { it.trim() }
                    if (parts.size >= 2) {
                        info.add(parts[0] to parts.subList(1, parts.size).joinToString(":").trim())
                    }
                }
            }

            // Features
            val featuresLine = lines.find { it.startsWith("Features") || it.startsWith("flags") }
            featuresLine?.split(":")?.getOrNull(1)?.trim()?.let {
                info.add("CPU Features" to it)
            }
        }

        // Số cores
        info.add("Total Cores" to processorSections.size.toString())

        // Thông tin tần số & governor từng core
        for (i in 0 until processorSections.size) {
            val basePath = "/sys/devices/system/cpu/cpu$i/cpufreq/"
            readSysFile("${basePath}scaling_cur_freq")?.let { info.add("CPU $i Current Frequency" to "${it.toFloat() / 1000} MHz") }
            readSysFile("${basePath}cpuinfo_min_freq")?.let { info.add("CPU $i Min Frequency" to "${it.toFloat() / 1000} MHz") }
            readSysFile("${basePath}cpuinfo_max_freq")?.let { info.add("CPU $i Max Frequency" to "${it.toFloat() / 1000} MHz") }
            readSysFile("${basePath}scaling_governor")?.let { info.add("CPU $i Governor" to it) }
        }

        // Cache
        val cacheDir = File("/sys/devices/system/cpu/cpu0/cache/")
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach { levelDir ->
                val level = readSysFile("${levelDir.absolutePath}/level")
                val type = readSysFile("${levelDir.absolutePath}/type")
                val size = readSysFile("${levelDir.absolutePath}/size")
                if (level != null && type != null && size != null) {
                    info.add("L$level $type Cache" to size)
                }
            }
        }

    } catch (e: Exception) {
        info.add("CPU Info" to "Unable to read CPU details: ${e.message}")
    }

    return info
}

private fun readSysFile(path: String): String? {
    return try {
        val file = File(path)
        if (file.exists()) file.readText().trim() else null
    } catch (e: Exception) {
        null
    }
}
