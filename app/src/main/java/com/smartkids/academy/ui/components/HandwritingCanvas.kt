package com.smartkids.academy.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp

data class DrawingStroke(
    val path: Path,
    val color: Color = Color.Black,
    val strokeWidth: Float = 8f
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HandwritingCanvas(
    modifier: Modifier = Modifier,
    drawColor: Color = Color.Black,
    strokeWidth: Float = 8f,
    isEraseMode: Boolean = false,
    useGridBackground: Boolean = true,
    onDrawEvent: () -> Unit = {}
) {
    val strokes = remember { mutableStateListOf<DrawingStroke>() }
    val undoneStrokes = remember { mutableStateListOf<DrawingStroke>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Column(modifier = modifier) {
        // Control Bar (Undo / Redo / Clear)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (strokes.isNotEmpty()) {
                        val last = strokes.removeAt(strokes.size - 1)
                        undoneStrokes.add(last)
                        onDrawEvent()
                    }
                },
                enabled = strokes.isNotEmpty()
            ) {
                Text("Undo")
            }
            Button(
                onClick = {
                    if (undoneStrokes.isNotEmpty()) {
                        val last = undoneStrokes.removeAt(undoneStrokes.size - 1)
                        strokes.add(last)
                        onDrawEvent()
                    }
                },
                enabled = undoneStrokes.isNotEmpty()
            ) {
                Text("Redo")
            }
            Button(
                onClick = {
                    strokes.clear()
                    undoneStrokes.clear()
                    currentPath = null
                    onDrawEvent()
                }
            ) {
                Text("Clear")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .pointerInteropFilter { event ->
                    val x = event.x
                    val y = event.y

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val path = Path().apply {
                                moveTo(x, y)
                            }
                            currentPath = path
                            undoneStrokes.clear()
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            currentPath?.lineTo(x, y)
                            // trigger recomposition
                            val temp = currentPath
                            currentPath = null
                            currentPath = temp
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            currentPath?.let {
                                val color = if (isEraseMode) Color.White else drawColor
                                strokes.add(DrawingStroke(it, color, strokeWidth))
                            }
                            currentPath = null
                            onDrawEvent()
                            true
                        }
                        else -> false
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw Grid or Lined backgrounds for handwriting guide
                if (useGridBackground) {
                    val gridSize = 40.dp.toPx()
                    val gridColor = Color.LightGray.copy(alpha = 0.5f)

                    // Vertical lines
                    var x = 0f
                    while (x < size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += gridSize
                    }
                    // Horizontal lines
                    var y = 0f
                    while (y < size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += gridSize
                    }
                } else {
                    // Draw normal handwriting lines
                    val lineSpacing = 30.dp.toPx()
                    var y = lineSpacing
                    val lineColor = Color.Blue.copy(alpha = 0.2f)
                    val baselineColor = Color.Red.copy(alpha = 0.2f)
                    var index = 0
                    while (y < size.height) {
                        val color = if (index % 3 == 2) baselineColor else lineColor
                        drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
                        y += lineSpacing
                        index++
                    }
                }

                // 2. Draw existing strokes
                strokes.forEach { stroke ->
                    drawPath(
                        path = stroke.path,
                        color = stroke.color,
                        style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // 3. Draw active stroke in progress
                currentPath?.let { path ->
                    drawPath(
                        path = path,
                        color = if (isEraseMode) Color.White else drawColor,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
