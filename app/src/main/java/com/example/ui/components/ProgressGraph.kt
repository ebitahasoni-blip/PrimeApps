package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssessmentReport
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InteractiveProgressGraph(
    reports: List<AssessmentReport>,
    targetBand: Float = 7.5f,
    modifier: Modifier = Modifier
) {
    if (reports.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Submit essays to track your IELTS band progression",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val sortedReports = remember(reports) {
        reports.sortedBy { it.createdAtTimestamp }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val textPrimary = MaterialTheme.colorScheme.onSurface

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .testTag("progress_graph_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 36.dp, end = 20.dp, top = 20.dp, bottom = 32.dp)
                .pointerInput(sortedReports) {
                    detectTapGestures { offset ->
                        val n = sortedReports.size
                        if (n > 1) {
                            val spacing = size.width / (n - 1)
                            val tappedIndex = ((offset.x + spacing / 2) / spacing)
                                .toInt()
                                .coerceIn(0, n - 1)
                            selectedIndex = tappedIndex
                        } else {
                            selectedIndex = 0
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            val minBand = 4.0f
            val maxBand = 9.0f
            val bandRange = maxBand - minBand

            // Draw horizontal grid lines for Bands 5.0, 6.0, 7.0, 8.0, 9.0
            for (b in 5..9) {
                val y = height - ((b - minBand) / bandRange) * height
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        isAntiAlias = true
                    }
                    drawText("B$b", -85f, y + 10f, paint)
                }
            }

            // Draw target line
            val targetY = height - ((targetBand - minBand) / bandRange) * height
            drawLine(
                color = tertiaryColor,
                start = Offset(0f, targetY),
                end = Offset(width, targetY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            // Draw the band progression line
            val points = mutableListOf<Offset>()
            val count = sortedReports.size

            sortedReports.forEachIndexed { index, report ->
                val x = if (count > 1) (index.toFloat() / (count - 1)) * width else width / 2
                val y = height - ((report.overallBandScore - minBand) / bandRange) * height
                points.add(Offset(x, y))
            }

            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        val midX = (p0.x + p1.x) / 2
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw point circles
            points.forEachIndexed { i, pt ->
                val isSelected = selectedIndex == i
                val radius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx()
                drawCircle(
                    color = if (isSelected) tertiaryColor else primaryColor,
                    radius = radius,
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = radius / 2,
                    center = pt
                )
            }
        }

        // Selected point tooltip overlay
        selectedIndex?.let { idx ->
            if (idx in sortedReports.indices) {
                val report = sortedReports[idx]
                val dateStr = SimpleDateFormat("MMM dd", Locale.ENGLISH).format(Date(report.createdAtTimestamp))
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "Essay #${idx + 1} ($dateStr): Band ${report.overallBandScore} (TR:${report.taskResponse.bandScore} CC:${report.coherenceCohesion.bandScore} LR:${report.lexicalResource.bandScore} GRA:${report.grammaticalRangeAccuracy.bandScore})",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
