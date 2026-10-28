
package com.example.moneypath.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.data.DateUtils
import com.example.moneypath.data.models.Budget
import com.example.moneypath.data.models.CategoryItem
import com.example.moneypath.data.models.Transaction
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen
import java.util.UUID

// Two lines shown under each category name:
// 1) the budget limit, 2) what was spent (or received) this month and the transaction count
private fun categorySubtitle(
    cat: CategoryItem,
    budgets: List<Budget>,
    transactions: List<Transaction>,
    currency: String
): String {
    val all = transactions.filter { it.category == cat.name }
    val monthTotal = all
        .filter { DateUtils.isInCurrentMonth(it.date) }
        .sumOf { it.amount }
    val countText = "${all.size} ${if (all.size == 1) "transaction" else "transactions"}"
    val moneyText = currency + "%,.0f".format(monthTotal)

    return if (cat.isExpense) {
        val budget = budgets.find { it.category == cat.name }
        val first = if (budget != null) "Limit $currency${"%,.0f".format(budget.monthlyLimit)}"
        else "No budget set"
        "$first\nSpent $moneyText this month · $countText"
    } else {
        "Income category\nReceived $moneyText this month · $countText"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: MoneyPathViewModel,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }

    var showEditor by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryItem?>(null) }
    var pendingDelete by remember { mutableStateOf<CategoryItem?>(null) }
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

    val expenseCategories = categories.filter { it.isExpense }
    val incomeCategories = categories.filter { !it.isExpense }

    fun openAdd() {
        editingCategory = null
        showEditor = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen,
                            fontSize = 28.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111827)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { openAdd() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Category",
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (categories.isEmpty()) {
                item {
                    Text(
                        text = "No categories yet. Tap Add Category to create one.",
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            if (expenseCategories.isNotEmpty()) {
                item { CategorySectionTitle("Expense categories") }
                items(expenseCategories, key = { it.id }) { cat ->
                    CategoryManagementCard(
                        title = cat.name,
                        limit = categorySubtitle(cat, budgets, transactions, currency),
                        icon = categoryIconFor(cat.iconName),
                        iconBg = Color(cat.colorHex),
                        onEdit = {
                            editingCategory = cat
                            showEditor = true
                        },
                        onDelete = { pendingDelete = cat }
                    )
                }
            }

            if (incomeCategories.isNotEmpty()) {
                item { CategorySectionTitle("Income categories") }
                items(incomeCategories, key = { it.id }) { cat ->
                    CategoryManagementCard(
                        title = cat.name,
                        limit = categorySubtitle(cat, budgets, transactions, currency),
                        icon = categoryIconFor(cat.iconName),
                        iconBg = Color(cat.colorHex),
                        onEdit = {
                            editingCategory = cat
                            showEditor = true
                        },
                        onDelete = { pendingDelete = cat }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { openAdd() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Category",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // Add / Edit sheet
        if (showEditor) {
            val existing = editingCategory
            CategoryEditorSheet(
                viewModel = viewModel,
                existing = existing,
                allCategories = categories,
                usageCount = existing?.let { e -> transactions.count { it.category == e.name } } ?: 0,
                primaryGreen = primaryGreen,
                onDismiss = { showEditor = false },
                onSaved = { message ->
                    showEditor = false
                    snackMessage = message
                }
            )
        }

        // Delete confirmation (or a block message if the category is in use)
        pendingDelete?.let { cat ->
            val usage = transactions.count { it.category == cat.name }
            val hasBudget = budgets.any { it.category == cat.name }

            if (usage > 0) {
                AlertDialog(
                    onDismissRequest = { pendingDelete = null },
                    title = { Text("Can't delete \"${cat.name}\"") },
                    text = {
                        Text(
                            "$usage ${if (usage == 1) "transaction uses" else "transactions use"} this category. " +
                                    "Delete or edit them first, then try again."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { pendingDelete = null }) { Text("OK") }
                    }
                )
            } else {
                AlertDialog(
                    onDismissRequest = { pendingDelete = null },
                    title = { Text("Delete \"${cat.name}\"?") },
                    text = {
                        Text(
                            if (hasBudget) "Its budget allocation will be removed too."
                            else "This category will be removed."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            snackMessage =
                                if (viewModel.deleteCategory(cat.id)) "\"${cat.name}\" deleted"
                                else "Could not delete category"
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
}

@Composable
private fun CategorySectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280)
        ),
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
fun CategoryManagementCard(
    title: String,
    limit: String,
    icon: ImageVector,
    iconBg: Color,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = limit,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp
                        ),
                        maxLines = 2
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit $title",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete $title",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditorSheet(
    viewModel: MoneyPathViewModel,
    existing: CategoryItem?,
    allCategories: List<CategoryItem>,
    usageCount: Int,
    primaryGreen: Color,
    onDismiss: () -> Unit,
    onSaved: (String) -> Unit
) {
    val palette = listOf(
        primaryGreen,
        Color(0xFF8B5CF6),
        Color(0xFFF97316),
        Color(0xFF3B82F6),
        Color(0xFFEF4444),
        Color(0xFF0D9488),
        Color(0xFFEC4899),
        Color(0xFFF59E0B)
    )

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var isExpense by remember { mutableStateOf(existing?.isExpense ?: true) }
    var iconName by remember { mutableStateOf(existing?.iconName ?: categoryIconOptions.first().first) }
    var colorHex by remember { mutableLongStateOf(existing?.colorHex ?: colorToHex(palette.first())) }
    var error by remember { mutableStateOf<String?>(null) }

    val typeLocked = existing != null && usageCount > 0
    val isEditing = existing != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFF4F7F5)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Category" else "Add Category",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen
                )
            )

            // Name
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Category name",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= 25) name = it
                        error = null
                    },
                    placeholder = { Text("e.g. Health", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFFE5E7EB),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }

            // Type
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true to "Expense", false to "Income").forEach { (value, label) ->
                        FilterChip(
                            selected = isExpense == value,
                            onClick = { isExpense = value },
                            enabled = !typeLocked,
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryGreen,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
                if (typeLocked) {
                    Text(
                        text = "Type can't change while transactions use this category.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }
            }

            // Icon grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Icon",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                )
                categoryIconOptions.chunked(4).forEach { rowIcons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowIcons.forEach { (optionName, optionIcon) ->
                            val isSelected = iconName == optionName
                            Card(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clickable { iconName = optionName },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = if (isSelected) BorderStroke(2.dp, primaryGreen)
                                else BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = optionIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) primaryGreen else Color(0xFF6B7280),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Color row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    palette.forEach { color ->
                        val isSelected = colorToHex(color) == colorHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { colorHex = colorToHex(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Button(
                onClick = {
                    val clean = name.trim()
                    when {
                        clean.isBlank() -> error = "Enter a category name"
                        allCategories.any { it.id != existing?.id && it.name.equals(clean, ignoreCase = true) } ->
                            error = "A category with this name already exists"
                        else -> {
                            val item = CategoryItem(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                name = clean,
                                iconName = iconName,
                                isExpense = isExpense,
                                colorHex = colorHex
                            )
                            val saved = if (existing == null) viewModel.addCategory(item)
                            else viewModel.updateCategory(item)

                            if (saved) onSaved(if (existing == null) "\"$clean\" added" else "\"$clean\" updated")
                            else error = "Could not save category"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text(
                    text = if (isEditing) "Update Category" else "Save Category",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}