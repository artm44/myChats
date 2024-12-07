package com.artm44.mychats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.artm44.mychats.data.ChatRepository
import com.artm44.mychats.network.SessionManager

class MainViewModelFactory(
    private val sessionManager: SessionManager,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(sessionManager, chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

