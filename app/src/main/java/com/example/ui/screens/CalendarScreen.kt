package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyPnL
import com.example.ui.components.CalendarGrid
import com.example.ui.components.TradeCard
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingLossRed
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.theme.TradingWinGreen
import com.example.ui.viewmodel.TradingUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    uiState: TradingUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDailyPnL by remember { mutableStateOf<DailyPnL?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Column(
        modifier = modifier
            .testTag("calendar_screen")
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DAILY P&L CALENDAR",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TradingTextMuted,
            letterSpacing = 1.sp
        )

        // P&L Calendar Grid Component
        CalendarGrid(
            year = uiState.calendarYear,
            month = uiState.calendarMonth,
            monthlyCalendar = uiState.monthlyCalendar,
            onPrevMonth = onPrevMonth,
            onNextMonth = onNextMonth,
            onDayClick = { daily ->
                if (daily.tradeCount > 0) {
                    selectedDailyPnL = daily
                }
            },
            currency = uiState.preferredCurrency
        )
    }

    // Modal Sheet for Day Trades
    if (selectedDailyPnL != null) {
        val daily = selectedDailyPnL!!
        val isProfit = daily.totalPnL >= 0

        ModalBottomSheet(
            onDismissRequest = { selectedDailyPnL = null },
            sheetState = sheetState,
            containerColor = TradingSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TRADES ON ${daily.dateString.uppercase()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextPrimary
                        )
                        Text(
                            text = "${daily.tradeCount} Trade Executions",
                            fontSize = 12.sp,
                            color = TradingTextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isProfit) TradingWinGreen.copy(alpha = 0.2f) else TradingLossRed.copy(alpha = 0.2f))
                            .border(1.dp, if (isProfit) TradingWinGreen else TradingLossRed, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${if (isProfit) "+" else ""}$${String.format("%.2f", daily.totalPnL)} P&L",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isProfit) TradingWinGreen else TradingLossRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(daily.trades, key = { it.id }) { trade ->
                        TradeCard(
                            trade = trade,
                            currency = uiState.preferredCurrency,
                            onDelete = {}
                        )
                    }
                }
            }
        }
    }
}
