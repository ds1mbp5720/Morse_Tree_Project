package com.example.morsedecoder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageDisplay(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF111827), RoundedCornerShape(24.dp))
            .padding(24.dp)
            .heightIn(min = 120.dp)
    ) {
        Column {
            Text(
                "DECODED_STREAM //",
                color = Color(0xFF2DD4BF).copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message.ifEmpty { "WAITING_FOR_SIGNAL..." },
                color = if (message.isEmpty()) Color.White.copy(alpha = 0.1f) else Color.White,
                fontSize = 32.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                lineHeight = 38.sp
            )
        }
    }
}
