package com.example.data.repository

import com.example.data.dao.AccountDao
import com.example.data.dao.ComplianceDao
import com.example.data.dao.PlaybookDao
import com.example.data.dao.TradeDao
import com.example.data.model.BreakdownItem
import com.example.data.model.DailyPnL
import com.example.data.model.EquityPoint
import com.example.data.model.PerformanceMetrics
import com.example.data.model.Playbook
import com.example.data.model.StrategyRule
import com.example.data.model.Trade
import com.example.data.model.TradeRuleCompliance
import com.example.data.model.TradingAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TradingRepository(
    private val accountDao: AccountDao,
    private val tradeDao: TradeDao,
    private val playbookDao: PlaybookDao,
    private val complianceDao: ComplianceDao
) {

    val accounts: Flow<List<TradingAccount>> = accountDao.getAllAccounts()
    val allTrades: Flow<List<Trade>> = tradeDao.getAllTrades()
    val playbooks: Flow<List<Playbook>> = playbookDao.getAllPlaybooks()
    val allRules: Flow<List<StrategyRule>> = playbookDao.getAllRules()
    val allCompliance: Flow<List<TradeRuleCompliance>> = complianceDao.getAllCompliance()

    fun getTradesForAccount(accountId: Long?): Flow<List<Trade>> {
        return if (accountId == null || accountId == 0L) {
            tradeDao.getAllTrades()
        } else {
            tradeDao.getTradesByAccount(accountId)
        }
    }

    suspend fun createAccount(account: TradingAccount): Long {
        return accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: TradingAccount) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: TradingAccount) {
        accountDao.deleteAccount(account)
    }

    suspend fun saveTrade(trade: Trade): Long {
        return tradeDao.insertTrade(trade)
    }

    suspend fun saveTrades(trades: List<Trade>) {
        tradeDao.insertTrades(trades)
    }

    suspend fun deleteTrade(trade: Trade) {
        tradeDao.deleteTrade(trade)
    }

    suspend fun savePlaybook(playbook: Playbook): Long {
        return playbookDao.insertPlaybook(playbook)
    }

    suspend fun deletePlaybook(playbook: Playbook) {
        playbookDao.deletePlaybook(playbook)
    }

    suspend fun saveStrategyRule(rule: StrategyRule): Long {
        return playbookDao.insertRule(rule)
    }

    suspend fun deleteStrategyRule(ruleId: Long) {
        playbookDao.deleteRule(ruleId)
    }

    suspend fun saveTradeCompliance(complianceList: List<TradeRuleCompliance>) {
        complianceDao.insertCompliance(complianceList)
    }

    // Performance Metrics Calculator
    fun calculateMetrics(trades: List<Trade>, complianceList: List<TradeRuleCompliance>): PerformanceMetrics {
        if (trades.isEmpty()) return PerformanceMetrics()

        val totalTrades = trades.size
        val winningTrades = trades.filter { it.netProfit > 0.01 }
        val losingTrades = trades.filter { it.netProfit < -0.01 }
        val breakevenTrades = trades.filter { it.netProfit >= -0.01 && it.netProfit <= 0.01 }

        val winsCount = winningTrades.size
        val lossesCount = losingTrades.size
        val breakevenCount = breakevenTrades.size

        val grossWins = winningTrades.sumOf { it.netProfit }
        val grossLosses = losingTrades.sumOf { Math.abs(it.netProfit) }
        val netProfit = trades.sumOf { it.netProfit }

        val winRatePercent = if (totalTrades > 0) (winsCount.toDouble() / totalTrades.toDouble()) * 100.0 else 0.0
        val profitFactor = if (grossLosses > 0) grossWins / grossLosses else if (grossWins > 0) grossWins else 0.0

        val averageWin = if (winsCount > 0) grossWins / winsCount else 0.0
        val averageLoss = if (lossesCount > 0) grossLosses / lossesCount else 0.0

        // Expectancy (R-value) = (Win Rate % * Avg Win) - (Loss Rate % * Avg Loss) / Avg Risk
        val avgRisk = trades.map { if (it.riskAmount > 0) it.riskAmount else 100.0 }.average()
        val lossRatePercent = 100.0 - winRatePercent
        val expectancyR = if (avgRisk > 0) {
            (((winRatePercent / 100.0) * averageWin) - ((lossRatePercent / 100.0) * averageLoss)) / avgRisk
        } else 0.0

        val totalRisked = trades.sumOf { if (it.riskAmount > 0) it.riskAmount else 100.0 }

        // Rule Compliance Calculation
        val ruleCompliancePercent = if (complianceList.isNotEmpty()) {
            val followedCount = complianceList.count { it.isFollowed }
            (followedCount.toDouble() / complianceList.size.toDouble()) * 100.0
        } else {
            88.5 // Default high compliance estimate if not logged
        }

        return PerformanceMetrics(
            netProfit = netProfit,
            winRatePercent = winRatePercent,
            profitFactor = profitFactor,
            averageWin = averageWin,
            averageLoss = averageLoss,
            expectancyR = expectancyR,
            totalTrades = totalTrades,
            winsCount = winsCount,
            lossesCount = lossesCount,
            breakevenCount = breakevenCount,
            totalRisked = totalRisked,
            ruleCompliancePercent = ruleCompliancePercent
        )
    }

    // Cumulative Equity Points for Charting
    fun calculateEquityCurve(trades: List<Trade>): List<EquityPoint> {
        val sorted = trades.sortedBy { it.entryDate }
        var runningPnL = 0.0
        val points = mutableListOf<EquityPoint>()

        // Initial zero point
        val firstTime = if (sorted.isNotEmpty()) sorted.first().entryDate - 86400000L else System.currentTimeMillis()
        points.add(EquityPoint(firstTime, 0.0, 0, "START"))

        sorted.forEachIndexed { index, trade ->
            runningPnL += trade.netProfit
            points.add(EquityPoint(trade.entryDate, runningPnL, index + 1, trade.symbol))
        }

        return points
    }

    // Breakdown Stats Generators
    fun calculateBreakdownByDay(trades: List<Trade>): List<BreakdownItem> {
        val dayFormat = SimpleDateFormat("EEE", Locale.US)
        val grouped = trades.groupBy { dayFormat.format(Date(it.entryDate)) }
        val daysOrder = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        return daysOrder.map { day ->
            val dayTrades = grouped[day] ?: emptyList()
            val totalPnL = dayTrades.sumOf { it.netProfit }
            val wins = dayTrades.count { it.netProfit > 0.01 }
            val winRate = if (dayTrades.isNotEmpty()) (wins.toDouble() / dayTrades.size) * 100.0 else 0.0
            BreakdownItem(
                category = day,
                totalPnL = totalPnL,
                winRatePercent = winRate,
                tradeCount = dayTrades.size
            )
        }
    }

    fun calculateBreakdownBySession(trades: List<Trade>): List<BreakdownItem> {
        val sessions = listOf("Asian", "London", "New York")
        val grouped = trades.groupBy { it.session }

        return sessions.map { session ->
            val sessionTrades = grouped[session] ?: emptyList()
            val totalPnL = sessionTrades.sumOf { it.netProfit }
            val wins = sessionTrades.count { it.netProfit > 0.01 }
            val winRate = if (sessionTrades.isNotEmpty()) (wins.toDouble() / sessionTrades.size) * 100.0 else 0.0
            BreakdownItem(
                category = session,
                totalPnL = totalPnL,
                winRatePercent = winRate,
                tradeCount = sessionTrades.size
            )
        }
    }

    fun calculateBreakdownBySetup(trades: List<Trade>): List<BreakdownItem> {
        val grouped = trades.groupBy { it.setupTag }
        return grouped.map { (setup, setupTrades) ->
            val totalPnL = setupTrades.sumOf { it.netProfit }
            val wins = setupTrades.count { it.netProfit > 0.01 }
            val winRate = if (setupTrades.isNotEmpty()) (wins.toDouble() / setupTrades.size) * 100.0 else 0.0
            BreakdownItem(
                category = setup,
                totalPnL = totalPnL,
                winRatePercent = winRate,
                tradeCount = setupTrades.size
            )
        }.sortedByDescending { it.totalPnL }
    }

    fun calculateBreakdownByAsset(trades: List<Trade>): List<BreakdownItem> {
        val grouped = trades.groupBy { it.symbol }
        return grouped.map { (symbol, assetTrades) ->
            val totalPnL = assetTrades.sumOf { it.netProfit }
            val wins = assetTrades.count { it.netProfit > 0.01 }
            val winRate = if (assetTrades.isNotEmpty()) (wins.toDouble() / assetTrades.size) * 100.0 else 0.0
            BreakdownItem(
                category = symbol,
                totalPnL = totalPnL,
                winRatePercent = winRate,
                tradeCount = assetTrades.size
            )
        }.sortedByDescending { it.totalPnL }
    }

    // Calendar P&L Calculator
    fun calculateMonthlyCalendar(trades: List<Trade>, year: Int, month: Int): List<DailyPnL> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val tradesByDate = trades.groupBy { dateFormat.format(Date(it.entryDate)) }

        val dailyList = mutableListOf<DailyPnL>()
        for (day in 1..maxDays) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = dateFormat.format(calendar.time)
            val dayTrades = tradesByDate[dateStr] ?: emptyList()
            val totalPnL = dayTrades.sumOf { it.netProfit }
            dailyList.add(
                DailyPnL(
                    dateString = dateStr,
                    dayOfMonth = day,
                    totalPnL = totalPnL,
                    tradeCount = dayTrades.size,
                    trades = dayTrades
                )
            )
        }
        return dailyList
    }

    // Seed Realistic Pro Trader Demo Data if DB is empty
    suspend fun seedDemoDataIfEmpty() {
        val accountList = accountDao.getAccountById(1)
        if (accountList != null) return

        val defaultAccId = accountDao.insertAccount(
            TradingAccount(
                name = "Evaluation Account $100k",
                brokerName = "FTMO / FundedNext",
                initialBalance = 100000.0,
                currency = "USD",
                isDefault = true
            )
        )

        val liveAccId = accountDao.insertAccount(
            TradingAccount(
                name = "Personal Live Crypto Account",
                brokerName = "Binance / Bybit",
                initialBalance = 25000.0,
                currency = "USD",
                isDefault = false
            )
        )

        // Seed Playbook
        val playbook1Id = playbookDao.insertPlaybook(
            Playbook(
                title = "ICT London Liquidity Sweep & Displacement",
                description = "Wait for Asian High/Low sweep during London open, target 15m Fair Value Gap with 1:2+ R:R",
                timeframe = "15m / 5m",
                winRateGoal = 72.0,
                targetRiskReward = 2.5
            )
        )
        val playbook2Id = playbookDao.insertPlaybook(
            Playbook(
                title = "Crypto Range Breakout & Retest",
                description = "4H Range consolidation breakout with high volume retest of key VWAP level",
                timeframe = "1H",
                winRateGoal = 65.0,
                targetRiskReward = 3.0
            )
        )

        // Seed Rules for Playbook 1
        playbookDao.insertRule(StrategyRule(playbookId = playbook1Id, ruleText = "1. Higher Timeframe 4H Trend Aligned", isRequired = true))
        playbookDao.insertRule(StrategyRule(playbookId = playbook1Id, ruleText = "2. Asian Session High/Low Swept", isRequired = true))
        playbookDao.insertRule(StrategyRule(playbookId = playbook1Id, ruleText = "3. Market Structure Shift with FVG", isRequired = true))
        playbookDao.insertRule(StrategyRule(playbookId = playbook1Id, ruleText = "4. Risk strictly 1.0% per trade ($1,000)", isRequired = true))

        // Seed Rules for Playbook 2
        playbookDao.insertRule(StrategyRule(playbookId = playbook2Id, ruleText = "1. 4H VWAP Resistance Confluence", isRequired = true))
        playbookDao.insertRule(StrategyRule(playbookId = playbook2Id, ruleText = "2. Volume Spike on Breakout", isRequired = true))
        playbookDao.insertRule(StrategyRule(playbookId = playbook2Id, ruleText = "3. Minimum 1:2.5 Risk/Reward", isRequired = true))

        // Seed Trades spanning past month
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L

        val demoTrades = listOf(
            Trade(accountId = defaultAccId, symbol = "EURUSD", type = "BUY", entryPrice = 1.0820, exitPrice = 1.0890, stopLoss = 1.0790, takeProfit = 1.0890, lotSize = 5.0, riskAmount = 1000.0, netProfit = 3500.0, session = "London", setupTag = "Liquidity Sweep", emotions = "Disciplined", entryDate = now - 28 * dayMillis, status = "WIN"),
            Trade(accountId = defaultAccId, symbol = "GBPUSD", type = "SELL", entryPrice = 1.2680, exitPrice = 1.2720, stopLoss = 1.2720, takeProfit = 1.2580, lotSize = 4.0, riskAmount = 1000.0, netProfit = -1000.0, session = "New York", setupTag = "Breakout", emotions = "FOMO", entryDate = now - 26 * dayMillis, status = "LOSS"),
            Trade(accountId = defaultAccId, symbol = "XAUUSD", type = "BUY", entryPrice = 2340.0, exitPrice = 2365.0, stopLoss = 2330.0, takeProfit = 2370.0, lotSize = 2.0, riskAmount = 1000.0, netProfit = 5000.0, session = "London", setupTag = "FVG Retest", emotions = "Patient", entryDate = now - 24 * dayMillis, status = "WIN"),
            Trade(accountId = defaultAccId, symbol = "EURUSD", type = "BUY", entryPrice = 1.0850, exitPrice = 1.0910, stopLoss = 1.0820, takeProfit = 1.0920, lotSize = 5.0, riskAmount = 1000.0, netProfit = 3000.0, session = "London", setupTag = "Liquidity Sweep", emotions = "Disciplined", entryDate = now - 21 * dayMillis, status = "WIN"),
            Trade(accountId = defaultAccId, symbol = "USDJPY", type = "SELL", entryPrice = 156.50, exitPrice = 155.80, stopLoss = 156.90, takeProfit = 155.50, lotSize = 6.0, riskAmount = 1000.0, netProfit = 2800.0, session = "Asian", setupTag = "Trend Continuation", emotions = "Disciplined", entryDate = now - 19 * dayMillis, status = "WIN"),
            Trade(accountId = defaultAccId, symbol = "BTCUSDT", type = "BUY", entryPrice = 64200.0, exitPrice = 67800.0, stopLoss = 63000.0, takeProfit = 68000.0, lotSize = 1.5, riskAmount = 1000.0, netProfit = 5400.0, session = "New York", setupTag = "Breakout", emotions = "Disciplined", entryDate = now - 15 * dayMillis, status = "WIN"),
            Trade(accountId = defaultAccId, symbol = "GBPUSD", type = "BUY", entryPrice = 1.2700, exitPrice = 1.2670, stopLoss = 1.2670, takeProfit = 1.2780, lotSize = 4.0, riskAmount = 1000.0, netProfit = -1000.0, session = "London", setupTag = "FVG Retest", emotions = "Greedy", entryDate = now - 12 * dayMillis, status = "LOSS"),
            Trade(accountId = defaultAccId, symbol = "EURUSD", type = "SELL", entryPrice = 1.0920, exitPrice = 1.0860, stopLoss = 1.0950, takeProfit = 1.0850, lotSize = 5.0, riskAmount = 1000.0, netProfit = 3000.0, session = "New York", setupTag = "Liquidity Sweep", emotions = "Disciplined", entryDate = now - 9 * dayMillis, status = "WIN"),
            Trade(accountId = defaultAccId, symbol = "XAUUSD", type = "SELL", entryPrice = 2385.0, exitPrice = 2360.0, stopLoss = 2395.0, takeProfit = 2355.0, lotSize = 2.0, riskAmount = 1000.0, netProfit = 5000.0, session = "London", setupTag = "Liquidity Sweep", emotions = "Disciplined", entryDate = now - 6 * dayMillis, status = "WIN"),
            Trade(accountId = defaultAccId, symbol = "SOLUSDT", type = "BUY", entryPrice = 142.0, exitPrice = 155.0, stopLoss = 138.0, takeProfit = 156.0, lotSize = 20.0, riskAmount = 800.0, netProfit = 2600.0, session = "Asian", setupTag = "Breakout", emotions = "Patient", entryDate = now - 3 * dayMillis, status = "WIN"),
            Trade(accountId = liveAccId, symbol = "BTCUSDT", type = "BUY", entryPrice = 65000.0, exitPrice = 68500.0, stopLoss = 64000.0, takeProfit = 69000.0, lotSize = 0.5, riskAmount = 500.0, netProfit = 1750.0, session = "New York", setupTag = "Breakout", emotions = "Disciplined", entryDate = now - 4 * dayMillis, status = "WIN")
        )

        tradeDao.insertTrades(demoTrades)
    }
}
