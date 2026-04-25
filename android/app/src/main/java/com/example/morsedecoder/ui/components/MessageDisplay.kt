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

import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MessageDisplay(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF111827), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .heightIn(min = 80.dp)
    ) {
        Column {
            Text(
                "STREAM //",
                color = Color(0xFF2DD4BF).copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                message.ifEmpty { "READY..." },
                color = if (message.isEmpty()) Color.White.copy(alpha = 0.1f) else Color.White,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                lineHeight = 28.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0B)
@Composable
fun MessageDisplayPreview() {
    MessageDisplay(message = "HELLO WORLD")
}
