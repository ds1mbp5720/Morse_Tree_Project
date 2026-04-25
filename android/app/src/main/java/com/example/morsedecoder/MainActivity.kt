package com.example.morsedecoder

import android.os.Bundle
import android.os.Vibrator
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.morsedecoder.ui.MorseScreen
import com.example.morsedecoder.ui.EncoderScreen
import com.example.morsedecoder.audio.MorseToneGenerator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedTab by remember { mutableStateOf(0) }
            val toneGenerator = remember { MorseToneGenerator() }
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF2DD4BF),
                    background = Color(0xFF0A0A0B),
                    surface = Color(0xFF111827)
                )
            ) {
                Scaffold(
                    bottomBar = {
                        NavigationBar(containerColor = Color(0xFF0F172A)) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.List, contentDescription = "Decoder") },
                                label = { Text("Decoder") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Send, contentDescription = "Encoder") },
                                label = { Text("Encoder") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
                        if (selectedTab == 0) {
                            MorseScreen(this@MainActivity)
                        } else {
                            EncoderScreen(
                                toneGenerator = toneGenerator,
                                animateVibrate = { ms -> vibrator.vibrate(ms) }
                            )
                        }
                    }
                }
            }
        }
    }
}
