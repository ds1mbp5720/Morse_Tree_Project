package com.example.morsedecoder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PulsePad(
    isPressing: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: (duration: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var startTime = 0L

    Box(
        modifier = modifier
            .size(160.dp)
            .background(Color(0xFF1F2937), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        startTime = System.currentTimeMillis()
                        onPressStart()
                        tryAwaitRelease()
                        val duration = System.currentTimeMillis() - startTime
                        onPressEnd(duration)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(if (isPressing) Color(0xFF2DD4BF) else Color(0xFF374151), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isPressing) "SIGNAL" else "READY",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
