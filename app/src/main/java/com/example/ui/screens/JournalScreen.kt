package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.ui.components.TradeCard
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.viewmodel.TradingUiState

@Composable
fun JournalScreen(
    uiState: TradingUiState,
    onSearchQueryChange: (String) -> Unit,
    onSessionFilterChange: (String) -> Unit,
    onSetupFilterChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onDeleteTrade: (Trade) -> Unit,
    onAddTradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trades = uiState.filteredTrades
    val currency = uiState.preferredCurrency

    Box(
        modifier = modifier
            .testTag("journal_screen")
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .testTag("trade_search_input")
                    .fillMaxWidth(),
                placeholder = { Text("Search by symbol (e.g. EURUSD), setup, or notes...", color = TradingTextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TradingTextMuted
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TradingPrimary,
                    unfocusedBorderColor = TradingCardBorder,
                    focusedContainerColor = TradingSurface,
                    unfocusedContainerColor = TradingSurface,
                    focusedTextColor = TradingTextPrimary,
                    unfocusedTextColor = TradingTextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text(
                        text = "STATUS:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextMuted
                    )
                }

                val statusOptions = listOf("ALL", "WIN", "LOSS", "BREAKEVEN")
                items(statusOptions) { st ->
                    FilterChip(
                        selected = uiState.statusFilter == st,
                        onClick = { onStatusFilterChange(st) },
                        label = { Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TradingPrimary,
                            selectedLabelColor = TradingTextPrimary,
                            containerColor = TradingSurface,
                            labelColor = TradingTextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = TradingCardBorder,
                            enabled = true,
                            selected = uiState.statusFilter == st
                        ),
                        modifier = Modifier.testTag("filter_status_$st")
                    )
                }

                item {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SESSION:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextMuted
                    )
                }

                val sessionOptions = listOf("ALL", "Asian", "London", "New York")
                items(sessionOptions) { sess ->
                    FilterChip(
                        selected = uiState.sessionFilter == sess,
                        onClick = { onSessionFilterChange(sess) },
                        label = { Text(sess, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TradingPrimary,
                            selectedLabelColor = TradingTextPrimary,
                            containerColor = TradingSurface,
                            labelColor = TradingTextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = TradingCardBorder,
                            enabled = true,
                            selected = uiState.sessionFilter == sess
                        ),
                        modifier = Modifier.testTag("filter_session_$sess")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trades Count Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRADE LOG JOURNAL (${trades.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TradingTextMuted,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = uiState.selectedAccountName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TradingPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trade List or Empty State
            if (trades.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TradingSurface)
                        .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TradingTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No trades recorded for active filter.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Log Trade' to add your manual trade entry or import MT4/MT5 CSV report.",
                            fontSize = 12.sp,
                            color = TradingTextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(trades, key = { it.id }) { trade ->
                        TradeCard(
                            trade = trade,
                            currency = currency,
                            onDelete = { onDeleteTrade(trade) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Log Trade
        FloatingActionButton(
            onClick = onAddTradeClick,
            containerColor = TradingPrimary,
            contentColor = TradingTextPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_trade")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Trade")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Log Trade",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
