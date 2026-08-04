package com.example.data.parser

import com.example.data.model.Trade
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvImportParser {

    fun parseCsvText(csvText: String, accountId: Long): List<Trade> {
        val lines = csvText.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        if (lines.isEmpty()) return emptyList()

        val parsedTrades = mutableListOf<Trade>()

        // Check header line or auto-detect index mapping
        val firstLine = lines.first()
        val hasHeader = firstLine.contains("symbol", ignoreCase = true) || 
                         firstLine.contains("pair", ignoreCase = true) ||
                         firstLine.contains("type", ignoreCase = true) ||
                         firstLine.contains("profit", ignoreCase = true)

        val dataLines = if (hasHeader) lines.drop(1) else lines

        val dateFormatters = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
            SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.US),
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US),
            SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US)
        )

        val now = System.currentTimeMillis()

        for ((index, line) in dataLines.withIndex()) {
            val tokens = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (tokens.size < 4) continue

            try {
                // Expected format flexible mapping:
                // Token options:
                // 1: Symbol (e.g., EURUSD)
                // 2: Type (BUY / SELL)
                // 3: Lots / EntryPrice
                // 4: ExitPrice / NetProfit
                
                var symbol = "EURUSD"
                var type = "BUY"
                var entryPrice = 1.0850
                var exitPrice = 1.0900
                var lotSize = 1.0
                var netProfit = 0.0
                var session = "London"
                var setupTag = "Breakout"
                var entryDate = now - (dataLines.size - index) * 86400000L

                // Intelligently parse tokens
                symbol = tokens.getOrNull(0)?.uppercase() ?: "EURUSD"
                val rawType = tokens.getOrNull(1)?.uppercase() ?: "BUY"
                type = if (rawType.contains("SELL") || rawType.contains("SHORT")) "SELL" else "BUY"

                lotSize = tokens.getOrNull(2)?.toDoubleOrNull() ?: 1.0
                entryPrice = tokens.getOrNull(3)?.toDoubleOrNull() ?: 1.0
                exitPrice = tokens.getOrNull(4)?.toDoubleOrNull() ?: 1.0
                netProfit = tokens.getOrNull(5)?.toDoubleOrNull() ?: (
                    if (type == "BUY") (exitPrice - entryPrice) * lotSize * 10000 else (entryPrice - exitPrice) * lotSize * 10000
                )

                if (tokens.size > 6) {
                    val dateStr = tokens[6]
                    for (df in dateFormatters) {
                        try {
                            val parsedDate = df.parse(dateStr)
                            if (parsedDate != null) {
                                entryDate = parsedDate.time
                                break
                            }
                        } catch (_: Exception) {}
                    }
                }

                if (tokens.size > 7) session = tokens[7]
                if (tokens.size > 8) setupTag = tokens[8]

                val status = when {
                    netProfit > 0.01 -> "WIN"
                    netProfit < -0.01 -> "LOSS"
                    else -> "BREAKEVEN"
                }

                parsedTrades.add(
                    Trade(
                        accountId = accountId,
                        symbol = symbol,
                        type = type,
                        entryPrice = entryPrice,
                        exitPrice = exitPrice,
                        stopLoss = if (type == "BUY") entryPrice * 0.995 else entryPrice * 1.005,
                        takeProfit = if (type == "BUY") entryPrice * 1.01 else entryPrice * 0.99,
                        lotSize = lotSize,
                        riskPercent = 1.0,
                        riskAmount = 100.0,
                        netProfit = netProfit,
                        session = session,
                        setupTag = setupTag,
                        emotions = "Disciplined",
                        executionNotes = "Imported from CSV",
                        entryDate = entryDate,
                        exitDate = entryDate + 3600000L,
                        status = status
                    )
                )
            } catch (_: Exception) {
                // Ignore corrupt row
            }
        }

        return parsedTrades
    }
}
