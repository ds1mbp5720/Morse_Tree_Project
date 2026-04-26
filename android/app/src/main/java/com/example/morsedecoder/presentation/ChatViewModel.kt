package com.example.morsedecoder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.morsedecoder.domain.model.MorseMessage
import com.example.morsedecoder.domain.repository.ChatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    val messages: StateFlow<List<MorseMessage>> = repository.getMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val peerCount: StateFlow<Int> = repository.getPeers()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val wifiName: String = repository.getWifiName()

    init {
        repository.startServer()
    }

    fun sendMessage(text: String, morse: String) {
        viewModelScope.launch {
            repository.sendMessage(text, morse)
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopServer()
    }
}
