package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AccountDao
import com.example.data.dao.ComplianceDao
import com.example.data.dao.PlaybookDao
import com.example.data.dao.TradeDao
import com.example.data.model.Playbook
import com.example.data.model.StrategyRule
import com.example.data.model.Trade
import com.example.data.model.TradeRuleCompliance
import com.example.data.model.TradingAccount

@Database(
    entities = [
        TradingAccount::class,
        Trade::class,
        Playbook::class,
        StrategyRule::class,
        TradeRuleCompliance::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun tradeDao(): TradeDao
    abstract fun playbookDao(): PlaybookDao
    abstract fun complianceDao(): ComplianceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "journnex_trading_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
