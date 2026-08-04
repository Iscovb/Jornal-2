package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPnL
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingLossRed
import com.example.ui.theme.TradingLossRedContainer
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingSurfaceVariant
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.theme.TradingWinGreen
import com.example.ui.theme.TradingWinGreenContainer
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarGrid(
    year: Int,
    month: Int, // 0-indexed
    monthlyCalendar: List<DailyPnL>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (DailyPnL) -> Unit,
    currency: String = "USD",
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatSymbols(Locale.US).months.getOrNull(month) ?: "Month"
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Determine day of week offset for the 1st of the month (0 = Mon, 6 = Sun)
    val firstDayOffset = rememberFirstDayOffset(year, month)

    // Calculate monthly totals
    val netMonthlyPnL = monthlyCalendar.sumOf { it.totalPnL }
    val totalMonthlyTrades = monthlyCalendar.sumOf { it.tradeCount }
    val winningDays = monthlyCalendar.count { it.totalPnL > 0.01 }
    val losingDays = monthlyCalendar.count { it.totalPnL < -0.01 }

    Box(
        modifier = modifier
            .testTag("pnl_calendar_grid")
            .fillMaxWidth()
            .background(TradingSurface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, TradingCardBorder, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.testTag("calendar_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = TradingTextPrimary
                        )
                    }

                    Text(
                        text = "$monthName $year",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextPrimary
                    )

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.testTag("calendar_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = TradingTextPrimary
                        )
                    }
                }

                // Monthly summary pill
                val isPos = netMonthlyPnL >= 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPos) TradingWinGreenContainer else TradingLossRedContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${if (isPos) "+" else ""}${formatCompactCurrency(netMonthlyPnL, currency)} ($winningDays W / $losingDays L)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPos) TradingWinGreen else TradingLossRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of week column headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName.uppercase(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid days
            val totalCells = firstDayOffset + monthlyCalendar.size
            val numRows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 0 until numRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (c in 0..6) {
                            val cellIndex = r * 7 + c
                            val dayIndex = cellIndex - firstDayOffset

                            if (dayIndex in monthlyCalendar.indices) {
                                val dailyData = monthlyCalendar[dayIndex]
                                CalendarDayTile(
                                    dailyData = dailyData,
                                    currency = currency,
                                    onClick = { onDayClick(dailyData) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Empty spacer tile
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayTile(
    dailyData: DailyPnL,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasTrades = dailyData.tradeCount > 0
    val isProfit = dailyData.totalPnL > 0.01
    val isLoss = dailyData.totalPnL < -0.01

    val bgColor = when {
        !hasTrades -> TradingSurfaceVariant
        isProfit -> TradingWinGreenContainer.copy(alpha = 0.85f)
        isLoss -> TradingLossRedContainer.copy(alpha = 0.85f)
        else -> TradingSurfaceVariant
    }

    val borderColor = when {
        !hasTrades -> TradingCardBorder
        isProfit -> TradingWinGreen
        isLoss -> TradingLossRed
        else -> TradingCardBorder
    }

    Box(
        modifier = modifier
            .testTag("calendar_day_${dailyData.dayOfMonth}")
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = hasTrades, onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dailyData.dayOfMonth}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasTrades) TradingTextPrimary else TradingTextMuted
                )

                if (hasTrades) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TradingSurface)
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${dailyData.tradeCount}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextMuted
                        )
                    }
                }
            }

            if (hasTrades) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${if (isProfit) "+" else ""}${formatCompactCurrency(dailyData.totalPnL, currency)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isProfit) TradingWinGreen else if (isLoss) TradingLossRed else TradingTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun rememberFirstDayOffset(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
    // Shift so Mon = 0, Sun = 6
    return (dayOfWeek + 5) % 7
}

private fun formatCompactCurrency(amount: Double, currency: String): String {
    val absAmount = Math.abs(amount)
    val formatted = when {
        absAmount >= 1000 -> String.format("%.1fk", amount / 1000.0)
        else -> String.format("%.0f", amount)
    }
    val symbol = when (currency) {
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "$"
    }
    return "$symbol$formatted"
}
