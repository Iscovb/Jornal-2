package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BreakdownItem
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingLossRed
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.theme.TradingWinGreen
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun BreakdownBarChart(
    title: String,
    items: List<BreakdownItem>,
    currency: String = "USD",
    modifier: Modifier = Modifier
) {
    val currencyFormatter = rememberCurrencyFormatter(currency)

    Box(
        modifier = modifier
            .testTag("breakdown_chart_${title.lowercase().replace(" ", "_")}")
            .fillMaxWidth()
            .background(TradingSurface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, TradingCardBorder, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TradingTextMuted,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (items.isEmpty()) {
                Text(
                    text = "No breakdown data available.",
                    fontSize = 12.sp,
                    color = TradingTextMuted
                )
            } else {
                val maxAbsPnL = items.maxOfOrNull { Math.abs(it.totalPnL) }?.coerceAtLeast(1.0) ?: 1.0

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items.forEach { item ->
                        BreakdownRow(
                            item = item,
                            maxAbsPnL = maxAbsPnL,
                            currencyFormatter = currencyFormatter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    item: BreakdownItem,
    maxAbsPnL: Double,
    currencyFormatter: NumberFormat
) {
    val isProfit = item.totalPnL >= 0
    val absRatio = (Math.abs(item.totalPnL) / maxAbsPnL).toFloat().coerceIn(0.05f, 1f)
    val animatedWidthRatio by animateFloatAsState(targetValue = absRatio, label = "bar_width")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.category,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TradingTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${item.tradeCount} trades • ${String.format("%.0f%%", item.winRatePercent)} WR)",
                    fontSize = 11.sp,
                    color = TradingTextMuted
                )
            }

            Text(
                text = "${if (isProfit) "+" else ""}${currencyFormatter.format(item.totalPnL)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isProfit) TradingWinGreen else TradingLossRed
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress track bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(TradingCardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedWidthRatio)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isProfit) TradingWinGreen else TradingLossRed)
            )
        }
    }
}

@Composable
private fun rememberCurrencyFormatter(currency: String): NumberFormat {
    return androidx.compose.runtime.remember(currency) {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        try {
            fmt.currency = Currency.getInstance(currency)
        } catch (_: Exception) {}
        fmt
    }
}
