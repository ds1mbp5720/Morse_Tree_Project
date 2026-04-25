package com.example.morsedecoder.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.morsedecoder.domain.model.MorseMessage
import com.example.morsedecoder.presentation.ChatViewModel

@Composable
fun WifiChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0B))
            .padding(16.dp)
    ) {
        Text(
            "WIFI_P2P_MESSSENGER_V1.0",
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
                ChatBubble(message)
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
fun ChatBubble(message: MorseMessage) {
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
        Box(
            modifier = Modifier
                .background(
                    if (message.isFromMe) Color(0xFF2DD4BF) else Color(0xFF374151),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    message.text,
                    color = if (message.isFromMe) Color(0xFF0A0A0B) else Color.White,
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
    }
}
