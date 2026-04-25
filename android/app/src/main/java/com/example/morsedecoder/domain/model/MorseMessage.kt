package com.example.morsedecoder.domain.model

data class MorseMessage(
    val id: String,
    val text: String,
    val morse: String,
    val sender: String,
    val timestamp: Long,
    val isFromMe: Boolean
)
