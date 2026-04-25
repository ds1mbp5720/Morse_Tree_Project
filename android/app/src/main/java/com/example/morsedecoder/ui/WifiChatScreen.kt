package com.example.morsedecoder.ui

import android.content.Context
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

import com.example.morsedecoder.ui.components.PulsePad
import com.example.morsedecoder.domain.util.MorseDictionary

@Composable
fun WifiChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var currentMorseBuffer by remember { mutableStateOf("") }
    var lastTapTime by remember { mutableStateOf(0L) }
    var isPressingPad by remember { mutableStateOf(false) }
    
    // 이미 재생된 메시지 ID들을 추적하여 탭 이동 시 중복 재생 방지
    var playedMessageIds by remember { mutableStateOf(messages.map { it.id }.toSet()) }
    var isTextVisible by remember { mutableStateOf(true) }
    
    var playingMessageId by remember { mutableStateOf<String?>(null) }
    var playingCharIndex by remember { mutableStateOf(-1) }
    
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val toneGenerator = remember { MorseToneGenerator() }
    val scope = rememberCoroutineScope()

    // Space detection for morse input
    LaunchedEffect(lastTapTime) {
        if (lastTapTime > 0) {
            // 1단계: 0.8초 동안 입력 없으면 글자 변환
            delay(800) 
            if (System.currentTimeMillis() - lastTapTime >= 800 && currentMorseBuffer.isNotEmpty()) {
                inputText += MorseDictionary.decodeChar(currentMorseBuffer)
                currentMorseBuffer = ""
            }
            
            // 2단계: 총 2초 동안 입력 없으면 띄어쓰기 추가
            delay(1200) 
            if (System.currentTimeMillis() - lastTapTime >= 2000 && inputText.isNotEmpty() && !inputText.endsWith(" ")) {
                inputText += " "
            }
        }
    }

    fun encodeToMorse(text: String): String = MorseDictionary.encode(text)

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

    // 새 메시지 수신 시 자동 재생 (이미 확인된 건 건너뜀)
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            val lastMessage = messages.last()
            if (!lastMessage.isFromMe && !playedMessageIds.contains(lastMessage.id)) {
                playedMessageIds = playedMessageIds + lastMessage.id
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
                    isTextVisible = isTextVisible,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("TEXT", color = Color.Gray, fontSize = 10.sp)
                Switch(
                    checked = isTextVisible,
                    onCheckedChange = { isTextVisible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF2DD4BF),
                        checkedTrackColor = Color(0xFF2DD4BF).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.scale(0.7f)
                )
            }
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
                placeholder = { 
                    Text(
                        if (currentMorseBuffer.isEmpty()) "Protocol input..." else currentMorseBuffer,
                        color = if (currentMorseBuffer.isEmpty()) Color.Gray else Color(0xFF2DD4BF)
                    ) 
                }
            )
            IconButton(
                onClick = {
                    if (inputText.isNotEmpty()) {
                        viewModel.sendMessage(inputText, encodeToMorse(inputText))
                        inputText = ""
                        currentMorseBuffer = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF2DD4BF))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Morse Pulse Pad for Wifi Chat
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            PulsePad(
                isPressing = isPressingPad,
                onPressStart = {
                    isPressingPad = true
                    toneGenerator.start()
                    vibrator.vibrate(50)
                },
                onPressEnd = { duration ->
                    isPressingPad = false
                    toneGenerator.stop()
                    val symbol = if (duration < 200) "." else "-"
                    currentMorseBuffer += symbol
                    lastTapTime = System.currentTimeMillis()
                }
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: MorseMessage,
    isPlaying: Boolean,
    playingIndex: Int,
    isTextVisible: Boolean,
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
                    if (isTextVisible) {
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
                    }
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
