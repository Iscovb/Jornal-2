package com.example.data.model

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val defaultRiskPercent: Double = 1.0,
    val targetWinRate: Double = 70.0,
    val minRiskReward: Double = 2.0,
    val maxDailyLoss: Double = 3.0,
    val traderTitle: String = "Forex & Crypto Trader",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)
