package com.example.morsedecoder

import android.os.Bundle
import android.os.Vibrator
import android.content.Context
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MorseApp(this)
            }
        }
    }
}

@Composable
fun MorseApp(context: Context) {
    var message by remember { mutableStateOf("") }
    var currentSequence by remember { mutableStateOf("") }
    var isPressing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Morse Tree Logic
    val morseMap = mapOf(
        ".-" to "A", "-..." to "B", "-.-." to "C", "-.." to "D", "." to "E",
        "..-." to "F", "--." to "G", "...." to "H", ".." to "I", ".---" to "J",
        "-.-" to "K", ".-.." to "L", "--" to "M", "-." to "N", "---" to "O",
        ".--." to "P", "--.-" to "Q", ".-." to "R", "..." to "S", "-" to "T",
        "..-" to "U", "...-" to "V", ".--" to "W", "-..-" to "X", "-.--" to "Y", "--.." to "Z"
    )

    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val toneGenerator = remember { MorseToneNode() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0B))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "SIGNAL PROCESSOR",
            color = Color(0xFF2DD4BF),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Message Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF111827), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Text(
                message.ifEmpty { "WAITING..." },
                color = Color.White,
                fontSize = 32.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Current Sequence
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF0F172A), RoundedCornerShape(16.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                currentSequence.ifEmpty { "----" },
                color = Color(0xFF2DD4BF),
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Pulse Pad (The Button)
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFF1F2937), CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            val startTime = System.currentTimeMillis()
                            isPressing = true
                            toneGenerator.start()
                            vibrator.vibrate(20)
                            
                            tryAwaitRelease()
                            
                            isPressing = false
                            toneGenerator.stop()
                            val duration = System.currentTimeMillis() - startTime
                            
                            if (duration < 200) {
                                currentSequence += "."
                            } else {
                                currentSequence += "-"
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(if (isPressing) Color(0xFF2DD4BF) else Color(0xFF374151), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isPressing) "SIGNAL" else "READY",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Auto-decode Logic
        LaunchedEffect(currentSequence) {
            if (currentSequence.isNotEmpty()) {
                delay(1000)
                val char = morseMap[currentSequence]
                if (char != null) {
                    message += char
                }
                currentSequence = ""
            }
        }

        Button(
            onClick = { message = "" },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Text("CLEAR CACHE", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

// Simple Audio Tone Generator for Morse
class MorseToneNode {
    private val sampleRate = 44100
    private val freq = 700.0
    private var audioTrack: AudioTrack? = null

    fun start() {
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        val samples = ShortArray(bufferSize)
        for (i in samples.indices) {
            samples[i] = (sin(2.0 * Math.PI * i / (sampleRate / freq)) * Short.MAX_VALUE).toInt().toShort()
        }
        
        audioTrack?.play()
        audioTrack?.write(samples, 0, samples.size)
    }

    fun stop() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
