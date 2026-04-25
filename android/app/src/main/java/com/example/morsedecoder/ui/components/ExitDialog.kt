package com.example.morsedecoder.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ExitDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit App") },
        text = { Text("Are you sure you want to exit the application?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK", color = Color(0xFF2DD4BF))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray
    )
}
