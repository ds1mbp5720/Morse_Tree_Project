package com.example.morsedecoder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.nativeCanvas

@Composable
fun MorseTreeVisualizer(
    currentPath: String,
    modifier: Modifier = Modifier
) {
    // 간단한 트리 맵핑 (알파벳 기준)
    val treeData = mapOf(
        "" to "START",
        "E" to ".", "T" to "-",
        "I" to "..", "A" to ".-", "N" to "-.", "M" to "--",
        "S" to "...", "U" to "..-", "R" to ".-.", "W" to ".--",
        "D" to "-..", "K" to "-.-", "G" to "--.", "O" to "---"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val startY = 40f
        val stepY = 40.dp.toPx()
        val stepX = 60.dp.toPx()

        fun drawNode(char: String, path: String, x: Float, y: Float, level: Int) {
            val isActive = currentPath == path
            val nodeColor = if (isActive) Color(0xFF2DD4BF) else Color(0xFF374151)
            val textColor = if (isActive) Color.White else Color.Gray

            // Draw children lines
            if (level < 3) {
                // Left (.)
                drawLine(
                    color = nodeColor,
                    start = Offset(x, y),
                    end = Offset(x - stepX / (level + 1), y + stepY),
                    strokeWidth = 2f
                )
                // Right (-)
                drawLine(
                    color = nodeColor,
                    start = Offset(x, y),
                    end = Offset(x + stepX / (level + 1), y + stepY),
                    strokeWidth = 2f
                )
            }

            // Draw Node Shape
            if (path.endsWith(".")) {
                drawCircle(color = nodeColor, radius = 12f, center = Offset(x, y))
            } else if (path.endsWith("-")) {
                drawRect(
                    color = nodeColor,
                    topLeft = Offset(x - 10f, y - 10f),
                    size = androidx.compose.ui.geometry.Size(20f, 20f)
                )
            } else {
                drawCircle(color = nodeColor, radius = 15f, center = Offset(x, y), style = Stroke(width = 4f))
            }

            // Text label
            drawContext.canvas.nativeCanvas.drawText(
                char,
                x + 20f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = textColor.hashCode()
                    textSize = 10.sp.toPx()
                    isFakeBoldText = isActive
                }
            )
        }

        // 0단계: Root
        drawNode("ROOT", "", centerX, startY, 0)
        
        // 1단계: E, T
        drawNode("E", ".", centerX - stepX, startY + stepY, 1)
        drawNode("T", "-", centerX + stepX, startY + stepY, 1)

        // 2단계: I, A, N, M
        drawNode("I", "..", centerX - stepX - stepX/2, startY + stepY*2, 2)
        drawNode("A", ".-", centerX - stepX + stepX/2, startY + stepY*2, 2)
        drawNode("N", "-.", centerX + stepX - stepX/2, startY + stepY*2, 2)
        drawNode("M", "--", centerX + stepX + stepX/2, startY + stepY*2, 2)
    }
}
