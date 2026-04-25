package com.example.morsedecoder

import android.os.Bundle
import android.os.Vibrator
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import com.example.morsedecoder.data.WifiChatRepositoryImpl
import com.example.morsedecoder.presentation.ChatViewModel
import com.example.morsedecoder.ui.WifiChatScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.morsedecoder.ui.MorseScreen
import com.example.morsedecoder.ui.EncoderScreen
import com.example.morsedecoder.audio.MorseToneGenerator

class MainActivity : ComponentActivity() {
    private val chatRepository by lazy { WifiChatRepositoryImpl(applicationContext) }
    private val chatViewModel by lazy { ChatViewModel(chatRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var selectedTab by remember { mutableStateOf(0) }
            var showExitDialog by remember { mutableStateOf(false) }
            val toneGenerator = remember { MorseToneGenerator() }
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            BackHandler {
                showExitDialog = true
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Exit App") },
                    text = { Text("Are you sure you want to exit the application?") },
                    confirmButton = {
                        TextButton(onClick = { finish() }) {
                            Text("OK", color = Color(0xFF2DD4BF))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1F2937),
                    titleContentColor = Color.White,
                    textContentColor = Color.LightGray
                )
            }

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
                            val navColors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2DD4BF),
                                selectedTextColor = Color(0xFF2DD4BF),
                                indicatorColor = Color(0xFF2DD4BF).copy(alpha = 0.1f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.List, contentDescription = "Decoder") },
                                label = { Text("Decoder") },
                                colors = navColors
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Send, contentDescription = "Encoder") },
                                label = { Text("Encoder") },
                                colors = navColors
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.Wifi, contentDescription = "Wifi Chat") },
                                label = { Text("Wifi Chat") },
                                colors = navColors
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
                        if (selectedTab == 0) {
                            MorseScreen(this@MainActivity)
                        } else if (selectedTab == 1) {
                            EncoderScreen(
                                toneGenerator = toneGenerator,
                                animateVibrate = { ms -> vibrator.vibrate(ms) }
                            )
                        } else {
                            WifiChatScreen(chatViewModel)
                        }
                    }
                }
            }
        }
    }
}
