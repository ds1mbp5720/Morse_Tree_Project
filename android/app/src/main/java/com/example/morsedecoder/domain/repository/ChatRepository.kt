package com.example.morsedecoder.domain.repository

import com.example.morsedecoder.domain.model.MorseMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(): Flow<List<MorseMessage>>
    suspend fun sendMessage(text: String, morse: String)
    fun startServer()
    fun stopServer()
    fun discoverPeers()
    fun connectToPeer(ipAddress: String)
}
