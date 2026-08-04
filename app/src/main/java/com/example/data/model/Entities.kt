package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "trading_accounts")
data class TradingAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g., "Evaluation Account $100k", "Live Account"
    val brokerName: String = "Generic Broker",
    val initialBalance: Double = 10000.0,
    val currency: String = "USD",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "trades",
    foreignKeys = [
        ForeignKey(
            entity = TradingAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["accountId"]), Index(value = ["entryDate"])]
)
data class Trade(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val symbol: String, // e.g. EURUSD, BTCUSDT, XAUUSD
    val type: String, // "BUY" or "SELL"
    val entryPrice: Double,
    val exitPrice: Double,
    val stopLoss: Double = 0.0,
    val takeProfit: Double = 0.0,
    val lotSize: Double = 1.0, // Lots or contracts
    val riskPercent: Double = 1.0,
    val riskAmount: Double = 100.0,
    val netProfit: Double = 0.0, // Auto-calculated or manual P&L
    val commission: Double = 0.0,
    val session: String = "London", // "Asian", "London", "New York"
    val setupTag: String = "Breakout", // e.g. Liquidity Sweep, FVG, Breakout
    val emotions: String = "Disciplined", // Comma-separated emotions
    val screenshotUri: String? = null,
    val executionNotes: String = "",
    val lessonsLearned: String = "",
    val entryDate: Long = System.currentTimeMillis(),
    val exitDate: Long = System.currentTimeMillis(),
    val status: String = "WIN" // "WIN", "LOSS", "BREAKEVEN"
)

@Entity(tableName = "playbooks")
data class Playbook(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // e.g. "ICT London Liquidity Sweep"
    val description: String = "",
    val timeframe: String = "15m",
    val winRateGoal: Double = 70.0,
    val targetRiskReward: Double = 2.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "strategy_rules",
    foreignKeys = [
        ForeignKey(
            entity = Playbook::class,
            parentColumns = ["id"],
            childColumns = ["playbookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playbookId"])]
)
data class StrategyRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playbookId: Long,
    val ruleText: String, // e.g. "1. HTF Trend Aligned"
    val isRequired: Boolean = true
)

@Entity(
    tableName = "trade_rule_compliance",
    primaryKeys = ["tradeId", "ruleId"],
    foreignKeys = [
        ForeignKey(
            entity = Trade::class,
            parentColumns = ["id"],
            childColumns = ["tradeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StrategyRule::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tradeId"]), Index(value = ["ruleId"])]
)
data class TradeRuleCompliance(
    val tradeId: Long,
    val ruleId: Long,
    val isFollowed: Boolean
)

// UI Aggregate Data Models
data class PerformanceMetrics(
    val netProfit: Double = 0.0,
    val winRatePercent: Double = 0.0,
    val profitFactor: Double = 0.0,
    val averageWin: Double = 0.0,
    val averageLoss: Double = 0.0,
    val expectancyR: Double = 0.0,
    val totalTrades: Int = 0,
    val winsCount: Int = 0,
    val lossesCount: Int = 0,
    val breakevenCount: Int = 0,
    val totalRisked: Double = 0.0,
    val maxDrawdown: Double = 0.0,
    val ruleCompliancePercent: Double = 0.0
)

data class EquityPoint(
    val timestamp: Long,
    val cumulativePnL: Double,
    val tradeNumber: Int,
    val symbol: String
)

data class BreakdownItem(
    val category: String,
    val totalPnL: Double,
    val winRatePercent: Double,
    val tradeCount: Int
)

data class DailyPnL(
    val dateString: String, // YYYY-MM-DD
    val dayOfMonth: Int,
    val totalPnL: Double,
    val tradeCount: Int,
    val trades: List<Trade> = emptyList()
)
