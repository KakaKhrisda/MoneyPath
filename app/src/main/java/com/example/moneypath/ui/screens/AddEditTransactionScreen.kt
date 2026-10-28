package com.example.moneypath.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.data.DateUtils
import com.example.moneypath.data.models.Transaction
import com.example.moneypath.data.models.TransactionType
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import java.util.UUID

private val fallbackExpenseCategories = listOf(
    "Food & Dining", "Transportation", "Bills & Utilities",
    "Entertainment", "Shopping", "Health & Fitness", "Housing"
)
private val fallbackIncomeCategories = listOf("Salary", "Freelance", "Investments")

// The date picker works in UTC midnight
private fun dateToPickerMillis(text: String): Long =
    (DateUtils.parse(text) ?: LocalDate.now())
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun pickerMillisToText(millis: Long): String =
    DateUtils.format(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF374151)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: MoneyPathViewModel,
    transactionId: String? = null,
    onNavigateBack: () -> Unit
) {
    val existingTransaction = remember(transactionId) {
        if (!transactionId.isNullOrEmpty() && transactionId != "new" && transactionId != "new_income") {
            viewModel.transactions.value.find { it.id == transactionId }
        } else null
    }
    val isEditing = existingTransaction != null

    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }
    val allCategories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    // Live budget numbers, used to reject expenses bigger than what is left
    val budgetSummary by viewModel.budgetSummary.collectAsState()

    // Expense or Income (can only be chosen when adding)
    var txType by remember {
        mutableStateOf(
            existingTransaction?.type ?: if (transactionId == "new_income") TransactionType.INCOME else TransactionType.EXPENSE
        )
    }
    val isIncome = txType == TransactionType.INCOME
    val noun = if (isIncome) "Income" else "Expense"

    val categoryNames = allCategories
        .filter { it.isExpense == !isIncome }
        .map { it.name }
        .ifEmpty { if (isIncome) fallbackIncomeCategories else fallbackExpenseCategories }

    val accountNames = (accounts.map { it.name } + listOfNotNull(existingTransaction?.accountName))
        .distinct()
        .ifEmpty { listOf("Main Checking") }

    var title by remember { mutableStateOf(existingTransaction?.title ?: "") }
    var amountText by remember {
        mutableStateOf(
            existingTransaction?.amount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }
    var selectedCategory by remember {
        mutableStateOf(existingTransaction?.category ?: if (isIncome) "Salary" else "Food & Dining")
    }
    var selectedAccount by remember {
        mutableStateOf(existingTransaction?.accountName ?: accounts.firstOrNull()?.name ?: "Main Checking")
    }
    var selectedDate by remember {
        mutableStateOf(
            existingTransaction?.date?.let { d -> DateUtils.parse(d)?.let { DateUtils.format(it) } ?: d }
                ?: DateUtils.format(LocalDate.now())
        )
    }
    var note by remember { mutableStateOf(existingTransaction?.note ?: "") }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var accountMenuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)
    val (categoryIcon, categoryColor) = budgetCategoryStyle(selectedCategory)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Color(0xFFE5E7EB),
        unfocusedBorderColor = Color(0xFFE5E7EB)
    )

    // Calendar pop-up
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateToPickerMillis(selectedDate)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedDate = pickerMillisToText(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDeleteDialog && existingTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${noun.lowercase()}?") },
            text = { Text("This will remove \"${existingTransaction.title}\" and update your totals.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTransaction(existingTransaction.id)
                    onNavigateBack()
                }) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit $noun" else "Add $noun",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen,
                            fontSize = 26.sp
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundLight)
            )
        },
        containerColor = backgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expense / Income switch (only when adding)
            if (!isEditing) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(TransactionType.EXPENSE to "Expense", TransactionType.INCOME to "Income")
                        .forEach { (type, label) ->
                            FilterChip(
                                selected = txType == type,
                                onClick = {
                                    if (txType != type) {
                                        txType = type
                                        selectedCategory =
                                            if (type == TransactionType.INCOME) "Salary" else "Food & Dining"
                                        errorMessage = null
                                    }
                                },
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
            }

            // Amount
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Amount")
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    placeholder = { Text("0") },
                    prefix = {
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )
            }

            // Name / source
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel(if (isIncome) "Income source" else "Expense name")
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    placeholder = { Text(if (isIncome) "e.g. Allowance" else "e.g. Lunch") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )
            }

            // Category
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Category")
                Box {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryMenuOpen = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(categoryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = categoryIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = selectedCategory,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF111827)
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = categoryMenuOpen,
                        onDismissRequest = { categoryMenuOpen = false }
                    ) {
                        categoryNames.forEach { name ->
                            val (icon, color) = budgetCategoryStyle(name)
                            DropdownMenuItem(
                                text = { Text(name) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCategory = name
                                    categoryMenuOpen = false
                                    errorMessage = null
                                }
                            )
                        }
                    }
                }
            }

            // Account
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel(if (isIncome) "Deposit to account" else "Pay from account")
                Box {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accountMenuOpen = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = selectedAccount,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF111827)
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = accountMenuOpen,
                        onDismissRequest = { accountMenuOpen = false }
                    ) {
                        accountNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedAccount = name
                                    accountMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            // Date
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Date")
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = selectedDate,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF111827)
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Note
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("Note (optional)")
                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 100) note = it },
                    placeholder = { Text("Add a note...") },
                    supportingText = {
                        Text(
                            text = "${note.length}/100",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF111827)),
                    colors = fieldColors
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save / Update
            Button(
                onClick = {
                    val cleanAmount = amountText.replace(",", "").trim().toDoubleOrNull()

                    // When editing an expense, its old amount is given back before checking
                    val alreadyCounted =
                        if (existingTransaction != null &&
                            existingTransaction.type == TransactionType.EXPENSE
                        ) existingTransaction.amount else 0.0
                    val allowedToSpend = budgetSummary.remaining + alreadyCounted

                    when {
                        cleanAmount == null || cleanAmount <= 0.0 ->
                            errorMessage = "Enter a valid amount greater than 0"
                        title.isBlank() ->
                            errorMessage = if (isIncome) "Enter an income source" else "Enter an expense name"
                        !isIncome && budgetSummary.total > 0 && cleanAmount > allowedToSpend ->
                            errorMessage = "Expense exceeds your remaining budget (" +
                                    currency + "%,.0f".format(allowedToSpend.coerceAtLeast(0.0)) + ")"
                        else -> {
                            val currentTime =
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                            val newTx = Transaction(
                                id = existingTransaction?.id ?: UUID.randomUUID().toString(),
                                title = title.trim(),
                                amount = cleanAmount,
                                category = selectedCategory,
                                date = selectedDate,
                                time = existingTransaction?.time ?: currentTime,
                                type = txType,
                                accountName = selectedAccount,
                                note = note.trim()
                            )

                            val saved = if (isEditing) viewModel.updateTransaction(newTx)
                            else viewModel.addTransaction(newTx)

                            if (saved) onNavigateBack()
                            else errorMessage = "Could not save. Check the details and try again."
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
                    text = if (isEditing) "Update $noun" else "Save $noun",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }

            // Delete (only when editing, asks to confirm)
            if (isEditing) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete $noun",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}