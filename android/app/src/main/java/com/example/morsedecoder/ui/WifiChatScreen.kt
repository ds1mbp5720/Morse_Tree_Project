package com.example.morsedecoder.ui

import android.content.Context
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.morsedecoder.audio.MorseToneGenerator
import com.example.morsedecoder.domain.model.MorseMessage
import com.example.morsedecoder.presentation.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WifiChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    
    var playingMessageId by remember { mutableStateOf<String?>(null) }
    var playingCharIndex by remember { mutableStateOf(-1) }
    
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val toneGenerator = remember { MorseToneGenerator() }
    val scope = rememberCoroutineScope()

    val morseMap = mapOf(
        "A" to ".-", "B" to "-...", "C" to "-.-.", "D" to "-..", "E" to ".", "F" to "..-.",
        "G" to "--.", "H" to "....", "I" to "..", "J" to ".---", "K" to "-.-", "L" to ".-..",
        "M" to "--", "N" to "-.", "O" to "---", "P" to ".--.", "Q" to "--.-", "R" to ".-.",
        "S" to "...", "T" to "-", "U" to "..-", "V" to "...-", "W" to ".--", "X" to "-..-",
        "Y" to "-.--", "Z" to "--.."
    )

    fun encodeToMorse(text: String): String {
        return text.uppercase().map { morseMap[it.toString()] ?: "" }.joinToString(" ")
    }

    suspend fun playMorse(messageId: String, morse: String) {
        playingMessageId = messageId
        val unit = 100L
        val parts = morse.split(" ")
        for (i in parts.indices) {
            playingCharIndex = i
            val part = parts[i]
            for (symbol in part) {
                toneGenerator.start()
                val duration = if (symbol == '.') unit else unit * 3
                vibrator.vibrate(duration)
                delay(duration)
                toneGenerator.stop()
                delay(unit)
            }
            delay(unit * 2) 
        }
        playingMessageId = null
        playingCharIndex = -1
    }

    // Auto-play incoming messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val lastMessage = messages.last()
            if (!lastMessage.isFromMe) {
                playMorse(lastMessage.id, lastMessage.morse)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0B))
            .padding(16.dp)
    ) {
        Text(
            "WIFI_P2P_MESSSENGER_V1.1",
            color = Color(0xFF2DD4BF),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(
                    message = message,
                    isPlaying = playingMessageId == message.id,
                    playingIndex = if (playingMessageId == message.id) playingCharIndex else -1,
                    onPlay = {
                        scope.launch { playMorse(message.id, message.morse) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1F2937),
                    unfocusedContainerColor = Color(0xFF1F2937),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                placeholder = { Text("Protocol input...") }
            )
            IconButton(
                onClick = {
                    if (inputText.isNotEmpty()) {
                        viewModel.sendMessage(inputText, encodeToMorse(inputText))
                        inputText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF2DD4BF))
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: MorseMessage,
    isPlaying: Boolean,
    playingIndex: Int,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        Text(
            message.sender,
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (message.isFromMe) {
                IconButton(onClick = onPlay, modifier = Modifier.size(24.dp), enabled = !isPlaying) {
                    Icon(
                        if (isPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isPlaying) Color(0xFF2DD4BF) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .background(
                        if (message.isFromMe) Color(0xFF2DD4BF) else Color(0xFF374151),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    val annotatedText = buildAnnotatedString {
                        message.text.forEachIndexed { index, char ->
                            val style = if (isPlaying && index == playingIndex) {
                                SpanStyle(color = if (message.isFromMe) Color.White else Color(0xFF2DD4BF), fontWeight = FontWeight.Black)
                            } else {
                                SpanStyle(color = if (message.isFromMe) Color(0xFF0A0A0B) else Color.White)
                            }
                            withStyle(style) {
                                append(char)
                            }
                        }
                    }
                    Text(
                        text = annotatedText,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        message.morse,
                        color = (if (message.isFromMe) Color(0xFF0A0A0B) else Color.White).copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (!message.isFromMe) {
                IconButton(onClick = onPlay, modifier = Modifier.size(24.dp), enabled = !isPlaying) {
                    Icon(
                        if (isPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isPlaying) Color(0xFF2DD4BF) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
