package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.StrategyRule
import com.example.data.model.Trade
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingLossRed
import com.example.ui.theme.TradingLossRedContainer
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.theme.TradingWinGreen
import com.example.ui.theme.TradingWinGreenContainer
import com.example.ui.viewmodel.TradingUiState

@Composable
fun AddEditTradeScreen(
    uiState: TradingUiState,
    onSaveTrade: (Trade, List<Long>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts = uiState.accounts
    var selectedAccId by remember { mutableStateOf(uiState.selectedAccountId ?: accounts.firstOrNull()?.id ?: 1L) }

    var symbol by remember { mutableStateOf("EURUSD") }
    var type by remember { mutableStateOf("BUY") }
    var entryPriceStr by remember { mutableStateOf("1.0850") }
    var exitPriceStr by remember { mutableStateOf("1.0910") }
    var stopLossStr by remember { mutableStateOf("1.0820") }
    var takeProfitStr by remember { mutableStateOf("1.0920") }
    var lotSizeStr by remember { mutableStateOf("1.0") }
    var riskPercentStr by remember { mutableStateOf("1.0") }
    var riskAmountStr by remember { mutableStateOf("100.0") }
    var netProfitStr by remember { mutableStateOf("600.0") }
    var session by remember { mutableStateOf("London") }
    var setupTag by remember { mutableStateOf("Liquidity Sweep") }

    val selectedEmotions = remember { mutableStateListOf("Disciplined") }
    val checkedRuleIds = remember { mutableStateListOf<Long>() }

    var notes by remember { mutableStateOf("") }
    var lessons by remember { mutableStateOf("") }

    val allRules = uiState.rulesMap.values.flatten()

    Column(
        modifier = modifier
            .testTag("add_trade_screen")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "LOG NEW TRADE ENTRY",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TradingTextPrimary,
            letterSpacing = 1.sp
        )

        // Target Account Dropdown Selector
        var accountDropdownExpanded by remember { mutableStateOf(false) }
        val selectedAccount = accounts.find { it.id == selectedAccId } ?: accounts.firstOrNull()

        Text(
            text = "TARGET TRADING ACCOUNT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TradingTextMuted,
            letterSpacing = 0.5.sp
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .testTag("trade_account_selector")
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TradingSurface)
                    .border(1.dp, TradingCardBorder, RoundedCornerShape(10.dp))
                    .clickable { accountDropdownExpanded = true }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = TradingPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = selectedAccount?.name ?: "Select Account",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextPrimary
                        )
                        if (selectedAccount != null) {
                            Text(
                                text = "${selectedAccount.brokerName} • ${selectedAccount.currency} ${String.format("%,.0f", selectedAccount.initialBalance)}",
                                fontSize = 11.sp,
                                color = TradingTextMuted
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select account",
                    tint = TradingTextMuted
                )
            }

            DropdownMenu(
                expanded = accountDropdownExpanded,
                onDismissRequest = { accountDropdownExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(TradingSurface)
                    .border(1.dp, TradingCardBorder)
            ) {
                accounts.forEach { acc ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = acc.name,
                                    fontWeight = if (selectedAccId == acc.id) FontWeight.Bold else FontWeight.Normal,
                                    color = TradingTextPrimary
                                )
                                Text(
                                    text = "${acc.brokerName} • ${acc.currency} ${String.format("%,.0f", acc.initialBalance)}",
                                    fontSize = 11.sp,
                                    color = TradingTextMuted
                                )
                            }
                        },
                        onClick = {
                            selectedAccId = acc.id
                            accountDropdownExpanded = false
                        },
                        modifier = Modifier.testTag("trade_account_option_${acc.id}")
                    )
                }
            }
        }

        // Account & Symbol Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Symbol Field
            OutlinedTextField(
                value = symbol,
                onValueChange = { symbol = it.uppercase() },
                label = { Text("Symbol / Pair", color = TradingTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_trade_symbol"),
                colors = customTextFieldColors()
            )

            // BUY / SELL Toggle
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TradingSurface)
                    .border(1.dp, TradingCardBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (type == "BUY") TradingWinGreenContainer else TradingSurface)
                        .clickable { type = "BUY" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BUY",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (type == "BUY") TradingWinGreen else TradingTextMuted,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (type == "SELL") TradingLossRedContainer else TradingSurface)
                        .clickable { type = "SELL" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SELL",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (type == "SELL") TradingLossRed else TradingTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Price specs: Entry, Exit, SL, TP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = entryPriceStr,
                onValueChange = { entryPriceStr = it },
                label = { Text("Entry Price", color = TradingTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_entry_price"),
                colors = customTextFieldColors()
            )

            OutlinedTextField(
                value = exitPriceStr,
                onValueChange = { exitPriceStr = it },
                label = { Text("Exit Price", color = TradingTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_exit_price"),
                colors = customTextFieldColors()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = stopLossStr,
                onValueChange = { stopLossStr = it },
                label = { Text("Stop Loss", color = TradingTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_stop_loss"),
                colors = customTextFieldColors()
            )

            OutlinedTextField(
                value = takeProfitStr,
                onValueChange = { takeProfitStr = it },
                label = { Text("Take Profit", color = TradingTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_take_profit"),
                colors = customTextFieldColors()
            )
        }

        // Lots, Risk %, Risk Amount, Net P&L
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = lotSizeStr,
                onValueChange = { lotSizeStr = it },
                label = { Text("Lots / Size", color = TradingTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_lot_size"),
                colors = customTextFieldColors()
            )

            OutlinedTextField(
                value = riskAmountStr,
                onValueChange = { riskAmountStr = it },
                label = { Text("Risk ($)", color = TradingTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_risk_amount"),
                colors = customTextFieldColors()
            )
        }

        // Net P&L Field with Auto-Calculate Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = netProfitStr,
                onValueChange = { netProfitStr = it },
                label = { Text("Net P&L ($)", color = TradingTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_net_pnl"),
                colors = customTextFieldColors()
            )

            Button(
                onClick = {
                    val entry = entryPriceStr.toDoubleOrNull() ?: 0.0
                    val exit = exitPriceStr.toDoubleOrNull() ?: 0.0
                    val lots = lotSizeStr.toDoubleOrNull() ?: 1.0
                    val diff = if (type == "BUY") (exit - entry) else (entry - exit)

                    val calcPnL = if (symbol.contains("JPY")) {
                        diff * lots * 1000
                    } else if (symbol.contains("BTC") || symbol.contains("SOL") || symbol.contains("XAU")) {
                        diff * lots
                    } else {
                        diff * lots * 100000
                    }
                    netProfitStr = String.format("%.2f", calcPnL)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(54.dp)
            ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = "Auto P&L")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Auto P&L", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Session Selector & Setup Tag
        Text(
            text = "SESSION & STRATEGY SETUP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TradingTextMuted
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sessions = listOf("Asian", "London", "New York")
            sessions.forEach { sess ->
                FilterChip(
                    selected = session == sess,
                    onClick = { session = sess },
                    label = { Text(sess, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TradingPrimary,
                        selectedLabelColor = TradingTextPrimary,
                        containerColor = TradingSurface,
                        labelColor = TradingTextMuted
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OutlinedTextField(
            value = setupTag,
            onValueChange = { setupTag = it },
            label = { Text("Setup / Strategy Tag (e.g. Liquidity Sweep, FVG, Breakout)", color = TradingTextMuted) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_setup_tag"),
            colors = customTextFieldColors()
        )

        // Mindset / Emotion Tags
        Text(
            text = "MINDSET & EMOTIONS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TradingTextMuted
        )

        val emotionOptions = listOf("Disciplined", "Patient", "FOMO", "Greedy", "Anxious", "Revenge")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            emotionOptions.forEach { emo ->
                val isSelected = selectedEmotions.contains(emo)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedEmotions.remove(emo) else selectedEmotions.add(emo)
                    },
                    label = { Text(emo, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (emo == "Disciplined" || emo == "Patient") TradingWinGreen else TradingLossRed,
                        selectedLabelColor = TradingTextPrimary,
                        containerColor = TradingSurface,
                        labelColor = TradingTextMuted
                    )
                )
            }
        }

        // Strategy Rule Compliance Checklist
        if (allRules.isNotEmpty()) {
            Text(
                text = "STRATEGY RULE COMPLIANCE CHECKLIST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TradingTextMuted
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TradingSurface)
                    .border(1.dp, TradingCardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allRules.forEach { rule ->
                    val isChecked = checkedRuleIds.contains(rule.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) checkedRuleIds.remove(rule.id) else checkedRuleIds.add(rule.id)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                if (it) checkedRuleIds.add(rule.id) else checkedRuleIds.remove(rule.id)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = TradingWinGreen,
                                uncheckedColor = TradingTextMuted
                            )
                        )
                        Text(
                            text = rule.ruleText,
                            fontSize = 12.sp,
                            fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                            color = if (isChecked) TradingTextPrimary else TradingTextMuted
                        )
                    }
                }
            }
        }

        // Execution Notes & Lessons
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Execution Notes & Analysis", color = TradingTextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_trade_notes"),
            colors = customTextFieldColors()
        )

        // Save & Cancel Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TradingSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TradingTextMuted, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val entryP = entryPriceStr.toDoubleOrNull() ?: 1.0
                    val exitP = exitPriceStr.toDoubleOrNull() ?: 1.0
                    val sl = stopLossStr.toDoubleOrNull() ?: 0.0
                    val tp = takeProfitStr.toDoubleOrNull() ?: 0.0
                    val lots = lotSizeStr.toDoubleOrNull() ?: 1.0
                    val riskP = riskPercentStr.toDoubleOrNull() ?: 1.0
                    val riskA = riskAmountStr.toDoubleOrNull() ?: 100.0
                    val netP = netProfitStr.toDoubleOrNull() ?: 0.0

                    val status = when {
                        netP > 0.01 -> "WIN"
                        netP < -0.01 -> "LOSS"
                        else -> "BREAKEVEN"
                    }

                    val trade = Trade(
                        accountId = selectedAccId,
                        symbol = symbol.ifBlank { "EURUSD" },
                        type = type,
                        entryPrice = entryP,
                        exitPrice = exitP,
                        stopLoss = sl,
                        takeProfit = tp,
                        lotSize = lots,
                        riskPercent = riskP,
                        riskAmount = riskA,
                        netProfit = netP,
                        session = session,
                        setupTag = setupTag.ifBlank { "Breakout" },
                        emotions = selectedEmotions.joinToString(", "),
                        executionNotes = notes,
                        lessonsLearned = lessons,
                        status = status
                    )

                    onSaveTrade(trade, checkedRuleIds.toList())
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("submit_save_trade"),
                colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Trade", fontWeight = FontWeight.Bold, color = TradingTextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TradingPrimary,
    unfocusedBorderColor = TradingCardBorder,
    focusedContainerColor = TradingSurface,
    unfocusedContainerColor = TradingSurface,
    focusedTextColor = TradingTextPrimary,
    unfocusedTextColor = TradingTextPrimary
)
