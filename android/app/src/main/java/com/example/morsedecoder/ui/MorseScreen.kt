package com.example.morsedecoder.ui

import android.content.Context
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.morsedecoder.audio.MorseToneGenerator
import com.example.morsedecoder.ui.components.MessageDisplay
import com.example.morsedecoder.ui.components.MorseTreeVisualizer
import com.example.morsedecoder.ui.components.PulsePad
import kotlinx.coroutines.delay

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import com.example.morsedecoder.domain.util.MorseDictionary

@Composable
fun MorseScreen(context: Context) {
    var message by remember { mutableStateOf("") }
    var currentSequence by remember { mutableStateOf("") }
    var isPressing by remember { mutableStateOf(false) }
    var isPlayingBack by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val toneGenerator = remember { MorseToneGenerator() }

    fun playMessage() {
        if (isPlayingBack || message.isEmpty()) return
        scope.launch {
            isPlayingBack = true
            val unit = 100L
            for (char in message.uppercase()) {
                val code = MorseDictionary.encode(char.toString())
                if (code.isNotEmpty()) {
                    for (symbol in code) {
                        toneGenerator.start()
                        val duration = if (symbol == '.') unit else unit * 3
                        vibrator.vibrate(duration)
                        delay(duration)
                        toneGenerator.stop()
                        delay(unit) // Inter-element gap
                    }
                    delay(unit * 3) // Inter-letter gap
                } else if (char == ' ') {
                    delay(unit * 7)
                }
            }
            isPlayingBack = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0B))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Bar / Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(Color(0xFF2DD4BF)).padding(end = 8.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "SIGNAL_PROCESSOR_V2.4",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        MorseTreeVisualizer(currentPath = currentSequence)

        Spacer(modifier = Modifier.height(16.dp))

        // Decoded Stream & Sequence Group
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val morseCode = MorseDictionary.encode(message)
            MessageDisplay(message = message, morseCode = morseCode)

            // Sequence Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    currentSequence.ifEmpty { "READY_FOR_INPUT" },
                    color = Color(0xFF2DD4BF),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PulsePad(
            isPressing = isPressing,
            onPressStart = {
                isPressing = true
                toneGenerator.start()
                vibrator.vibrate(30)
            },
            onPressEnd = { duration ->
                isPressing = false
                toneGenerator.stop()
                if (duration < 250) currentSequence += "." else currentSequence += "-"
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { if (message.isNotEmpty()) message = message.dropLast(1) }) {
                Text("BACKSPACE", color = Color.Gray)
            }

            Button(
                onClick = { playMessage() },
                enabled = message.isNotEmpty() && !isPlayingBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2DD4BF),
                    contentColor = Color(0xFF0A0A0B),
                    disabledContainerColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("PLAYBACK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = { message = "" }) {
                Text("CLEAR_ALL", color = Color(0xFFEF4444))
            }
        }
    }

    // Auto-decode Logic
    LaunchedEffect(currentSequence) {
        if (currentSequence.isNotEmpty()) {
            delay(1200) // Wait for user to stop typing
            val char = MorseDictionary.decodeChar(currentSequence)
            if (char != "?") {
                message += char
            }
            currentSequence = ""
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MorseScreenPreview() {
    MaterialTheme {
        // Mock context normally wouldn't work well in preview for system services,
        // but for layout visualization it's often okay or requires a wrapper.
    }
}
