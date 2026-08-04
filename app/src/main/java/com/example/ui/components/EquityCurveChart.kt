package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EquityPoint
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingLossRed
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.theme.TradingWinGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EquityCurveChart(
    points: List<EquityPoint>,
    currency: String = "USD",
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<EquityPoint?>(null) }

    val currencyFormatter = remember(currency) {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        fmt.currency = java.util.Currency.getInstance(currency)
        fmt
    }

    Box(
        modifier = modifier
            .testTag("equity_curve_chart")
            .fillMaxWidth()
            .background(TradingSurface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, TradingCardBorder, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACCOUNT EQUITY CURVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextMuted,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val latestPnL = points.lastOrNull()?.cumulativePnL ?: 0.0
                    Text(
                        text = "${if (latestPnL >= 0) "+" else ""}${currencyFormatter.format(latestPnL)} Net Growth",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (latestPnL >= 0) TradingWinGreen else TradingLossRed
                    )
                }

                if (selectedPoint != null) {
                    val dateStr = SimpleDateFormat("MMM dd", Locale.US).format(Date(selectedPoint!!.timestamp))
                    Box(
                        modifier = Modifier
                            .background(TradingPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, TradingPrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Trade #${selectedPoint!!.tradeNumber} (${selectedPoint!!.symbol}): ${currencyFormatter.format(selectedPoint!!.cumulativePnL)} ($dateStr)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (points.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Log more trades to visualize equity growth curve.",
                        fontSize = 12.sp,
                        color = TradingTextMuted
                    )
                }
            } else {
                val minPnL = points.minOf { it.cumulativePnL }.coerceAtMost(0.0)
                val maxPnL = points.maxOf { it.cumulativePnL }.coerceAtLeast(100.0)
                val range = (maxPnL - minPnL).coerceAtLeast(1.0)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val stepX = width / (points.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                                selectedPoint = points[index]
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw horizontal gridlines & zero line
                    val gridSteps = 4
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    for (i in 0..gridSteps) {
                        val y = height * i / gridSteps
                        drawLine(
                            color = TradingCardBorder.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = dashPathEffect
                        )
                    }

                    // Zero level line
                    val zeroY = height - ((0.0 - minPnL) / range * height).toFloat()
                    drawLine(
                        color = TradingTextMuted.copy(alpha = 0.6f),
                        start = Offset(0f, zeroY),
                        end = Offset(width, zeroY),
                        strokeWidth = 1.5f,
                        pathEffect = dashPathEffect
                    )

                    // 2. Build curve path
                    val path = Path()
                    val fillPath = Path()

                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    val firstX = 0f
                    val firstY = height - ((points[0].cumulativePnL - minPnL) / range * height).toFloat()
                    path.moveTo(firstX, firstY)
                    fillPath.moveTo(firstX, height)
                    fillPath.lineTo(firstX, firstY)

                    for (i in 1 until points.size) {
                        val x = i * stepX
                        val y = height - ((points[i].cumulativePnL - minPnL) / range * height).toFloat()

                        val prevX = (i - 1) * stepX
                        val prevY = height - ((points[i - 1].cumulativePnL - minPnL) / range * height).toFloat()

                        val controlX1 = prevX + (x - prevX) / 2f
                        val controlY1 = prevY
                        val controlX2 = prevX + (x - prevX) / 2f
                        val controlY2 = y

                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    }

                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // Draw area gradient fill
                    val isPositiveOverall = (points.lastOrNull()?.cumulativePnL ?: 0.0) >= 0
                    val chartColor = if (isPositiveOverall) TradingWinGreen else TradingLossRed

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                chartColor.copy(alpha = 0.35f),
                                chartColor.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Draw main line
                    drawPath(
                        path = path,
                        color = chartColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw dots for each trade point
                    points.forEachIndexed { idx, point ->
                        val px = idx * stepX
                        val py = height - ((point.cumulativePnL - minPnL) / range * height).toFloat()
                        val isSelected = selectedPoint == point

                        drawCircle(
                            color = if (isSelected) Color.White else chartColor,
                            radius = if (isSelected) 6.dp.toPx() else 3.dp.toPx(),
                            center = Offset(px, py)
                        )
                        if (isSelected) {
                            drawCircle(
                                color = chartColor,
                                radius = 9.dp.toPx(),
                                center = Offset(px, py),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
            }
        }
    }
}
