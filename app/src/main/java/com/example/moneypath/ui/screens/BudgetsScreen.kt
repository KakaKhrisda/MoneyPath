package com.example.moneypath.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.example.moneypath.data.models.Budget
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val WarnAmber = Color(0xFFF59E0B)
private val OverRed = Color(0xFFEF4444)

// 100000.0 -> "100000" so the text boxes look clean
private fun plain(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: MoneyPathViewModel,
    onNavigateToAddBudget: () -> Unit,
    onNavigateToAddSpecificBudget: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }
    val totalBudget by viewModel.totalBudget.collectAsState()
    val budgets by viewModel.budgetsWithSpent.collectAsState()

    var totalBudgetText by remember(totalBudget) { mutableStateOf(plain(totalBudget)) }
    val categoryLimits = remember(budgets) {
        mutableStateMapOf<String, String>().apply {
            budgets.forEach { b -> this[b.category] = plain(b.monthlyLimit) }
        }
    }

    var editingCategoryTarget by remember { mutableStateOf<Budget?>(null) }
    var editCategoryLimitText by remember { mutableStateOf("") }
    var snackMessage by remember { mutableStateOf<String?>(null) }
    var budgetError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackMessage = null
        }
    }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)
    val monthLabel = YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    // Live comparison of what is allocated against the total
    val totalLive = totalBudgetText.toDoubleOrNull() ?: 0.0
    val allocatedLive = categoryLimits.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val mismatch = allocatedLive > totalLive

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen,
                            fontSize = 28.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToAddSpecificBudget) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Category Budget",
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
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Section Title: "Create Budget"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Budget",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Editable Total Budget Card
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
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Total Overall Budget Amount",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = totalBudgetText,
                            onValueChange = {
                                totalBudgetText = it
                                budgetError = null
                            },
                            prefix = {
                                Text(
                                    text = currency,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9FAFB),
                                unfocusedContainerColor = Color(0xFFF9FAFB),
                                focusedBorderColor = primaryGreen,
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live "Allocated / Total" line, red when the categories add up to more
                        Text(
                            text = "Allocated ${currency}%,.0f of ${currency}%,.0f".format(allocatedLive, totalLive),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (mismatch) OverRed else Color(0xFF6B7280),
                                fontWeight = if (mismatch) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                        if (mismatch) {
                            Text(
                                text = "Categories add up to more than the total budget.",
                                style = MaterialTheme.typography.bodySmall.copy(color = OverRed)
                            )
                        }
                    }
                }
            }

            // Section Header for Categories
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories (Tap any to edit)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                    )
                    TextButton(onClick = onNavigateToAddSpecificBudget) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = primaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add new", color = primaryGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Clickable & Editable Category Budget Cards
            items(budgets, key = { it.id }) { b ->
                val (icon, color) = budgetCategoryStyle(b.category)
                val currentLimit = categoryLimits[b.category]?.toDoubleOrNull() ?: b.monthlyLimit
                val percent = if (currentLimit > 0) ((b.spentAmount / currentLimit) * 100).toInt() else 0
                val progress = if (currentLimit > 0) (b.spentAmount / currentLimit).toFloat().coerceIn(0f, 1f) else 0f
                val isOver = b.spentAmount > currentLimit
                val isNearing = !isOver && currentLimit > 0 && percent >= b.alertThresholdPercent

                val barColor = when {
                    isOver -> OverRed
                    isNearing -> WarnAmber
                    else -> color
                }
                val statusText = when {
                    isOver -> "Over limit by " + currency + "%,.0f".format(b.spentAmount - currentLimit)
                    isNearing -> "Nearing limit (${b.alertThresholdPercent}% reached)"
                    else -> null
                }

                BudgetCategoryCardClickable(
                    title = b.category,
                    spentLimit = currency + "%,.0f / %,.0f".format(b.spentAmount, currentLimit),
                    percentage = "$percent%",
                    progress = progress,
                    icon = icon,
                    iconBg = color,
                    barColor = barColor,
                    statusText = statusText,
                    onClick = {
                        editingCategoryTarget = b
                        editCategoryLimitText = plain(currentLimit)
                    }
                )
            }

            // Error shown when the budget cannot be saved
            item {
                budgetError?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OverRed,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Two Buttons Down There
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Button 1: Save Budget
                    Button(
                        onClick = {
                            val totalVal = totalBudgetText.toDoubleOrNull()
                            val allocations = mutableMapOf<String, Double>()
                            categoryLimits.forEach { (cat, limitStr) ->
                                allocations[cat] = limitStr.toDoubleOrNull() ?: 0.0
                            }
                            val allocated = allocations.values.sum()

                            when {
                                totalVal == null || totalVal <= 0.0 ->
                                    budgetError = "Enter a valid total budget amount"
                                allocations.values.any { it < 0 } ->
                                    budgetError = "Category limits cannot be negative"
                                allocated > totalVal ->
                                    budgetError = "Allocations (" + currency + "%,.0f".format(allocated) +
                                            ") exceed your total budget (" + currency + "%,.0f".format(totalVal) +
                                            "). Reduce them and save again."
                                else -> {
                                    val success = viewModel.saveBudgetPlan(totalVal, allocations)
                                    if (success) {
                                        budgetError = null
                                        snackMessage = "Budget plan saved successfully!"
                                    } else {
                                        budgetError = "Failed to save budget plan."
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Budget",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                    }

                    // Button 2: Adjust Budget (moves funds between categories)
                    OutlinedButton(
                        onClick = onNavigateToAddBudget,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, primaryGreen),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryGreen,
                            containerColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, tint = primaryGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Adjust Budget",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryGreen,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }

        // Edit Category Limit Dialog
        if (editingCategoryTarget != null) {
            val target = editingCategoryTarget!!
            AlertDialog(
                onDismissRequest = { editingCategoryTarget = null },
                title = { Text("Edit Budget for ${target.category}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Enter new monthly limit ($currency):")
                        OutlinedTextField(
                            value = editCategoryLimitText,
                            onValueChange = { editCategoryLimitText = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newLimit = editCategoryLimitText.toDoubleOrNull()
                            if (newLimit != null && newLimit >= 0) {
                                categoryLimits[target.category] = plain(newLimit)
                                budgetError = null
                            }
                            editingCategoryTarget = null
                        }
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = primaryGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingCategoryTarget = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun budgetCategoryStyle(name: String): Pair<ImageVector, Color> = when (name) {
    "Food & Dining" -> Icons.Default.Restaurant to Color(0xFFF97316)
    "Transportation" -> Icons.Default.DirectionsBus to Color(0xFF3B82F6)
    "Bills & Utilities" -> Icons.Default.Description to Color(0xFF8B5CF6)
    "Entertainment" -> Icons.Default.SportsEsports to Color(0xFFEF4444)
    "Shopping" -> Icons.Default.ShoppingBag to Color(0xFFEC4899)
    else -> Icons.Default.Savings to Color(0xFF0F7A55)
}

@Composable
fun BudgetCategoryCardClickable(
    title: String,
    spentLimit: String,
    percentage: String,
    progress: Float,
    icon: ImageVector,
    iconBg: Color,
    barColor: Color,
    onClick: () -> Unit,
    statusText: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        )
                        Text(
                            text = spentLimit,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF6B7280),
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = percentage,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = barColor,
                trackColor = Color(0xFFF3F4F6)
            )

            // Warning label shown at the threshold and when over the limit
            if (statusText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = barColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = barColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}