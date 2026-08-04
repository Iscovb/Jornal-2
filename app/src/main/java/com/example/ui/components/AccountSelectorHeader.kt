package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@Composable
fun AccountSelectorHeader(
    accounts: List<TradingAccount>,
    selectedAccountId: Long?,
    selectedAccountName: String,
    onSelectAccount: (Long?) -> Unit,
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .testTag("account_selector_header")
            .fillMaxWidth()
            .background(TradingSurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown Selector Button
            Box {
                Row(
                    modifier = Modifier
                        .testTag("account_dropdown_trigger")
                        .clip(RoundedCornerShape(8.dp))
                        .background(TradingSurface)
                        .border(1.dp, TradingCardBorder, RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = TradingPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ACTIVE ACCOUNT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = selectedAccountName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TradingTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand account menu",
                        tint = TradingTextMuted
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(TradingSurface)
                        .border(1.dp, TradingCardBorder)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "All Accounts Combined",
                                fontWeight = if (selectedAccountId == null) FontWeight.Bold else FontWeight.Normal,
                                color = TradingTextPrimary
                            )
                        },
                        onClick = {
                            onSelectAccount(null)
                            expanded = false
                        },
                        modifier = Modifier.testTag("account_item_all")
                    )

                    accounts.forEach { acc ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = acc.name,
                                        fontWeight = if (selectedAccountId == acc.id) FontWeight.Bold else FontWeight.Normal,
                                        color = TradingTextPrimary
                                    )
                                    Text(
                                        text = "${acc.brokerName} • ${acc.currency} ${String.format("%,.0f", acc.initialBalance)}",
                                        fontSize = 10.sp,
                                        color = TradingTextMuted
                                    )
                                }
                            },
                            onClick = {
                                onSelectAccount(acc.id)
                                expanded = false
                            },
                            modifier = Modifier.testTag("account_item_${acc.id}")
                        )
                    }
                }
            }

            // New Account Action Pill
            Box(
                modifier = Modifier
                    .testTag("create_account_button")
                    .clip(RoundedCornerShape(8.dp))
                    .background(TradingPrimary)
                    .clickable { onAddAccountClick() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = TradingTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextPrimary
                    )
                }
            }
        }
    }
}
