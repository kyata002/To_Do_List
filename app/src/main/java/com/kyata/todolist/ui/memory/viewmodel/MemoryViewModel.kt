package com.kyata.todolist.ui.memory.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyata.todolist.ui.memory.model.MemoryUsage
import com.kyata.todolist.ui.memory.repository.MemoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MemoryViewModel(private val repo: MemoryRepository) : ViewModel() {
    private val _usageData = MutableStateFlow<List<MemoryUsage>>(emptyList())
    val usageData = _usageData.asStateFlow()

    private val _realtimeMemoryInfo = MutableStateFlow<MemoryUsage?>(null)
    val realtimeMemoryInfo = _realtimeMemoryInfo.asStateFlow()

    private val _realtimeProcMemInfo = MutableStateFlow<Map<String, String>>(emptyMap())
    val realtimeProcMemInfo = _realtimeProcMemInfo.asStateFlow()

    private var updateJob: Job? = null
    private val _historicalData = MutableStateFlow<List<MemoryUsage>>(emptyList())
    val historicalData = _historicalData.asStateFlow()

    private val _availableDays = MutableStateFlow<List<String>>(emptyList())
    val availableDays = _availableDays.asStateFlow()

    private val _selectedDay = MutableStateFlow<String?>(null)
    val selectedDay = _selectedDay.asStateFlow()

    fun loadAvailableDays() {
        viewModelScope.launch {
            _availableDays.value = repo.getAvailableDays()
            // Mặc định chọn ngày hiện tại
            _selectedDay.value = _availableDays.value.firstOrNull()
        }
    }

    fun loadHistoricalDataForDay(day: String) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(day)!!
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startTime = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endTime = calendar.timeInMillis - 1

            _historicalData.value = repo.getDataByTimeRange(startTime, endTime)
            _selectedDay.value = day
        }
    }

    fun startRealtimeUpdates(intervalMs: Long = 2000) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                // Cập nhật thông tin memory
                _realtimeMemoryInfo.value = repo.getRealtimeMemoryInfo()
                _realtimeProcMemInfo.value = repo.getRealtimeProcMemInfo()

                // Nghỉ giữa các lần cập nhật
                delay(intervalMs)
            }
        }
    }

    fun stopRealtimeUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    fun loadDataForDay(day: Long) {
        viewModelScope.launch {
            _usageData.value = repo.getDataByDay(day)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeUpdates()
    }
}
