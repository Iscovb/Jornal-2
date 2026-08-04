package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BreakdownBarChart
import com.example.ui.components.EquityCurveChart
import com.example.ui.components.StatCard
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingWinGreen
import com.example.ui.viewmodel.TradingUiState
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: TradingUiState,
    modifier: Modifier = Modifier
) {
    val metrics = uiState.metrics
    val currency = uiState.preferredCurrency

    val currencyFormatter = remember(currency) {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        try {
            fmt.currency = Currency.getInstance(currency)
        } catch (_: Exception) {}
        fmt
    }

    Column(
        modifier = modifier
            .testTag("dashboard_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PERFORMANCE ANALYTICS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TradingTextMuted,
            letterSpacing = 1.sp
        )

        // 1. Metric Cards Grid (Row 1 & Row 2)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val netPnLVal = currencyFormatter.format(metrics.netProfit)
            StatCard(
                title = "Net P&L",
                value = "${if (metrics.netProfit >= 0) "+" else ""}$netPnLVal",
                subtitle = "${metrics.winsCount} Wins • ${metrics.lossesCount} Losses",
                isPositive = metrics.netProfit >= 0,
                icon = Icons.Default.ShowChart,
                modifier = Modifier.weight(1f),
                testTag = "stat_net_pnl"
            )

            StatCard(
                title = "Win Rate",
                value = String.format("%.1f%%", metrics.winRatePercent),
                subtitle = "${metrics.totalTrades} Total Executions",
                isPositive = metrics.winRatePercent >= 50.0,
                icon = Icons.Default.PieChart,
                modifier = Modifier.weight(1f),
                testTag = "stat_win_rate"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Profit Factor",
                value = String.format("%.2f", metrics.profitFactor),
                subtitle = "Avg Win: ${currencyFormatter.format(metrics.averageWin)}",
                isPositive = metrics.profitFactor >= 1.5,
                icon = Icons.Default.QueryStats,
                modifier = Modifier.weight(1f),
                testTag = "stat_profit_factor"
            )

            StatCard(
                title = "Expectancy (R)",
                value = String.format("%.2f R", metrics.expectancyR),
                subtitle = "Rule Compliance: ${String.format("%.0f%%", metrics.ruleCompliancePercent)}",
                isPositive = metrics.expectancyR > 0,
                icon = Icons.Default.FactCheck,
                accentColor = TradingPrimary,
                modifier = Modifier.weight(1f),
                testTag = "stat_expectancy"
            )
        }

        // 2. Account Equity Curve Chart
        EquityCurveChart(
            points = uiState.equityCurve,
            currency = currency
        )

        // 3. Breakdown Charts
        BreakdownBarChart(
            title = "Performance by Day of Week",
            items = uiState.breakdownByDay,
            currency = currency
        )

        BreakdownBarChart(
            title = "Performance by Trading Session",
            items = uiState.breakdownBySession,
            currency = currency
        )

        BreakdownBarChart(
            title = "Performance by Strategy Playbook",
            items = uiState.breakdownBySetup,
            currency = currency
        )

        BreakdownBarChart(
            title = "Performance by Asset / Pair",
            items = uiState.breakdownByAsset,
            currency = currency
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
