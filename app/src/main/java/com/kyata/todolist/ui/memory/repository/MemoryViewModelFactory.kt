package com.kyata.todolist.ui.memory.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kyata.todolist.ui.memory.viewmodel.MemoryViewModel

class MemoryViewModelFactory(
    private val repo: MemoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoryViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
