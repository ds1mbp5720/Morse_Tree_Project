package com.example.morsedecoder.ui

import android.content.Context
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val peerCount by viewModel.peerCount.collectAsState()
    val wifiName = viewModel.wifiName
    
    var inputText by remember { mutableStateOf("") }
    var currentMorseBuffer by remember { mutableStateOf("") }
    var lastTapTime by remember { mutableStateOf(0L) }
    var isPressingPad by remember { mutableStateOf(false) }
    
    var showProsignSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
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
            if (part.isEmpty()) {
                delay(unit * 4) // Word gap (total 7 units with trailing delay)
            } else {
                for (symbol in part) {
                    toneGenerator.start()
                    val isDash = symbol == '-'
                    val duration = if (isDash) unit * 3 else unit
                    
                    if (isDash) {
                        vibrator.vibrate(longArrayOf(0, duration), -1)
                    } else {
                        vibrator.vibrate(duration)
                    }
                    
                    delay(duration)
                    toneGenerator.stop()
                    delay(unit)
                }
            }
            delay(unit * 2) // Letter gap
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
        // Networking Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Wifi,
                    contentDescription = null,
                    tint = Color(0xFF2DD4BF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = wifiName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Surface(
                color = if (peerCount > 0) Color(0xFF2DD4BF).copy(alpha = 0.1f) else Color.DarkGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (peerCount > 0) "$peerCount PEER(S) CONNECTED" else "WAITING FOR PEERS...",
                    color = if (peerCount > 0) Color(0xFF2DD4BF) else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

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
                    val meaning = MorseDictionary.getMeaning(currentMorseBuffer)
                    Column {
                        Text(
                            currentMorseBuffer,
                            color = if (currentMorseBuffer.isEmpty()) Color.Gray else Color(0xFF2DD4BF)
                        )
                        if (meaning != null) {
                            Text(
                                text = "($meaning)",
                                color = Color(0xFF2DD4BF).copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            )
            IconButton(
                onClick = { showProsignSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Prosign", tint = Color(0xFF2DD4BF))
            }
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

        if (showProsignSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProsignSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF1F2937),
                contentColor = Color.White
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            "SELECT_PROSIGN",
                            color = Color(0xFF2DD4BF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    val prosigns = MorseDictionary.getAllProsigns()
                    items(prosigns.toList()) { (morse, meaning) ->
                        ListItem(
                            headlineContent = {
                                Text(meaning, color = Color.White, fontSize = 14.sp)
                            },
                            supportingContent = {
                                Text(morse, color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            },
                            modifier = Modifier.clickable {
                                val decoded = MorseDictionary.decodeChar(morse)
                                inputText += (if (inputText.isNotEmpty() && !inputText.endsWith(" ")) " " else "") + decoded
                                scope.launch {
                                    sheetState.hide()
                                    showProsignSheet = false
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Morse Pulse Pad for Wifi Chat
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sequence Display Bar
            if (currentMorseBuffer.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF1F2937), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            currentMorseBuffer,
                            color = Color(0xFF2DD4BF),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val meaning = MorseDictionary.getMeaning(currentMorseBuffer)
                        if (meaning != null) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "(${meaning.substringAfter(":").trim()})",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            PulsePad(
                isPressing = isPressingPad,
                onPressStart = {
                    isPressingPad = true
                    toneGenerator.start()
                    vibrator.vibrate(longArrayOf(0, 10000), 0)
                },
                onPressEnd = { duration ->
                    isPressingPad = false
                    toneGenerator.stop()
                    vibrator.cancel()
                    val symbol = if (duration < 200) "." else "-"
                    currentMorseBuffer += symbol
                    lastTapTime = System.currentTimeMillis()
                }
            )

            LaunchedEffect(isPressingPad) {
                if (isPressingPad) {
                    delay(200)
                    if (isPressingPad) {
                        // Provide a slight "pulse" feeling when it becomes a dash
                        vibrator.vibrate(20) 
                    }
                }
            }
        }
    }
}

fun getSenderColor(sender: String): Color {
    if (sender == "Me" || sender.isEmpty()) return Color(0xFF2DD4BF)
    val colors = listOf(
        Color(0xFFF472B6), // Pink
        Color(0xFF60A5FA), // Blue
        Color(0xFFFB923C), // Orange
        Color(0xFFA78BFA), // Violet
        Color(0xFFFACC15), // Yellow
        Color(0xFF4ADE80), // Green
        Color(0xFFF87171)  // Red
    )
    val index = (sender.hashCode().let { if (it == Int.MIN_VALUE) 0 else Math.abs(it) }) % colors.size
    return colors[index]
}

@Composable
fun ChatBubble(
    message: MorseMessage,
    isPlaying: Boolean,
    playingIndex: Int,
    isTextVisible: Boolean,
    onPlay: () -> Unit
) {
    val senderColor = remember(message.sender) { getSenderColor(message.sender) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        Text(
            message.sender,
            color = if (message.isFromMe) Color.Gray else senderColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 2.dp)
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
                        if (message.isFromMe) Color(0xFF2DD4BF) else senderColor.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .then(
                        if (!message.isFromMe) Modifier.border(1.dp, senderColor, RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .padding(12.dp)
            ) {
                Column {
                    if (isTextVisible) {
                        val textParts = remember(message.morse) { 
                            message.morse.split(" ").map { MorseDictionary.decodeChar(it) } 
                        }
                        val annotatedText = buildAnnotatedString {
                            textParts.forEachIndexed { index, part ->
                                val style = if (isPlaying && index == playingIndex) {
                                    SpanStyle(
                                        color = if (message.isFromMe) Color.White else senderColor,
                                        fontWeight = FontWeight.Black,
                                        background = if (message.isFromMe) Color.Black.copy(alpha = 0.2f) else Color.Transparent
                                    )
                                } else {
                                    SpanStyle(color = if (message.isFromMe) Color(0xFF0A0A0B) else Color.White)
                                }
                                withStyle(style) {
                                    append(part)
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
                        tint = if (isPlaying) senderColor else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
