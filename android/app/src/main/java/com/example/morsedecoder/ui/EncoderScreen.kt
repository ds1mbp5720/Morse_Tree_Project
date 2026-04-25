package com.example.morsedecoder.ui

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.compose.ui.tooling.preview.Preview

import com.example.morsedecoder.domain.util.MorseDictionary

@Composable
fun EncoderScreen(toneGenerator: MorseToneGenerator, animateVibrate: (Long) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var isTransmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun encode(text: String): String = MorseDictionary.encode(text)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0B))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "ENCODER CONSOLE",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Input Message", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF2DD4BF)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFF111827), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("ENCODED_SIGNAL //", color = Color(0xFF2DD4BF), fontSize = 10.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    encode(inputText).ifEmpty { "PENDING..." },
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    isTransmitting = true
                    val sequence = encode(inputText)
                    val unit = 100L
                    for (char in sequence) {
                        when (char) {
                            '.' -> {
                                toneGenerator.start()
                                animateVibrate(unit)
                                delay(unit)
                                toneGenerator.stop()
                            }
                            '-' -> {
                                toneGenerator.start()
                                animateVibrate(unit * 3)
                                delay(unit * 3)
                                toneGenerator.stop()
                            }
                            ' ' -> delay(unit * 3)
                            '/' -> delay(unit * 7)
                        }
                        delay(unit)
                    }
                    isTransmitting = false
                }
            },
            enabled = !isTransmitting && inputText.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isTransmitting) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("TRANSMIT SEQUENCE", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0B)
@Composable
fun EncoderScreenPreview() {
    EncoderScreen(
        toneGenerator = MorseToneGenerator(),
        animateVibrate = {}
    )
}
