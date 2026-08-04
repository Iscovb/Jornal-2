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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.data.model.TradingAccount
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingSurfaceVariant
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.theme.TradingWinGreen
import com.example.ui.viewmodel.TradingUiState

@Composable
fun AccountsScreen(
    uiState: TradingUiState,
    onCreateAccount: (String, String, Double, String) -> Unit,
    onSelectAccount: (Long?) -> Unit,
    onImportCsv: (String, Long) -> Unit,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts = uiState.accounts
    var showCreateAccount by remember { mutableStateOf(false) }

    var csvText by remember { mutableStateOf("") }
    var targetAccountId by remember { mutableStateOf(uiState.selectedAccountId ?: accounts.firstOrNull()?.id ?: 1L) }

    Box(
        modifier = modifier
            .testTag("accounts_screen")
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accounts Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = "MULTI-ACCOUNT MANAGEMENT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Manage live, evaluation & personal trading accounts",
                        fontSize = 12.sp,
                        color = TradingTextMuted
                    )
                }

                Button(
                    onClick = { showCreateAccount = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("button_add_account_dialog")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // List of Accounts Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                accounts.forEach { acc ->
                    val isSelected = uiState.selectedAccountId == acc.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TradingSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) TradingPrimary else TradingCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectAccount(acc.id) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (isSelected) TradingPrimary else TradingTextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = acc.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TradingTextPrimary
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Active",
                                                tint = TradingPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${acc.brokerName} • ${acc.currency} ${String.format("%,.0f", acc.initialBalance)} Initial Balance",
                                        fontSize = 12.sp,
                                        color = TradingTextMuted
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TradingSurfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isSelected) "ACTIVE" else "SELECT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TradingPrimary else TradingTextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preferred Base Currency Selector
            Text(
                text = "DISPLAY CURRENCY SETTINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TradingTextMuted,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val currencies = listOf("USD", "EUR", "GBP")
                currencies.forEach { curr ->
                    val isSelected = uiState.preferredCurrency == curr
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TradingPrimary else TradingSurface)
                            .border(1.dp, TradingCardBorder, RoundedCornerShape(8.dp))
                            .clickable { onCurrencyChange(curr) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = curr,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TradingTextPrimary else TradingTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CSV & MT4/MT5 Trade Data Import Engine Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TradingSurface)
                    .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, tint = TradingPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRADE DATA IMPORT ENGINE (CSV / MT4 / MT5)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextPrimary
                        )
                    }

                    Text(
                        text = "Paste CSV rows or report text below to parse & import trades automatically:",
                        fontSize = 12.sp,
                        color = TradingTextMuted
                    )

                    var importAccDropdownExpanded by remember { mutableStateOf(false) }
                    val importAccount = accounts.find { it.id == targetAccountId } ?: accounts.firstOrNull()

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .testTag("import_account_selector")
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TradingSurfaceVariant)
                                .border(1.dp, TradingCardBorder, RoundedCornerShape(8.dp))
                                .clickable { importAccDropdownExpanded = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = TradingPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Target Account: ${importAccount?.name ?: "Select"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TradingTextPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Choose account",
                                tint = TradingTextMuted
                            )
                        }

                        DropdownMenu(
                            expanded = importAccDropdownExpanded,
                            onDismissRequest = { importAccDropdownExpanded = false },
                            modifier = Modifier
                                .background(TradingSurface)
                                .border(1.dp, TradingCardBorder)
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = acc.name,
                                            fontWeight = if (targetAccountId == acc.id) FontWeight.Bold else FontWeight.Normal,
                                            color = TradingTextPrimary
                                        )
                                    },
                                    onClick = {
                                        targetAccountId = acc.id
                                        importAccDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("import_account_option_${acc.id}")
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = csvText,
                        onValueChange = { csvText = it },
                        placeholder = {
                            Text(
                                text = "Symbol, Type, Lots, Entry, Exit, NetProfit, Date, Session, Setup\nEURUSD, BUY, 1.0, 1.0850, 1.0920, 700.0, 2026-07-25 14:00, London, Breakout",
                                fontSize = 11.sp,
                                color = TradingTextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("input_csv_text"),
                        colors = dialogTextFieldColors()
                    )

                    Button(
                        onClick = {
                            if (csvText.isNotBlank()) {
                                onImportCsv(csvText, targetAccountId)
                                csvText = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("button_import_csv"),
                        colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Parse & Import Trades", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showCreateAccount) {
        CreateAccountDialog(
            onDismiss = { showCreateAccount = false },
            onSubmit = { name, broker, balance, curr ->
                onCreateAccount(name, broker, balance, curr)
                showCreateAccount = false
            }
        )
    }
}

@Composable
private fun CreateAccountDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var broker by remember { mutableStateOf("FTMO") }
    var balanceStr by remember { mutableStateOf("100000") }
    var currency by remember { mutableStateOf("USD") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TradingSurface,
        title = { Text("Add Trading Account", color = TradingTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. $100k Evaluation)") },
                    colors = dialogTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = broker,
                    onValueChange = { broker = it },
                    label = { Text("Broker / Prop Firm (e.g. FTMO, FundedNext)") },
                    colors = dialogTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    label = { Text("Initial Balance ($)") },
                    colors = dialogTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val bal = balanceStr.toDoubleOrNull() ?: 10000.0
                        onSubmit(name, broker, bal, currency)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TradingPrimary)
            ) {
                Text("Save Account", color = TradingTextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = TradingSurfaceVariant)) {
                Text("Cancel", color = TradingTextMuted)
            }
        }
    )
}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TradingPrimary,
    unfocusedBorderColor = TradingCardBorder,
    focusedContainerColor = TradingSurfaceVariant,
    unfocusedContainerColor = TradingSurfaceVariant,
    focusedTextColor = TradingTextPrimary,
    unfocusedTextColor = TradingTextPrimary
)
