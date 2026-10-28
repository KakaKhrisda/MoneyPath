package com.example.moneypath.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.data.DateUtils
import com.example.moneypath.data.models.TransactionType
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen
import java.time.LocalDate

private fun shortCategoryName(name: String): String = when (name) {
    "Food & Dining" -> "Food"
    "Transportation" -> "Transport"
    "Bills & Utilities" -> "Bills"
    "Entertainment" -> "Entertain."
    else -> name
}

private fun money(currency: String, value: Double): String = currency + "%,.0f".format(value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MoneyPathViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToAddGoal: () -> Unit,
    onNavigateToBills: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }

    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    val summary by viewModel.budgetSummary.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val available by viewModel.availableBalance.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val billItems by viewModel.bills.collectAsState()

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)

    val expenses = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sortedByDescending { DateUtils.parse(it.date) ?: LocalDate.MIN }
    val recentExpenses = expenses.take(4)

    val topCategories = expenses
        .filter { DateUtils.isInCurrentMonth(it.date) }
        .groupBy { it.category }
        .map { (category, list) -> category to list.sumOf { it.amount } }
        .sortedByDescending { it.second }
        .take(4)

    val availableBudgetBalance = money(currency, available)
    val totalBudgetAmountText = money(currency, totalBudget)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // New MoneyPath logo (M with growth arrow)
                        MoneyPathLogo(logoSize = 38.dp)

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "MoneyPath",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                fontSize = 22.sp
                            )
                        )
                    }
                },
                actions = {
                    Box(
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF374151)
                            )
                        }

                        if (unreadCount > 0) {
                            val countText = if (unreadCount > 99) "99+" else unreadCount.toString()
                            Box(
                                modifier = Modifier
                                    .offset(x = (-2).dp, y = (2).dp)
                                    .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = countText,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundLight)
            )
        },
        containerColor = backgroundLight
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingTopBottom
        ) {
            // 1. Greeting Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Good morning, ${userProfile.name}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            fontSize = 24.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Small steps today, bigger goals tomorrow.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    )
                }
            }

            // 2. Available balance card with left-aligned available balance and bottom-right total budget amount
            item {
                AbstractPatternedCard(
                    availableBalance = availableBudgetBalance,
                    totalBudgetAmount = totalBudgetAmountText,
                    growthText = "${money(currency, summary.spent)} spent this month"
                )
            }

            // 3. Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButtonItem(
                        icon = Icons.Default.Add,
                        label = "Add Expense",
                        iconBackground = primaryGreen,
                        iconTint = Color.White,
                        onClick = onNavigateToAddTransaction
                    )
                    QuickActionButtonItem(
                        icon = Icons.Default.Flag,
                        label = "Add Goal",
                        iconBackground = Color(0xFF3B82F6),
                        iconTint = Color.White,
                        onClick = onNavigateToAddGoal
                    )
                    QuickActionButtonItem(
                        icon = Icons.Default.TrackChanges,
                        label = "Set Budget",
                        iconBackground = Color(0xFF8B5CF6),
                        iconTint = Color.White,
                        onClick = onNavigateToBudgets
                    )
                    QuickActionButtonItem(
                        icon = Icons.Default.BarChart,
                        label = "View Summary",
                        iconBackground = Color(0xFFF59E0B),
                        iconTint = Color.White,
                        onClick = onNavigateToAnalytics
                    )
                }
            }

            // 4. Budget Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Budget Overview",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "This month",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF6B7280),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .background(Color(0xFFF4F7F5), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { summary.ringProgress },
                                    modifier = Modifier.size(90.dp),
                                    color = if (summary.spent > summary.total && summary.total > 0)
                                        Color(0xFFEF4444) else primaryGreen,
                                    strokeWidth = 10.dp,
                                    trackColor = Color(0xFFE5E7EB),
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${summary.percentSpent}%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111827)
                                        )
                                    )
                                    Text(
                                        text = "spent",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF6B7280)
                                        )
                                    )
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BudgetDetailRow(label = "Spent", amount = money(currency, summary.spent), color = Color(0xFF111827))
                                BudgetDetailRow(label = "Remaining", amount = money(currency, summary.remaining), color = primaryGreen)
                                BudgetDetailRow(label = "Total budget", amount = money(currency, summary.total), color = Color(0xFF374151))
                            }
                        }
                    }
                }
            }

            // 5. Top Spending Categories Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Top Spending Categories",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        )
                        TextButton(onClick = onNavigateToBudgets) {
                            Text(
                                text = "View all >",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = primaryGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (topCategories.isEmpty()) {
                        Text(
                            text = "No spending recorded this month.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            topCategories.forEach { (catName, amount) ->
                                val pct = if (summary.spent > 0) ((amount / summary.spent) * 100).toInt() else 0
                                val (icon, iconBg) = budgetCategoryStyle(catName)
                                CategoryCardItem(
                                    title = shortCategoryName(catName),
                                    amount = money(currency, amount),
                                    percentage = "$pct%",
                                    icon = icon,
                                    iconBg = iconBg,
                                    badgeBg = Color(0xFFFFF7ED),
                                    badgeText = Color(0xFFC2410C)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Recent Expenses Section (Clickable items + See all -> Transactions)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Recent Expenses",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                            }
                            TextButton(onClick = onNavigateToTransactions) {
                                Text(
                                    text = "View all",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = primaryGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        if (recentExpenses.isEmpty()) {
                            Text(
                                text = "No recent expenses. Tap '+' to add one.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                            )
                        }

                        recentExpenses.forEachIndexed { index, txn ->
                            val (icon, color) = budgetCategoryStyle(txn.category)
                            ExpenseItemRow(
                                title = txn.title,
                                categoryDate = "${shortCategoryName(txn.category)} • ${DateUtils.friendly(txn.date)}",
                                amount = "-${money(currency, txn.amount)}",
                                icon = icon,
                                iconBg = color,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToTransactionDetail(txn.id) }
                                    .padding(vertical = 4.dp)
                            )
                            if (index < recentExpenses.lastIndex) {
                                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // 7. Upcoming Bills Section (Clickable items + See all -> Bills)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Upcoming Bills",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                            }
                            TextButton(onClick = onNavigateToBills) {
                                Text(
                                    text = "View all",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = primaryGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        if (billItems.isEmpty()) {
                            Text(
                                text = "No upcoming bills.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                            )
                        } else {
                            // Soonest unpaid bills first, show up to 4
                            val upcomingBills = billItems
                                .sortedWith(
                                    compareBy(
                                        { it.isPaid },
                                        { DateUtils.parse(it.dueDate) ?: LocalDate.MAX }
                                    )
                                )
                                .take(4)

                            upcomingBills.forEachIndexed { index, bill ->
                                val st = billStatus(bill.dueDate, bill.isPaid)
                                UpcomingBillRow(
                                    title = bill.title,
                                    dueText = "Due " + (DateUtils.parse(bill.dueDate)
                                        ?.let { DateUtils.format(it) } ?: bill.dueDate),
                                    amount = money(currency, bill.amount),
                                    badgeLabel = st.label,
                                    badgeBg = st.bg,
                                    badgeColor = st.fg,
                                    icon = Icons.Default.Receipt,
                                    iconBg = primaryGreen,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToBills() }
                                        .padding(vertical = 4.dp)
                                )
                                if (index < upcomingBills.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }

            // 8. Savings Goals (Clickable items + See all -> Goals)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = primaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Savings Goals",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                            }
                            TextButton(onClick = onNavigateToGoals) {
                                Text(
                                    text = "View all",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = primaryGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        if (goals.isEmpty()) {
                            Text(
                                text = "No goals yet. Tap Add Goal to start saving.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                            )
                        }

                        goals
                            .sortedWith(
                                compareBy(
                                    { it.currentAmount >= it.targetAmount },
                                    { DateUtils.parse(it.targetDate) ?: LocalDate.MAX }
                                )
                            )
                            .take(3)
                            .forEach { goal ->
                                HomeGoalRow(
                                    title = goal.title,
                                    amountText = money(currency, goal.currentAmount) + " / " + "%,.0f".format(goal.targetAmount),
                                    percentText = "${(goal.progress * 100).toInt()}%",
                                    progress = goal.progress,
                                    barColor = Color(goal.colorHex),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToGoals() }
                                        .padding(vertical = 4.dp)
                                )
                            }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AbstractPatternedCard(
    availableBalance: String,
    totalBudgetAmount: String,
    growthText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B7A6E))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawCircle(
                    color = Color(0xFF5A8F76).copy(alpha = 0.85f),
                    radius = 90f,
                    center = Offset(w * 0.15f, h * 0.45f)
                )

                drawRect(
                    color = Color(0xFF82B3A6).copy(alpha = 0.75f),
                    topLeft = Offset(w * 0.08f, h * 0.05f),
                    size = Size(w * 0.25f, h * 0.35f)
                )

                drawOval(
                    color = Color(0xFF6B9685).copy(alpha = 0.8f),
                    topLeft = Offset(w * 0.02f, h * 0.65f),
                    size = Size(w * 0.4f, h * 0.35f)
                )

                drawRect(
                    color = Color(0xFF74908C).copy(alpha = 0.7f),
                    topLeft = Offset(w * 0.6f, h * 0.12f),
                    size = Size(w * 0.3f, h * 0.35f)
                )

                drawOval(
                    color = Color(0xFF539C93).copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.85f, h * 0.15f),
                    size = Size(w * 0.2f, h * 0.6f)
                )

                val trianglePath = Path().apply {
                    moveTo(w * 0.8f, h * 0.55f)
                    lineTo(w, h * 0.95f)
                    lineTo(w * 0.65f, h)
                    close()
                }
                drawPath(
                    path = trianglePath,
                    color = Color(0xFFB4F2DA).copy(alpha = 0.75f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available balance",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Toggle Balance",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Middle Available Balance Display positioned on the left side
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = availableBalance,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 32.sp
                        ),
                        textAlign = TextAlign.Start
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = growthText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Budget",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = totalBudgetAmount,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

private val PaddingTopBottom = PaddingValues(top = 12.dp, bottom = 24.dp)

@Composable
fun QuickActionButtonItem(
    icon: ImageVector,
    label: String,
    iconBackground: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151),
                fontSize = 11.sp
            ),
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun BudgetDetailRow(label: String, amount: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF6B7280),
                fontSize = 12.sp
            )
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 15.sp
            )
        )
    }
}

@Composable
fun CategoryCardItem(
    title: String,
    amount: String,
    percentage: String,
    icon: ImageVector,
    iconBg: Color,
    badgeBg: Color,
    badgeText: Color
) {
    Card(
        modifier = Modifier.width(82.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF6B7280),
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = percentage,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = badgeText,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun ExpenseItemRow(
    title: String,
    categoryDate: String,
    amount: String,
    icon: ImageVector,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                )
                Text(
                    text = categoryDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp
                    )
                )
            }
        }

        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        )
    }
}

@Composable
fun UpcomingBillRow(
    title: String,
    dueText: String,
    amount: String,
    badgeLabel: String,
    badgeBg: Color,
    badgeColor: Color,
    icon: ImageVector,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                )
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF6B7280),
                        fontSize = 12.sp
                    )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun HomeGoalRow(
    title: String,
    amountText: String,
    percentText: String,
    progress: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            )
            Text(
                text = percentText,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            )
        }
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF6B7280)
            )
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = Color(0xFFF3F4F6)
        )
    }
}
