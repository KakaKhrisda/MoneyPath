package com.example.moneypath.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.data.DateUtils
import com.example.moneypath.data.models.Goal
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Icon choices for a goal: name, icon, colour. Used by this page, Add Goal and Home.
val goalIconOptions: List<Triple<String, ImageVector, Long>> = listOf(
    Triple("Savings", Icons.Default.Savings, 0xFF2EAF7B),
    Triple("Laptop", Icons.Default.Laptop, 0xFF3B82F6),
    Triple("Flight", Icons.Default.Flight, 0xFF8B5CF6),
    Triple("Security", Icons.Default.Security, 0xFF0D9488),
    Triple("PhoneAndroid", Icons.Default.PhoneAndroid, 0xFFF97316),
    Triple("Home", Icons.Default.Home, 0xFFF59E0B),
    Triple("School", Icons.Default.School, 0xFFEC4899),
    Triple("DirectionsCar", Icons.Default.DirectionsCar, 0xFFEF4444)
)

fun goalIconFor(name: String): ImageVector =
    goalIconOptions.firstOrNull { it.first == name }?.second
        ?: if (name == "Computer") Icons.Default.Laptop else Icons.Default.Savings

fun goalColorFor(name: String): Long =
    goalIconOptions.firstOrNull { it.first == name }?.third ?: 0xFF2EAF7B

private fun Goal.isDone(): Boolean = targetAmount > 0 && currentAmount >= targetAmount

private fun goalStatus(goal: Goal): Pair<String, Color> {
    if (goal.isDone()) return "Goal reached" to Color(0xFF16A34A)
    val date = DateUtils.parse(goal.targetDate) ?: return "No target date" to Color(0xFF6B7280)
    val days = ChronoUnit.DAYS.between(LocalDate.now(), date)
    val dateText = DateUtils.format(date)
    return when {
        days < 0 -> "Overdue · $dateText" to Color(0xFFEF4444)
        days == 0L -> "Due today" to Color(0xFFF59E0B)
        days == 1L -> "1 day left · $dateText" to Color(0xFF6B7280)
        else -> "$days days left · $dateText" to Color(0xFF6B7280)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    viewModel: MoneyPathViewModel,
    onNavigateToAddGoal: () -> Unit,
    onNavigateToEditGoal: (String) -> Unit = {}
) {
    val goals by viewModel.goals.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }

    var depositGoalId by remember { mutableStateOf<String?>(null) }
    var depositText by remember { mutableStateOf("") }
    var depositError by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<Goal?>(null) }
    var snackMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackMessage = null
        }
    }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)

    // Active goals first (nearest date first), finished goals last
    val sortedGoals = goals.sortedWith(
        compareBy({ it.isDone() }, { DateUtils.parse(it.targetDate) ?: LocalDate.MAX })
    )
    val totalSaved = goals.sumOf { it.currentAmount }
    val totalTarget = goals.sumOf { it.targetAmount }
    val overallProgress =
        if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f
    val completedCount = goals.count { it.isDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Goals",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen,
                            fontSize = 28.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToAddGoal) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Goal",
                            tint = primaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundLight)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundLight
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Overall summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(primaryGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Total saved",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF111827)
                                        )
                                    )
                                    Text(
                                        text = "$currency${"%,.0f".format(totalSaved)} of $currency${"%,.0f".format(totalTarget)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF6B7280),
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "${(overallProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = primaryGreen,
                            trackColor = Color(0xFFD1E7DD)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${goals.size} ${if (goals.size == 1) "goal" else "goals"} · $completedCount completed",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                        )
                    }
                }
            }

            // Header
            item {
                Text(
                    text = "My Goals",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                )
            }

            if (goals.isEmpty()) {
                item {
                    Text(
                        text = "No goals yet. Tap Create Goal to start saving.",
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            items(sortedGoals, key = { it.id }) { goal ->
                GoalCard(
                    goal = goal,
                    currency = currency,
                    primaryGreen = primaryGreen,
                    onDeposit = {
                        depositText = ""
                        depositError = null
                        depositGoalId = goal.id
                    },
                    onEdit = { onNavigateToEditGoal(goal.id) },
                    onDelete = { pendingDelete = goal }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToAddGoal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Goal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // Deposit dialog
        val depositGoal = goals.find { it.id == depositGoalId }
        if (depositGoal != null) {
            val remaining = depositGoal.targetAmount - depositGoal.currentAmount
            AlertDialog(
                onDismissRequest = { depositGoalId = null },
                title = { Text("Add Savings Deposit") },
                text = {
                    Column {
                        Text("Goal: ${depositGoal.title}")
                        Text(
                            text = "Left to reach target: $currency${"%,.0f".format(remaining)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = depositText,
                            onValueChange = {
                                depositText = it
                                depositError = null
                            },
                            label = { Text("Deposit amount ($currency)") },
                            singleLine = true,
                            isError = depositError != null,
                            supportingText = { depositError?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val amount = depositText.replace(",", "").trim().toDoubleOrNull()
                            when {
                                amount == null || amount <= 0.0 ->
                                    depositError = "Enter an amount greater than 0"
                                amount > remaining + 0.001 ->
                                    depositError = "Only $currency${"%,.0f".format(remaining)} left to reach this goal"
                                else -> {
                                    if (viewModel.addDepositToGoal(depositGoal.id, amount)) {
                                        snackMessage = "Deposit added to ${depositGoal.title}"
                                        depositGoalId = null
                                    } else {
                                        depositError = "Could not add deposit"
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Add Deposit", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { depositGoalId = null }) { Text("Cancel") }
                }
            )
        }

        // Delete confirmation
        pendingDelete?.let { goal ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Delete \"${goal.title}\"?") },
                text = { Text("This goal and its saved progress will be removed.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteGoal(goal.id)
                        snackMessage = "\"${goal.title}\" deleted"
                        pendingDelete = null
                    }) {
                        Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    currency: String,
    primaryGreen: Color,
    onDeposit: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = Color(goal.colorHex)
    val done = goal.isDone()
    val percent = (goal.progress * 100).toInt()
    val (statusText, statusColor) = goalStatus(goal)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !done) { onDeposit() },
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
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = goalIconFor(goal.iconName),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "$currency${"%,.0f".format(goal.currentAmount)} / ${"%,.0f".format(goal.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF6B7280),
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (done) Color(0xFF16A34A) else Color(0xFF111827)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (done) Color(0xFF16A34A) else color,
                trackColor = Color(0xFFF3F4F6)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!done) {
                        TextButton(onClick = onDeposit) {
                            Text("Deposit", color = primaryGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit ${goal.title}",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete ${goal.title}",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}