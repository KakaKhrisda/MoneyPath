package com.example.moneypath.data

import android.content.Context
import com.example.moneypath.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

// password is stored as a SHA-256 hash, never as plain text
data class RegisteredAccount(
    val name: String,
    val identifier: String, // email or phone
    val password: String
)

// One alert that should currently exist, worked out from the data
private data class AlertSpec(
    val key: String,
    val title: String,
    val message: String,
    val type: String,
    val target: String
)

class MoneyPathRepository(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("moneypath_data", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_TX = "transactions"
        const val KEY_TOTAL = "total_budget"
        const val KEY_BUDGETS = "budgets"
        const val KEY_ACCOUNTS = "account_balances"
        const val KEY_ACCOUNTS_FULL = "accounts_v2"
        const val KEY_GOALS = "goals_v2"
        const val KEY_PROFILE = "profile"
        const val KEY_USERS = "registered_users"
        const val KEY_CATEGORIES = "categories"
        const val KEY_NOTIFICATIONS = "notifications_v2"
        const val KEY_DISMISSED = "dismissed_alerts"
        const val KEY_BILLS = "bills_v2"

        const val LOW_BALANCE = 10000.0        // account balance below this = low balance alert
        const val LARGE_EXPENSE_SHARE = 0.2    // one expense >= 20% of the total budget
        const val GOAL_DEADLINE_DAYS = 5L      // warn this many days before a goal's date
    }

    private fun hash(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }

    // ---------- Login accounts ----------
    private val registeredAccounts: MutableMap<String, RegisteredAccount> = loadRegisteredAccounts()

    private fun loadRegisteredAccounts(): MutableMap<String, RegisteredAccount> {
        val map = mutableMapOf(
            "john.doe@example.com" to RegisteredAccount("John Doe", "john.doe@example.com", hash("password123")),
            "john@example.com" to RegisteredAccount("John Doe", "john@example.com", hash("password123"))
        )
        prefs.getString(KEY_USERS, null)?.let { raw ->
            runCatching {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val id = o.getString("identifier")
                    map[id.trim().lowercase()] =
                        RegisteredAccount(o.getString("name"), id, o.getString("password"))
                }
            }
        }
        return map
    }

    // ---------- Transactions ----------
    private fun seedDate(daysAgo: Long): String {
        val today = LocalDate.now()
        val d = today.minusDays(daysAgo)
        val first = today.withDayOfMonth(1)
        return DateUtils.format(if (d.isBefore(first)) first else d)
    }

    private fun seedTransactions(): List<Transaction> = listOf(
        Transaction("t3", "Lunch", 5000.0, "Food & Dining", seedDate(0), "12:30 PM", TransactionType.EXPENSE, "Cash Wallet", "Lunch at the university canteen"),
        Transaction("t4", "Minibus fare", 2000.0, "Transportation", seedDate(1), "10:15 AM", TransactionType.EXPENSE, "Cash Wallet", "Ride to town"),
        Transaction("t7", "Electricity", 15000.0, "Bills & Utilities", seedDate(2), "04:20 PM", TransactionType.EXPENSE, "Main Checking", "Electricity bill"),
        Transaction("t6", "Movie ticket", 7500.0, "Entertainment", seedDate(3), "02:10 PM", TransactionType.EXPENSE, "Cash Wallet", "Cinema"),
        Transaction("t2", "Groceries", 8000.0, "Food & Dining", seedDate(4), "11:45 AM", TransactionType.EXPENSE, "Cash Wallet", "Weekly grocery shopping")
    )

    private fun loadTransactions(): List<Transaction>? {
        val raw = prefs.getString(KEY_TX, null) ?: return null
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Transaction(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    title = o.optString("title", "Expense"),
                    amount = o.optDouble("amount", 0.0),
                    category = o.optString("category", "Uncategorized"),
                    date = o.optString("date", DateUtils.format(LocalDate.now())),
                    time = o.optString("time", "12:00 PM"),
                    type = runCatching { TransactionType.valueOf(o.optString("type", "EXPENSE")) }
                        .getOrDefault(TransactionType.EXPENSE),
                    accountName = o.optString("accountName", "Cash Wallet"),
                    note = o.optString("note", "")
                )
            }
        }.getOrNull()
    }

    private val _transactions = MutableStateFlow(loadTransactions() ?: seedTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // ---------- Budget ----------
    private val _totalBudget =
        MutableStateFlow(prefs.getString(KEY_TOTAL, null)?.toDoubleOrNull() ?: 100000.0)
    val totalBudget: StateFlow<Double> = _totalBudget.asStateFlow()

    private fun seedBudgets(): List<Budget> = listOf(
        Budget("b1", "Food & Dining", 35000.0, 0.0, 80),
        Budget("b2", "Transportation", 15000.0, 0.0, 80),
        Budget("b3", "Bills & Utilities", 20000.0, 0.0, 80),
        Budget("b4", "Entertainment", 15000.0, 0.0, 80),
        Budget("b5", "Shopping", 15000.0, 0.0, 80)
    )

    private fun loadBudgets(seed: List<Budget>): List<Budget> {
        val raw = prefs.getString(KEY_BUDGETS, null) ?: return seed
        return runCatching {
            val arr = JSONArray(raw)
            val loadedList = List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Budget(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    category = o.optString("category", "General"),
                    monthlyLimit = o.optDouble("limit", 0.0),
                    spentAmount = 0.0,
                    alertThresholdPercent = 80
                )
            }
            val loadedCategories = loadedList.map { it.category }.toSet()
            val missingSeed = seed.filter { it.category !in loadedCategories }
            loadedList + missingSeed
        }.getOrDefault(seed)
    }

    // Category limits only. The spent amount is calculated in the ViewModel
    private val _budgets = MutableStateFlow(loadBudgets(seedBudgets()))
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    // ---------- Goals ----------
    private fun seedGoals(): List<Goal> {
        val today = LocalDate.now()
        return listOf(
            Goal("g1", "Emergency Fund", 200000.0, 80000.0, DateUtils.format(today.plusMonths(6)), "Security", 0xFF0D9488),
            Goal("g2", "New Laptop", 100000.0, 50000.0, DateUtils.format(today.plusMonths(3)), "Laptop", 0xFF3B82F6),
            Goal("g3", "Travel Fund", 200000.0, 20000.0, DateUtils.format(today.plusMonths(9)), "Flight", 0xFF8B5CF6)
        )
    }

    private fun loadGoals(): List<Goal> {
        val seed = seedGoals()
        val raw = prefs.getString(KEY_GOALS, null) ?: return seed
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Goal(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    title = o.optString("title", "Goal"),
                    targetAmount = o.optDouble("targetAmount", 0.0),
                    currentAmount = o.optDouble("currentAmount", 0.0),
                    targetDate = o.optString("targetDate", DateUtils.format(LocalDate.now())),
                    iconName = o.optString("iconName", "Savings"),
                    colorHex = o.optLong("colorHex", 0xFF10B981)
                )
            }
        }.getOrDefault(seed)
    }

    private val _goals = MutableStateFlow(loadGoals())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()

    // ---------- Accounts ----------
    private fun seedAccounts(): List<Account> = listOf(
        Account("a1", "Main Checking", "Bank", 425080.0, "**** 4821", 0xFF3B82F6),
        Account("a2", "High Yield Savings", "Savings", 1250000.0, "**** 9012", 0xFF10B981),
        Account("a3", "Primary Credit Card", "Card", -24550.0, "**** 3310", 0xFFEF4444),
        Account("a4", "Cash Wallet", "Cash", 18000.0, "Wallet", 0xFFF59E0B)
    )

    private fun loadAccounts(): List<Account> {
        val seed = seedAccounts()
        val raw = prefs.getString(KEY_ACCOUNTS_FULL, null)
        if (raw != null) {
            val loaded = runCatching {
                val arr = JSONArray(raw)
                List(arr.length()) { i ->
                    val o = arr.getJSONObject(i)
                    Account(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        name = o.optString("name", "Account"),
                        type = o.optString("type", "Bank"),
                        balance = o.optDouble("balance", 0.0),
                        accountNumber = o.optString("accountNumber", "**** 1234"),
                        colorHex = o.optLong("colorHex", 0xFF3B82F6)
                    )
                }
            }.getOrNull()
            if (!loaded.isNullOrEmpty()) return loaded
        }
        val oldSaved = prefs.getString(KEY_ACCOUNTS, null)?.let { runCatching { JSONObject(it) }.getOrNull() }
        if (oldSaved != null) {
            return seed.map { a -> if (oldSaved.has(a.name)) a.copy(balance = oldSaved.getDouble(a.name)) else a }
        }
        return seed
    }

    private val _accounts = MutableStateFlow(loadAccounts())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    // ---------- Categories ----------
    private fun seedCategories(): List<CategoryItem> = listOf(
        CategoryItem("c1", "Food & Dining", "Restaurant", true, 0xFFF97316),
        CategoryItem("c2", "Housing", "Home", true, 0xFFF59E0B),
        CategoryItem("c3", "Transportation", "DirectionsBus", true, 0xFF3B82F6),
        CategoryItem("c4", "Entertainment", "SportsEsports", true, 0xFFEF4444),
        CategoryItem("c5", "Shopping", "ShoppingBag", true, 0xFFEC4899),
        CategoryItem("c6", "Bills & Utilities", "Description", true, 0xFF8B5CF6),
        CategoryItem("c7", "Health & Fitness", "FitnessCenter", true, 0xFF10B981),
        CategoryItem("c8", "Salary", "Payments", false, 0xFF10B981),
        CategoryItem("c9", "Freelance", "Work", false, 0xFF06B6D4),
        CategoryItem("c10", "Investments", "TrendingUp", false, 0xFF8B5CF6)
    )

    private fun loadCategories(): List<CategoryItem> {
        val seed = seedCategories()
        val raw = prefs.getString(KEY_CATEGORIES, null) ?: return seed
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                CategoryItem(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    name = o.optString("name", "Category"),
                    iconName = o.optString("iconName", "Category"),
                    isExpense = o.optBoolean("isExpense", true),
                    colorHex = o.optLong("colorHex", 0xFF6366F1)
                )
            }
        }.getOrDefault(seed)
    }

    private val _categories = MutableStateFlow(loadCategories())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    // ---------- Bills ----------
    private fun seedBills(): List<BillItem> = listOf(
        BillItem("b1", "Electricity (ESCOM)", 15000.0, "Due 21 Oct 2025", "Bills & Utilities", false),
        BillItem("b2", "Water bill", 4500.0, "Due 25 Oct 2025", "Bills & Utilities", false),
        BillItem("b3", "Internet and airtime", 8000.0, "Due 28 Oct 2025", "Bills & Utilities", false),
        BillItem("b4", "House rent", 60000.0, "Due 01 Nov 2025", "Housing", false)
    )

    private fun loadBills(): List<BillItem> {
        val seed = seedBills()
        val raw = prefs.getString(KEY_BILLS, null) ?: return seed
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                BillItem(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    title = o.optString("title", "Bill"),
                    amount = o.optDouble("amount", 0.0),
                    dueDate = o.optString("dueDate", "Due soon"),
                    category = o.optString("category", "Bills & Utilities"),
                    isPaid = o.optBoolean("isPaid", false)
                )
            }
        }.getOrDefault(seed)
    }

    private val _bills = MutableStateFlow(loadBills())
    val bills: StateFlow<List<BillItem>> = _bills.asStateFlow()

    fun addBill(bill: BillItem) {
        _bills.value = listOf(bill) + _bills.value
        val notif = NotificationItem(
            id = UUID.randomUUID().toString(),
            title = "Bill Reminder: ${bill.title}",
            message = "Reminder: ${bill.title} (${_userProfile.value.currencySymbol}${bill.amount}) is ${bill.dueDate}.",
            timeAgo = "Just now",
            isRead = false,
            type = "Reminder",
            key = "bill_${bill.id}",
            target = "bills"
        )
        _notifications.value = listOf(notif) + _notifications.value
        persist()
    }

    fun toggleBillPaid(id: String) {
        _bills.value = _bills.value.map { if (it.id == id) it.copy(isPaid = !it.isPaid) else it }
        persist()
    }

    // ---------- Notifications ----------
    // Alerts the user cleared. They stay hidden until the cause goes away, then can fire again.
    private val dismissedKeys: MutableSet<String> = loadDismissed()

    private fun loadDismissed(): MutableSet<String> {
        val raw = prefs.getString(KEY_DISMISSED, null) ?: return mutableSetOf()
        return runCatching {
            val arr = JSONArray(raw)
            val set = mutableSetOf<String>()
            for (i in 0 until arr.length()) set.add(arr.getString(i))
            set
        }.getOrDefault(mutableSetOf())
    }

    private fun seedNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "n1",
            title = "Budget Alert: Food & Dining",
            message = "You have spent 85% of your monthly Food & Dining budget limit.",
            timeAgo = "2 hrs ago",
            isRead = false,
            type = "Alert",
            key = "seed_food"
        ),
        NotificationItem(
            id = "n2",
            title = "Upcoming Bill: Electricity",
            message = "Electric Utility bill (MWK 15,000) is due in 2 days.",
            timeAgo = "1 day ago",
            isRead = false,
            type = "Reminder",
            key = "seed_bill",
            target = "bills"
        ),
        NotificationItem(
            id = "n3",
            title = "Goal Milestone Reached!",
            message = "Congratulations! You reached 50% of your Emergency Fund savings target.",
            timeAgo = "3 days ago",
            isRead = true,
            type = "System",
            key = "seed_goal",
            target = "savings_goals"
        ),
        NotificationItem(
            id = "n4",
            title = "Monthly Financial Summary",
            message = "Your February spending report is now available for review.",
            timeAgo = "1 week ago",
            isRead = true,
            type = "System",
            key = "seed_summary",
            target = "analytics"
        )
    )

    private fun loadNotifications(): List<NotificationItem> {
        val seed = seedNotifications()
        val raw = prefs.getString(KEY_NOTIFICATIONS, null) ?: return seed
        return runCatching {
            val arr = JSONArray(raw)
            if (arr.length() == 0) return seed
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                NotificationItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    message = o.getString("message"),
                    timeAgo = o.optString("timeAgo", ""),
                    isRead = o.getBoolean("isRead"),
                    type = o.optString("type", "Alert"),
                    key = o.optString("key", ""),
                    target = o.optString("target", ""),
                    createdAt = o.optLong("createdAt", 0L)
                )
            }
        }.getOrDefault(seed)
    }

    private val _notifications = MutableStateFlow(loadNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // ---------- Profile ----------
    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfile {
        val base = UserProfile()
        val raw = prefs.getString(KEY_PROFILE, null) ?: return base
        return runCatching {
            val o = JSONObject(raw)
            base.copy(
                name = o.optString("name", base.name),
                email = o.optString("email", base.email),
                currencySymbol = o.optString("symbol", base.currencySymbol),
                currencyCode = o.optString("code", base.currencyCode),
                language = o.optString("language", base.language),
                isDarkMode = o.optBoolean("dark", base.isDarkMode),
                isBiometricsEnabled = o.optBoolean("biometrics", base.isBiometricsEnabled),
                isNotificationsEnabled = o.optBoolean("notifications", base.isNotificationsEnabled)
            )
        }.getOrDefault(base)
    }

    init {
        // Work out the alerts for the data we just loaded, and save
        persist()
    }

    // ---------- Alerts: worked out from the data ----------
    private fun desiredAlerts(): List<AlertSpec> {
        val out = mutableListOf<AlertSpec>()
        val symbol = _userProfile.value.currencySymbol.ifBlank { "MWK " }
        fun money(v: Double) = symbol + "%,.0f".format(v)

        val today = LocalDate.now()
        val thisMonth = YearMonth.from(today)
        val monthKey = thisMonth.toString()
        val monthExpenses = _transactions.value.filter {
            it.type == TransactionType.EXPENSE && DateUtils.isInCurrentMonth(it.date)
        }
        val monthSpentTotal = monthExpenses.sumOf { it.amount }

        // 1 + 2. Category budget near its limit / over its limit
        _budgets.value.forEach { b ->
            if (b.monthlyLimit <= 0.0) return@forEach
            val spent = monthExpenses.filter { it.category == b.category }.sumOf { it.amount }
            val percent = spent / b.monthlyLimit * 100.0
            when {
                spent > b.monthlyLimit -> out += AlertSpec(
                    key = "budget_over:${b.category}:$monthKey",
                    title = "Over budget: ${b.category}",
                    message = "You have spent ${money(spent)} of your ${money(b.monthlyLimit)} ${b.category} budget.",
                    type = "Alert",
                    target = "budgets"
                )
                percent >= b.alertThresholdPercent -> out += AlertSpec(
                    key = "budget_warn:${b.category}:$monthKey",
                    title = "Budget warning: ${b.category}",
                    message = "You have used ${percent.toInt()}% of your ${b.category} budget (${money(spent)} of ${money(b.monthlyLimit)}).",
                    type = "Alert",
                    target = "budgets"
                )
            }
        }

        // 3. Whole budget used up
        val total = _totalBudget.value
        if (total > 0.0 && monthSpentTotal > total) {
            out += AlertSpec(
                key = "total_over:$monthKey",
                title = "Total budget exceeded",
                message = "You have spent ${money(monthSpentTotal)} against a total budget of ${money(total)} this month.",
                type = "Alert",
                target = "budgets"
            )
        }

        // 4, 5, 6. Goals: milestone, reached, deadline
        _goals.value.forEach { g ->
            if (g.targetAmount <= 0.0) return@forEach
            if (g.currentAmount >= g.targetAmount) {
                out += AlertSpec(
                    key = "goal_done:${g.id}",
                    title = "Goal reached: ${g.title}",
                    message = "You have saved ${money(g.targetAmount)}. Well done!",
                    type = "System",
                    target = "savings_goals"
                )
            } else {
                val percent = (g.currentAmount / g.targetAmount * 100.0).toInt()
                val level = when {
                    percent >= 75 -> 75
                    percent >= 50 -> 50
                    else -> 0
                }
                if (level > 0) {
                    out += AlertSpec(
                        key = "goal_progress:${g.id}:$level",
                        title = "Goal progress: ${g.title}",
                        message = "You are $percent% of the way to ${money(g.targetAmount)}. Keep going!",
                        type = "System",
                        target = "savings_goals"
                    )
                }
                DateUtils.parse(g.targetDate)?.let { due ->
                    val days = ChronoUnit.DAYS.between(today, due)
                    when {
                        days < 0 -> out += AlertSpec(
                            key = "goal_overdue:${g.id}",
                            title = "Goal overdue: ${g.title}",
                            message = "The target date has passed and you still need ${money(g.targetAmount - g.currentAmount)}.",
                            type = "Reminder",
                            target = "savings_goals"
                        )
                        days <= GOAL_DEADLINE_DAYS -> out += AlertSpec(
                            key = "goal_due:${g.id}",
                            title = "Goal deadline close: ${g.title}",
                            message = if (days == 0L) "Your target date is today. You still need ${money(g.targetAmount - g.currentAmount)}."
                            else "$days ${if (days == 1L) "day" else "days"} left. You still need ${money(g.targetAmount - g.currentAmount)}.",
                            type = "Reminder",
                            target = "savings_goals"
                        )
                    }
                }
            }
        }

        // 7. Bills due soon or overdue (unpaid)
        _bills.value.filter { !it.isPaid }.forEach { b ->
            DateUtils.parse(b.dueDate)?.let { due ->
                val days = ChronoUnit.DAYS.between(today, due)
                when {
                    days < 0 -> out += AlertSpec(
                        key = "bill_overdue:${b.id}",
                        title = "Bill overdue: ${b.title}",
                        message = "Due date (${b.dueDate}) has passed. Amount: ${money(b.amount)}.",
                        type = "Reminder",
                        target = "bills"
                    )
                    days <= 5 -> out += AlertSpec(
                        key = "bill_due:${b.id}",
                        title = "Bill due soon: ${b.title}",
                        message = if (days == 0L) "Due today! Amount: ${money(b.amount)}."
                        else "Due in $days ${if (days == 1L) "day" else "days"}. Amount: ${money(b.amount)}.",
                        type = "Reminder",
                        target = "bills"
                    )
                }
            }
        }

        // 8. Low or overdrawn accounts (credit cards are skipped, they are normally negative)
        _accounts.value.filter { !it.type.equals("Card", ignoreCase = true) }.forEach { a ->
            when {
                a.balance < 0.0 -> out += AlertSpec(
                    key = "acct_neg:${a.name}",
                    title = "Account overdrawn: ${a.name}",
                    message = "The balance is ${money(a.balance)}.",
                    type = "Alert",
                    target = "accounts"
                )
                a.balance < LOW_BALANCE -> out += AlertSpec(
                    key = "acct_low:${a.name}",
                    title = "Low balance: ${a.name}",
                    message = "Only ${money(a.balance)} left in this account.",
                    type = "Alert",
                    target = "accounts"
                )
            }
        }

        // 9. Unusually large expense (compared with the total budget)
        if (total > 0.0) {
            monthExpenses.filter { it.amount >= total * LARGE_EXPENSE_SHARE }.forEach { t ->
                out += AlertSpec(
                    key = "big_tx:${t.id}",
                    title = "Large expense: ${t.title}",
                    message = "${money(t.amount)} is ${(t.amount / total * 100.0).toInt()}% of your monthly budget.",
                    type = "Alert",
                    target = "transaction_detail/${t.id}"
                )
            }
        }

        // 10. Monthly summary: shown in the first days of a new month
        if (today.dayOfMonth <= 5) {
            val previous = thisMonth.minusMonths(1)
            val previousSpent = _transactions.value
                .filter { it.type == TransactionType.EXPENSE }
                .filter { t -> DateUtils.parse(t.date)?.let { YearMonth.from(it) == previous } == true }
                .sumOf { it.amount }
            if (previousSpent > 0.0) {
                val monthName = previous.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                out += AlertSpec(
                    key = "summary:$monthKey",
                    title = "Monthly summary ready",
                    message = "You spent ${money(previousSpent)} in $monthName. Tap to review.",
                    type = "System",
                    target = "analytics"
                )
            }
        }
        // 11. No spending logged for 3+ days (only once the user has some history)
        val lastEntry = _transactions.value
            .mapNotNull { DateUtils.parse(it.date) }
            .filter { !it.isAfter(today) }
            .maxOrNull()
        if (lastEntry != null) {
            val quietDays = ChronoUnit.DAYS.between(lastEntry, today)
            if (quietDays >= 3) {
                out += AlertSpec(
                    key = "no_spend:$today",
                    title = "No spending logged lately",
                    message = "Nothing recorded for $quietDays days. Add today's expenses to keep your budget accurate.",
                    type = "Reminder",
                    target = "add_transaction/new"
                )
            }
        }
        return out
    }

    // Adds alerts that should exist, removes ones whose cause is gone, and refreshes the text.
    private fun syncNotifications() {
        if (!_userProfile.value.isNotificationsEnabled) return

        val desired = desiredAlerts()
        val byKey = desired.associateBy { it.key }
        dismissedKeys.retainAll(byKey.keys)

        val kept = _notifications.value
            .filter { it.key.isBlank() || it.key in byKey }
            .map { n ->
                byKey[n.key]?.let { s ->
                    n.copy(title = s.title, message = s.message, type = s.type, target = s.target)
                } ?: n
            }

        val existingKeys = kept.map { it.key }.toSet()
        val now = System.currentTimeMillis()
        val added = desired
            .filter { it.key !in existingKeys && it.key !in dismissedKeys }
            .map {
                NotificationItem(
                    id = UUID.randomUUID().toString(),
                    title = it.title,
                    message = it.message,
                    timeAgo = "Just now",
                    isRead = false,
                    type = it.type,
                    key = it.key,
                    target = it.target,
                    createdAt = now
                )
            }

        _notifications.value = added + kept
    }

    // ---------- Saving to the phone ----------
    private fun persist() {
        syncNotifications() // every change to the data re-checks the alerts
        runCatching {
            val tx = JSONArray()
            _transactions.value.forEach { t ->
                tx.put(JSONObject().apply {
                    put("id", t.id); put("title", t.title); put("amount", t.amount)
                    put("category", t.category); put("date", t.date); put("time", t.time)
                    put("type", t.type.name); put("accountName", t.accountName); put("note", t.note)
                })
            }
            val budgets = JSONArray()
            _budgets.value.forEach { b ->
                budgets.put(JSONObject().apply {
                    put("id", b.id); put("category", b.category); put("limit", b.monthlyLimit)
                })
            }
            val categories = JSONArray()
            _categories.value.forEach { c ->
                categories.put(JSONObject().apply {
                    put("id", c.id); put("name", c.name); put("iconName", c.iconName)
                    put("isExpense", c.isExpense); put("colorHex", c.colorHex)
                })
            }
            val goals = JSONArray()
            _goals.value.forEach { g ->
                goals.put(JSONObject().apply {
                    put("id", g.id); put("title", g.title)
                    put("targetAmount", g.targetAmount); put("currentAmount", g.currentAmount)
                    put("targetDate", g.targetDate); put("iconName", g.iconName)
                    put("colorHex", g.colorHex)
                })
            }
            val bills = JSONArray()
            _bills.value.forEach { b ->
                bills.put(JSONObject().apply {
                    put("id", b.id); put("title", b.title); put("amount", b.amount)
                    put("dueDate", b.dueDate); put("category", b.category); put("isPaid", b.isPaid)
                })
            }
            val notifications = JSONArray()
            _notifications.value.forEach { n ->
                notifications.put(JSONObject().apply {
                    put("id", n.id); put("title", n.title); put("message", n.message)
                    put("timeAgo", n.timeAgo); put("isRead", n.isRead); put("type", n.type)
                    put("key", n.key); put("target", n.target); put("createdAt", n.createdAt)
                })
            }
            val dismissed = JSONArray().also { arr -> dismissedKeys.forEach { arr.put(it) } }
            val balances = JSONObject().also { o -> _accounts.value.forEach { o.put(it.name, it.balance) } }
            val accountsFull = JSONArray()
            _accounts.value.forEach { a ->
                accountsFull.put(JSONObject().apply {
                    put("id", a.id); put("name", a.name); put("type", a.type)
                    put("balance", a.balance); put("accountNumber", a.accountNumber)
                    put("colorHex", a.colorHex)
                })
            }
            val p = _userProfile.value
            val profile = JSONObject().apply {
                put("name", p.name); put("email", p.email)
                put("symbol", p.currencySymbol); put("code", p.currencyCode)
                put("language", p.language)
                put("dark", p.isDarkMode); put("biometrics", p.isBiometricsEnabled)
                put("notifications", p.isNotificationsEnabled)
            }
            val users = JSONArray()
            registeredAccounts.values.forEach { u ->
                users.put(JSONObject().apply {
                    put("name", u.name); put("identifier", u.identifier); put("password", u.password)
                })
            }
            prefs.edit()
                .putString(KEY_TX, tx.toString())
                .putString(KEY_TOTAL, _totalBudget.value.toString())
                .putString(KEY_BUDGETS, budgets.toString())
                .putString(KEY_CATEGORIES, categories.toString())
                .putString(KEY_GOALS, goals.toString())
                .putString(KEY_BILLS, bills.toString())
                .putString(KEY_NOTIFICATIONS, notifications.toString())
                .putString(KEY_DISMISSED, dismissed.toString())
                .putString(KEY_ACCOUNTS, balances.toString())
                .putString(KEY_ACCOUNTS_FULL, accountsFull.toString())
                .putString(KEY_PROFILE, profile.toString())
                .putString(KEY_USERS, users.toString())
                .apply()
        }
    }

    // ---------- Login / Register ----------
    fun registerAccount(name: String, identifier: String, pass: String): Boolean {
        val cleanId = identifier.trim()
        val key = cleanId.lowercase()
        if (name.isBlank() || cleanId.isBlank() || pass.length < 6) return false

        registeredAccounts[key] = RegisteredAccount(name.trim(), cleanId, hash(pass))
        _userProfile.value = _userProfile.value.copy(name = name.trim(), email = cleanId)
        persist()
        return true
    }

    fun loginAccount(identifier: String, pass: String): Boolean {
        if (identifier.isBlank() || pass.isBlank()) return false
        val account = registeredAccounts[identifier.trim().lowercase()]
        if (account != null && account.password == hash(pass)) {
            _userProfile.value = _userProfile.value.copy(name = account.name, email = account.identifier)
            persist()
            return true
        }
        return false
    }

    // ---------- Transactions ----------
    // Expense takes money out of the account, income puts it in.
    // direction = +1 to apply a transaction, -1 to reverse it.
    private fun applyToAccount(t: Transaction, direction: Int) {
        val sign = if (t.type == TransactionType.INCOME) 1 else -1
        val delta = t.amount * sign * direction
        _accounts.value = _accounts.value.map {
            if (it.name.equals(t.accountName, ignoreCase = true)) it.copy(balance = it.balance + delta) else it
        }
    }

    fun addTransaction(transaction: Transaction): Boolean {
        if (transaction.amount <= 0.0 || transaction.title.isBlank()) return false
        _transactions.value = listOf(transaction) + _transactions.value // newest first
        applyToAccount(transaction, +1)
        persist()

        // Check category spending against budget limit for ALERT_LIMIT_001
        if (transaction.type == TransactionType.EXPENSE) {
            val catName = transaction.category
            val spent = monthSpent(catName)
            val budget = _budgets.value.find { it.category.equals(catName, ignoreCase = true) || catName.contains(it.category, ignoreCase = true) }
            if (budget != null && budget.monthlyLimit > 0.0) {
                val ratio = spent / budget.monthlyLimit
                if (ratio >= 0.8) {
                    val msg = if (ratio >= 1.0)
                        "Budget Alert: $catName spending (${"%,.0f".format(spent)}) has exceeded your limit of ${"%,.0f".format(budget.monthlyLimit)}!"
                    else
                        "Budget Warning: $catName spending (${"%,.0f".format(spent)}) is approaching your limit of ${"%,.0f".format(budget.monthlyLimit)} (${(ratio * 100).toInt()}%)."

                    val newNotif = NotificationItem(
                        id = UUID.randomUUID().toString(),
                        title = "Budget Alert: $catName",
                        message = msg,
                        timeAgo = "Just now",
                        isRead = false,
                        type = "Alert",
                        key = "budget_$catName"
                    )
                    _notifications.value = listOf(newNotif) + _notifications.value
                }
            }
        }

        return true
    }

    fun updateTransaction(transaction: Transaction): Boolean {
        if (transaction.amount <= 0.0 || transaction.title.isBlank()) return false
        val old = _transactions.value.find { it.id == transaction.id } ?: return false
        applyToAccount(old, -1)
        applyToAccount(transaction, +1)
        _transactions.value = _transactions.value.map { if (it.id == transaction.id) transaction else it }
        persist()
        return true
    }

    fun deleteTransaction(id: String) {
        val old = _transactions.value.find { it.id == id } ?: return
        applyToAccount(old, -1)
        _transactions.value = _transactions.value.filter { it.id != id }
        persist()
    }

    // ---------- Budget operations ----------
    private fun monthSpent(category: String): Double =
        _transactions.value
            .filter {
                it.type == TransactionType.EXPENSE &&
                        it.category == category &&
                        DateUtils.isInCurrentMonth(it.date)
            }
            .sumOf { it.amount }

    fun addBudget(budget: Budget) {
        _budgets.value = _budgets.value + budget
        persist()
    }

    // Saves the total budget and every category allocation in one go.
    // Returns false (and saves nothing) if the plan is invalid.
    fun saveBudgetPlan(total: Double, allocations: Map<String, Double>): Boolean {
        if (total <= 0.0) return false
        if (allocations.values.any { it < 0.0 }) return false
        if (allocations.values.sum() > total + 0.001) return false

        _totalBudget.value = total
        val existing = _budgets.value.associateBy { it.category }
        _budgets.value = allocations
            .filterValues { it > 0 }
            .map { (category, limit) ->
                existing[category]?.copy(monthlyLimit = limit)
                    ?: Budget(UUID.randomUUID().toString(), category, limit, 0.0)
            }
        persist()
        return true
    }

    // Moves allocation from one category to another. The total budget never changes.
    fun moveBudgetFunds(fromCategory: String, toCategory: String, amount: Double): Boolean {
        if (amount <= 0.0 || fromCategory == toCategory) return false
        val source = _budgets.value.find { it.category == fromCategory } ?: return false
        _budgets.value.find { it.category == toCategory } ?: return false

        val availableToMove = source.monthlyLimit - monthSpent(fromCategory)
        if (amount > availableToMove) return false

        _budgets.value = _budgets.value.map {
            when (it.category) {
                fromCategory -> it.copy(monthlyLimit = it.monthlyLimit - amount)
                toCategory -> it.copy(monthlyLimit = it.monthlyLimit + amount)
                else -> it
            }
        }
        persist()
        return true
    }

    // ---------- Goals ----------
    private fun goalIsValid(goal: Goal): Boolean =
        goal.title.isNotBlank() &&
                goal.targetAmount > 0.0 &&
                goal.currentAmount >= 0.0 &&
                goal.currentAmount <= goal.targetAmount

    // Returns false (and saves nothing) if the goal is invalid
    fun addGoal(goal: Goal): Boolean {
        if (!goalIsValid(goal)) return false
        _goals.value = _goals.value + goal.copy(title = goal.title.trim())
        persist()
        return true
    }

    fun updateGoal(goal: Goal): Boolean {
        if (!goalIsValid(goal)) return false
        if (_goals.value.none { it.id == goal.id }) return false
        _goals.value = _goals.value.map {
            if (it.id == goal.id) goal.copy(title = goal.title.trim()) else it
        }
        persist()
        return true
    }

    fun deleteGoal(id: String) {
        _goals.value = _goals.value.filter { it.id != id }
        persist()
    }

    // A deposit must be above 0 and cannot be more than what is left to reach the target
    fun addDepositToGoal(goalId: String, amount: Double): Boolean {
        val goal = _goals.value.find { it.id == goalId } ?: return false
        val remaining = goal.targetAmount - goal.currentAmount
        if (amount <= 0.0 || amount > remaining + 0.001) return false

        val newAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount)
        _goals.value = _goals.value.map {
            if (it.id == goalId) it.copy(currentAmount = newAmount) else it
        }
        persist()
        return true
    }

    // ---------- Accounts ----------
    fun addAccount(account: Account) {
        _accounts.value = _accounts.value + account
        persist()
    }

    // ---------- Categories ----------
    // Returns false if the name is blank or already exists
    fun addCategory(category: CategoryItem): Boolean {
        val name = category.name.trim()
        if (name.isBlank()) return false
        if (_categories.value.any { it.name.equals(name, ignoreCase = true) }) return false
        _categories.value = _categories.value + category.copy(name = name)
        persist()
        return true
    }

    // Edit a category. If the name changes, every transaction and budget using it follows.
    fun updateCategory(updated: CategoryItem): Boolean {
        val old = _categories.value.find { it.id == updated.id } ?: return false
        val newName = updated.name.trim()
        if (newName.isBlank()) return false
        if (_categories.value.any { it.id != updated.id && it.name.equals(newName, ignoreCase = true) }) return false

        // a category with transactions cannot switch between expense and income
        val inUse = _transactions.value.any { it.category == old.name }
        if (inUse && old.isExpense != updated.isExpense) return false

        _categories.value = _categories.value.map {
            if (it.id == updated.id) updated.copy(name = newName) else it
        }
        if (old.name != newName) {
            _transactions.value = _transactions.value.map {
                if (it.category == old.name) it.copy(category = newName) else it
            }
            _budgets.value = _budgets.value.map {
                if (it.category == old.name) it.copy(category = newName) else it
            }
        }
        persist()
        return true
    }

    // A category that still has transactions cannot be deleted (they would be orphaned).
    // Its budget allocation is removed together with it.
    fun deleteCategory(id: String): Boolean {
        val category = _categories.value.find { it.id == id } ?: return false
        if (_transactions.value.any { it.category == category.name }) return false
        _categories.value = _categories.value.filter { it.id != id }
        _budgets.value = _budgets.value.filter { it.category != category.name }
        persist()
        return true
    }

    // ---------- Notifications ----------
    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        persist()
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        persist()
    }

    // Remove one notification. It will not come back unless its cause goes away and returns.
    fun dismissNotification(id: String) {
        val item = _notifications.value.find { it.id == id } ?: return
        if (item.key.isNotBlank()) dismissedKeys.add(item.key)
        _notifications.value = _notifications.value.filter { it.id != id }
        persist()
    }

    fun clearAllNotifications() {
        _notifications.value.forEach { if (it.key.isNotBlank()) dismissedKeys.add(it.key) }
        _notifications.value = emptyList()
        persist()
    }

    // ---------- Profile ----------
    fun updateProfile(name: String, email: String, imageUri: String) {
        _userProfile.value = _userProfile.value.copy(name = name, email = email, profileImageUri = imageUri)
        persist()
    }

    fun updateCurrency(symbol: String, code: String) {
        _userProfile.value = _userProfile.value.copy(currencySymbol = symbol, currencyCode = code)
        persist()
    }

    fun toggleDarkMode(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(isDarkMode = enabled)
        persist()
    }

    fun toggleBiometrics(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(isBiometricsEnabled = enabled)
        persist()
    }

    // Turning this off stops new alerts. Turning it back on brings current ones in.
    fun toggleNotifications(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(isNotificationsEnabled = enabled)
        persist()
    }

    fun updatePassword(currentPass: String, newPass: String): Boolean {
        if (newPass.length < 6) return false
        val currentEmail = _userProfile.value.email.trim().lowercase()
        val account = registeredAccounts[currentEmail]
        if (account != null) {
            if (account.password != hash(currentPass) && account.password != hash("password123")) {
                return false
            }
            registeredAccounts[currentEmail] = account.copy(password = hash(newPass))
        } else {
            registeredAccounts[currentEmail] = RegisteredAccount(_userProfile.value.name, currentEmail, hash(newPass))
        }
        persist()
        return true
    }

    fun updateLanguage(language: String) {
        _userProfile.value = _userProfile.value.copy(language = language)
        persist()
    }
}