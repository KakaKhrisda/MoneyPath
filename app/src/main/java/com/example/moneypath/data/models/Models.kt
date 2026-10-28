package com.example.moneypath.data.models

import androidx.compose.ui.graphics.Color

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String, // e.g. "2025-02-24"
    val time: String = "12:00 PM",
    val type: TransactionType,
    val accountName: String = "Main Bank",
    val note: String = ""
)

data class Budget(
    val id: String,
    val category: String,
    val monthlyLimit: Double,
    val spentAmount: Double,
    val alertThresholdPercent: Int = 80
) {
    val progress: Float
        get() = if (monthlyLimit > 0) (spentAmount / monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f

    val isOverBudget: Boolean
        get() = spentAmount > monthlyLimit
}

data class Goal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String,
    val iconName: String = "Savings",
    val colorHex: Long = 0xFF10B981
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
}

data class Account(
    val id: String,
    val name: String,
    val type: String, // Bank, Cash, E-Wallet, Card, Savings
    val balance: Double,
    val accountNumber: String = "**** 1234",
    val colorHex: Long = 0xFF3B82F6
)

data class CategoryItem(
    val id: String,
    val name: String,
    val iconName: String,
    val isExpense: Boolean = true,
    val colorHex: Long = 0xFF6366F1
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val type: String = "Alert", // Alert, Reminder, System
    val key: String = "",       // identifies what caused it, so it is never duplicated
    val target: String = "",    // screen to open when tapped (a navigation route)
    val createdAt: Long = 0L    // when it appeared, used for "5 min ago"
)

data class UserProfile(
    val name: String = "John",
    val email: String = "john.doe@example.com",
    val profileImageUri: String = "",
    val currencySymbol: String = "MWK ",
    val currencyCode: String = "MWK",
    val language: String = "English (US)",
    val isDarkMode: Boolean = false,
    val isBiometricsEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true
)

// NEW: overall budget numbers used by Home, Budget and Summary screens
data class BudgetSummary(
    val total: Double = 0.0,
    val spent: Double = 0.0
) {
    val remaining: Double
        get() = total - spent

    val percentSpent: Int
        get() = if (total > 0) ((spent / total) * 100).toInt() else 0

    val ringProgress: Float
        get() = if (total > 0) (spent / total).toFloat().coerceIn(0f, 1f) else 0f
}

data class BillItem(
    val id: String,
    val title: String,
    val amount: Double,
    val dueDate: String,
    val category: String,
    val isPaid: Boolean = false
)
