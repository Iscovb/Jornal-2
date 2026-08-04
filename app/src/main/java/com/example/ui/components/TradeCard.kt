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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trade
import com.example.ui.theme.SessionAsianColor
import com.example.ui.theme.SessionLondonColor
import com.example.ui.theme.SessionNYColor
import com.example.ui.theme.TradingCardBorder
import com.example.ui.theme.TradingLossRed
import com.example.ui.theme.TradingLossRedContainer
import com.example.ui.theme.TradingPrimary
import com.example.ui.theme.TradingSurface
import com.example.ui.theme.TradingSurfaceVariant
import com.example.ui.theme.TradingTextMuted
import com.example.ui.theme.TradingTextPrimary
import com.example.ui.theme.TradingTextSecondary
import com.example.ui.theme.TradingWinGreen
import com.example.ui.theme.TradingWinGreenContainer
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

@Composable
fun TradeCard(
    trade: Trade,
    currency: String = "USD",
    onDelete: () -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isWin = trade.netProfit > 0.01
    val isLoss = trade.netProfit < -0.01
    val isBuy = trade.type.equals("BUY", ignoreCase = true)

    val currencyFormatter = remember(currency) {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US)
        try {
            fmt.currency = Currency.getInstance(currency)
        } catch (_: Exception) {}
        fmt
    }

    val dateStr = remember(trade.entryDate) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(trade.entryDate))
    }

    val sessionColor = when (trade.session.lowercase()) {
        "asian" -> SessionAsianColor
        "london" -> SessionLondonColor
        else -> SessionNYColor
    }

    Box(
        modifier = modifier
            .testTag("trade_card_${trade.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TradingSurface)
            .border(1.dp, TradingCardBorder, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(14.dp)
    ) {
        Column {
            // Top Row: Symbol, BUY/SELL badge, P&L
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // BUY / SELL Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isBuy) TradingWinGreenContainer else TradingLossRedContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBuy) Icons.Default.NorthEast else Icons.Default.SouthEast,
                                contentDescription = null,
                                tint = if (isBuy) TradingWinGreen else TradingLossRed,
                                modifier = Modifier.height(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = trade.type.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isBuy) TradingWinGreen else TradingLossRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = trade.symbol.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TradingTextPrimary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${trade.lotSize} lots",
                        fontSize = 12.sp,
                        color = TradingTextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Net P&L chip
                    Text(
                        text = "${if (isWin) "+" else ""}${currencyFormatter.format(trade.netProfit)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isWin) TradingWinGreen else if (isLoss) TradingLossRed else TradingTextMuted
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .testTag("delete_trade_${trade.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete trade",
                            tint = TradingTextMuted,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price specs & session/setup pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entry: ${trade.entryPrice}  ➔  Exit: ${trade.exitPrice}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TradingTextSecondary
                )

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = TradingTextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges row: Session, Setup tag, Emotions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Session Tag
                TagPill(
                    text = trade.session,
                    color = sessionColor
                )

                // Setup Tag
                TagPill(
                    text = trade.setupTag,
                    color = TradingPrimary
                )

                // Emotion Tag
                if (trade.emotions.isNotBlank()) {
                    TagPill(
                        text = trade.emotions,
                        color = TradingSurfaceVariant,
                        textColor = TradingTextSecondary
                    )
                }
            }

            if (trade.executionNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Notes: ${trade.executionNotes}",
                    fontSize = 11.sp,
                    color = TradingTextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TagPill(
    text: String,
    color: Color,
    textColor: Color = Color.White
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
