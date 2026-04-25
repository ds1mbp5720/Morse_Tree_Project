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

import androidx.compose.ui.tooling.preview.Preview

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

    Canvas(modifier = modifier.fillMaxWidth().height(300.dp)) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val startY = 30f
        val stepY = 60.dp.toPx()
        val stepX = 80.dp.toPx()

        fun drawNode(char: String, path: String, x: Float, y: Float, level: Int) {
            val isActive = currentPath == path
            val nodeColor = if (isActive) Color(0xFF2DD4BF) else Color(0xFF374151)
            val textColor = if (isActive) Color.White else Color.Gray

            // Draw children lines
            if (level < 2) { // 2단계 까지만 그리기 (공간 문제)
                val nextXStep = stepX / (level + 1)
                // Left (.)
                drawLine(
                    color = Color(0xFF1F2937),
                    start = Offset(x, y),
                    end = Offset(x - nextXStep, y + stepY),
                    strokeWidth = 2f
                )
                // Right (-)
                drawLine(
                    color = Color(0xFF1F2937),
                    start = Offset(x, y),
                    end = Offset(x + nextXStep, y + stepY),
                    strokeWidth = 2f
                )
            }

            // Draw Node Shape
            if (path.isEmpty()) {
                drawCircle(color = nodeColor, radius = 15f, center = Offset(x, y), style = Stroke(width = 4f))
            } else if (path.endsWith(".")) {
                drawCircle(color = nodeColor, radius = 12f, center = Offset(x, y))
            } else if (path.endsWith("-")) {
                drawRect(
                    color = nodeColor,
                    topLeft = Offset(x - 20f, y - 8f),
                    size = androidx.compose.ui.geometry.Size(40f, 16f)
                )
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

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0B)
@Composable
fun MorseTreeVisualizerPreview() {
    MorseTreeVisualizer(currentPath = ".-")
}
