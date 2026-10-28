package com.example.moneypath.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneypath.data.DateUtils
import com.example.moneypath.data.MoneyPathRepository
import com.example.moneypath.data.models.*
import com.example.moneypath.ui.screens.CategoryStyleRegistry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MoneyPathViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MoneyPathRepository(application)

    val transactions: StateFlow<List<Transaction>> = repository.transactions
    val budgets: StateFlow<List<Budget>> = repository.budgets
    val goals: StateFlow<List<Goal>> = repository.goals
    val accounts: StateFlow<List<Account>> = repository.accounts
    val categories: StateFlow<List<CategoryItem>> = repository.categories
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val totalBudget: StateFlow<Double> = repository.totalBudget
    val bills: StateFlow<List<BillItem>> = repository.bills

    init {
        // Keep icon/colour lookups on every screen in step with the category list
        CategoryStyleRegistry.sync(repository.categories.value)
        viewModelScope.launch {
            repository.categories.collect { CategoryStyleRegistry.sync(it) }
        }
    }

    // Calculated fields (kept for screens that already use them)
    val totalBalance: Double
        get() = accounts.value.sumOf { it.balance }

    val totalIncome: Double
        get() = transactions.value.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

    val totalExpenses: Double
        get() = transactions.value.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    // ---- everything below only counts the CURRENT MONTH ----
    private fun thisMonth(txs: List<Transaction>) = txs.filter { DateUtils.isInCurrentMonth(it.date) }

    private fun buildBudgets(budgets: List<Budget>, txs: List<Transaction>): List<Budget> {
        val expenses = thisMonth(txs).filter { it.type == TransactionType.EXPENSE }
        return budgets.map { b ->
            b.copy(spentAmount = expenses.filter { it.category == b.category }.sumOf { it.amount })
        }
    }

    private fun buildSummary(total: Double, txs: List<Transaction>): BudgetSummary =
        BudgetSummary(
            total = total,
            spent = thisMonth(txs).filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        )

    // Available funds = total budget + income this month - expenses this month
    private fun buildAvailable(total: Double, txs: List<Transaction>): Double {
        val month = thisMonth(txs)
        val income = month.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = month.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return total + income - expenses
    }

    // Each budget with its spent amount. Use on Budget, Home and Summary so they always agree.
    val budgetsWithSpent: StateFlow<List<Budget>> =
        combine(repository.budgets, repository.transactions) { b, t -> buildBudgets(b, t) }
            .stateIn(
                viewModelScope, SharingStarted.Eagerly,
                buildBudgets(repository.budgets.value, repository.transactions.value)
            )

    // Overall numbers: total budget, spent, remaining, percent
    val budgetSummary: StateFlow<BudgetSummary> =
        combine(repository.totalBudget, repository.transactions) { total, t -> buildSummary(total, t) }
            .stateIn(
                viewModelScope, SharingStarted.Eagerly,
                buildSummary(repository.totalBudget.value, repository.transactions.value)
            )

    // The "Available balance" on Home
    val availableBalance: StateFlow<Double> =
        combine(repository.totalBudget, repository.transactions) { total, t -> buildAvailable(total, t) }
            .stateIn(
                viewModelScope, SharingStarted.Eagerly,
                buildAvailable(repository.totalBudget.value, repository.transactions.value)
            )

    fun registerAccount(name: String, identifier: String, pass: String): Boolean =
        repository.registerAccount(name, identifier, pass)

    fun loginAccount(identifier: String, pass: String): Boolean =
        repository.loginAccount(identifier, pass)

    // Transactions (return false if the data is invalid)
    fun addTransaction(transaction: Transaction): Boolean = repository.addTransaction(transaction)
    fun updateTransaction(transaction: Transaction): Boolean = repository.updateTransaction(transaction)
    fun deleteTransaction(id: String) = repository.deleteTransaction(id)

    // Budget (saveBudgetPlan returns false if allocations are negative or exceed the total)
    fun addBudget(budget: Budget) = repository.addBudget(budget)
    fun saveBudgetPlan(total: Double, allocations: Map<String, Double>): Boolean =
        repository.saveBudgetPlan(total, allocations)
    fun moveBudgetFunds(from: String, to: String, amount: Double): Boolean =
        repository.moveBudgetFunds(from, to, amount)

    // Categories
    fun addCategory(category: CategoryItem): Boolean = repository.addCategory(category)
    fun updateCategory(category: CategoryItem): Boolean = repository.updateCategory(category)
    fun deleteCategory(id: String): Boolean = repository.deleteCategory(id)

    // Bills
    fun addBill(bill: BillItem) = repository.addBill(bill)
    fun toggleBillPaid(id: String) = repository.toggleBillPaid(id)

    // Goals (add/update return false if the goal is invalid; deposit returns false if the
    // amount is 0 or less, or more than what is left to reach the target)
    fun addGoal(goal: Goal): Boolean = repository.addGoal(goal)
    fun updateGoal(goal: Goal): Boolean = repository.updateGoal(goal)
    fun deleteGoal(id: String) = repository.deleteGoal(id)
    fun addDepositToGoal(goalId: String, amount: Double): Boolean =
        repository.addDepositToGoal(goalId, amount)

    fun addAccount(account: Account) = repository.addAccount(account)
    fun markNotificationAsRead(id: String) = repository.markNotificationAsRead(id)
    fun markAllNotificationsAsRead() = repository.markAllNotificationsAsRead()
    fun dismissNotification(id: String) = repository.dismissNotification(id)
    fun clearAllNotifications() = repository.clearAllNotifications()
    fun updateProfile(name: String, email: String, imageUri: String) = repository.updateProfile(name, email, imageUri)
    fun updateCurrency(symbol: String, code: String) = repository.updateCurrency(symbol, code)
    fun updatePassword(currentPass: String, newPass: String): Boolean = repository.updatePassword(currentPass, newPass)
    fun updateLanguage(language: String) = repository.updateLanguage(language)
    fun toggleDarkMode(enabled: Boolean) = repository.toggleDarkMode(enabled)
    fun toggleBiometrics(enabled: Boolean) = repository.toggleBiometrics(enabled)
    fun toggleNotifications(enabled: Boolean) = repository.toggleNotifications(enabled)
}