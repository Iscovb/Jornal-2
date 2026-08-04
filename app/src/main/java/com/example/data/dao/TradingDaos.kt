package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Playbook
import com.example.data.model.StrategyRule
import com.example.data.model.Trade
import com.example.data.model.TradeRuleCompliance
import com.example.data.model.TradingAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM trading_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<TradingAccount>>

    @Query("SELECT * FROM trading_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): TradingAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: TradingAccount): Long

    @Update
    suspend fun updateAccount(account: TradingAccount)

    @Delete
    suspend fun deleteAccount(account: TradingAccount)
}

@Dao
interface TradeDao {
    @Query("SELECT * FROM trades ORDER BY entryDate DESC")
    fun getAllTrades(): Flow<List<Trade>>

    @Query("SELECT * FROM trades WHERE accountId = :accountId ORDER BY entryDate DESC")
    fun getTradesByAccount(accountId: Long): Flow<List<Trade>>

    @Query("SELECT * FROM trades WHERE id = :id LIMIT 1")
    suspend fun getTradeById(id: Long): Trade?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: Trade): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<Trade>)

    @Update
    suspend fun updateTrade(trade: Trade)

    @Delete
    suspend fun deleteTrade(trade: Trade)

    @Query("DELETE FROM trades WHERE accountId = :accountId")
    suspend fun deleteAllTradesForAccount(accountId: Long)
}

@Dao
interface PlaybookDao {
    @Query("SELECT * FROM playbooks ORDER BY createdAt DESC")
    fun getAllPlaybooks(): Flow<List<Playbook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaybook(playbook: Playbook): Long

    @Query("SELECT * FROM strategy_rules WHERE playbookId = :playbookId")
    fun getRulesForPlaybook(playbookId: Long): Flow<List<StrategyRule>>

    @Query("SELECT * FROM strategy_rules")
    fun getAllRules(): Flow<List<StrategyRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: StrategyRule): Long

    @Delete
    suspend fun deletePlaybook(playbook: Playbook)

    @Query("DELETE FROM strategy_rules WHERE id = :ruleId")
    suspend fun deleteRule(ruleId: Long)
}

@Dao
interface ComplianceDao {
    @Query("SELECT * FROM trade_rule_compliance WHERE tradeId = :tradeId")
    suspend fun getComplianceForTrade(tradeId: Long): List<TradeRuleCompliance>

    @Query("SELECT * FROM trade_rule_compliance")
    fun getAllCompliance(): Flow<List<TradeRuleCompliance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompliance(complianceList: List<TradeRuleCompliance>)
}
