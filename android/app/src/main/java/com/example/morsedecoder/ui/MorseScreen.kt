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
import com.example.morsedecoder.ui.components.PulsePad
import kotlinx.coroutines.delay

@Composable
fun MorseScreen(context: Context) {
    var message by remember { mutableStateOf("") }
    var currentSequence by remember { mutableStateOf("") }
    var isPressing by remember { mutableStateOf(false) }
    
    val morseMap = mapOf(
        ".-" to "A", "-..." to "B", "-.-." to "C", "-.." to "D", "." to "E",
        "..-." to "F", "--." to "G", "...." to "H", ".." to "I", ".---" to "J",
        "-.-" to "K", ".-.." to "L", "--" to "M", "-." to "N", "---" to "O",
        ".--." to "P", "--.-" to "Q", ".-." to "R", "..." to "S", "-" to "T",
        "..-" to "U", "...-" to "V", ".--" to "W", "-..-" to "X", "-.--" to "Y", "--.." to "Z"
    )

    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val toneGenerator = remember { MorseToneGenerator() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0B))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Bar / Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(Color(0xFF2DD4BF)).padding(end = 8.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "SIGNAL_PROCESSOR_V2.4",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        MessageDisplay(message = message)

        Spacer(modifier = Modifier.height(24.dp))

        // Sequence Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                currentSequence.ifEmpty { "READY_FOR_INPUT" },
                color = Color(0xFF2DD4BF),
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { message = message.dropLast(1) }) {
                Text("BACKSPACE", color = Color.Gray)
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
            val char = morseMap[currentSequence]
            if (char != null) {
                message += char
            }
            currentSequence = ""
        }
    }
}
