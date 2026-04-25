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

    Canvas(modifier = modifier.fillMaxWidth().height(330.dp)) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val startY = 40f
        val stepY = 44.dp.toPx()
        val baseStepX = 105.dp.toPx()

        val paint = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }

        fun getChar(path: String): String {
            return when(path) {
                "" -> "START"
                "." -> "E"
                "-" -> "T"
                ".." -> "I"
                ".-" -> "A"
                "-." -> "N"
                "--" -> "M"
                "..." -> "S"
                "..-" -> "U"
                ".-." -> "R"
                ".--" -> "W"
                "-.." -> "D"
                "-.-" -> "K"
                "--." -> "G"
                "---" -> "O"
                "...." -> "H"
                "...-" -> "V"
                "..-." -> "F"
                ".-.." -> "L"
                ".--." -> "P"
                ".---" -> "J"
                "-..." -> "B"
                "-..-" -> "X"
                "-.-." -> "C"
                "-.--" -> "Y"
                "--.." -> "Z"
                "--.-" -> "Q"
                // Numbers
                "-----" -> "0"
                ".----" -> "1"
                "..---" -> "2"
                "...--" -> "3"
                "....-" -> "4"
                "....." -> "5"
                "-...." -> "6"
                "--..." -> "7"
                "---.." -> "8"
                "----." -> "9"
                // Special chars
                ".-.-.-" -> "."
                "--..--" -> ","
                "..--.." -> "?"
                "-.-.--" -> "!"
                "-..-." -> "/"
                else -> " "
            }
        }

        fun hasContent(path: String, level: Int): Boolean {
            if (getChar(path) != " ") return true
            if (level >= 6) return false
            return hasContent(path + ".", level + 1) || hasContent(path + "-", level + 1)
        }

        fun drawMorseNode(char: String, path: String, x: Float, y: Float) {
            val isActive = currentPath == path
            val nodeColor = if (isActive) Color(0xFF2DD4BF) else Color(0xFF374151)
            val textColor = if (isActive) Color.White else Color(0xFF94A3B8)
            
            // Draw current node
            if (path.isEmpty()) {
                drawCircle(color = nodeColor, radius = 15f, center = Offset(x, y), style = Stroke(width = 4f))
            } else {
                if (path.last() == '.') {
                    drawCircle(color = nodeColor, radius = 10f, center = Offset(x, y))
                } else {
                    drawRect(
                        color = nodeColor,
                        topLeft = Offset(x - 18f, y - 6f),
                        size = androidx.compose.ui.geometry.Size(36f, 12f)
                    )
                }
            }

            // Text Label
            if (char != " " && char != "START") {
                drawContext.canvas.nativeCanvas.drawText(
                    char,
                    x,
                    y - 25f,
                    paint.apply {
                        color = textColor.hashCode()
                        textSize = (if (isActive) 19.sp else 14.sp).toPx()
                    }
                )
            } else if (char == "START") {
                drawContext.canvas.nativeCanvas.drawText(
                    char,
                    x,
                    y - 25f,
                    paint.apply {
                        color = textColor.hashCode()
                        textSize = 14.sp.toPx()
                    }
                )
            }
        }

        // Tree structure definitions
        fun render(x: Float, y: Float, path: String, level: Int) {
            val char = getChar(path)
            drawMorseNode(char, path, x, y)
            
            if (level < 6) {
                val nextXStep = baseStepX / Math.pow(2.4, level.toDouble()).toFloat()
                
                // Left child (.)
                val leftPath = path + "."
                if (hasContent(leftPath, level + 1)) {
                    drawLine(
                        color = Color(0xFF1F2937),
                        start = Offset(x, y),
                        end = Offset(x - nextXStep, y + stepY),
                        strokeWidth = 3f
                    )
                    render(x - nextXStep, y + stepY, leftPath, level + 1)
                }
                
                // Right child (-)
                val rightPath = path + "-"
                if (hasContent(rightPath, level + 1)) {
                    drawLine(
                        color = Color(0xFF1F2937),
                        start = Offset(x, y),
                        end = Offset(x + nextXStep, y + stepY),
                        strokeWidth = 3f
                    )
                    render(x + nextXStep, y + stepY, rightPath, level + 1)
                }
            }
        }

        render(centerX, startY, "", 0)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0B)
@Composable
fun MorseTreeVisualizerPreview() {
    MorseTreeVisualizer(currentPath = ".-")
}
