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

private val fallbackIncomeCategoryNames = listOf("Salary", "Freelance", "Investments")

private fun incomeDateToMillis(text: String): Long =
    (DateUtils.parse(text) ?: LocalDate.now())
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun incomeMillisToDate(millis: Long): String =
    DateUtils.format(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())

@Composable
private fun IncomeFieldLabel(text: String) {
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
fun IncomeScreen(
    viewModel: MoneyPathViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val allCategories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val available by viewModel.availableBalance.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)

    fun money(value: Double) = currency + "%,.0f".format(value)

    val incomeList = transactions.filter { it.type == TransactionType.INCOME }
        .sortedByDescending { DateUtils.parse(it.date) ?: LocalDate.MIN }
    val totalIncome = incomeList.sumOf { it.amount }
    val monthIncome = incomeList.filter { DateUtils.isInCurrentMonth(it.date) }.sumOf { it.amount }

    val categoryNames = allCategories.filter { !it.isExpense }.map { it.name }
        .ifEmpty { fallbackIncomeCategoryNames }
    val accountNames = accounts.map { it.name }.ifEmpty { listOf("Main Checking") }

    // Form state
    var showDialog by remember { mutableStateOf(false) }
    var source by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categoryNames.first()) }
    var account by remember { mutableStateOf(accountNames.first()) }
    var date by remember { mutableStateOf(DateUtils.format(LocalDate.now())) }
    var note by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }

    var sourceError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    var categoryMenuOpen by remember { mutableStateOf(false) }
    var accountMenuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Transaction?>(null) }

    var snackMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackMessage = null
        }
    }

    val categoryOptions = (categoryNames + category).distinct()
    val accountOptions = (accountNames + account).distinct()
    val (_, categoryColor) = budgetCategoryStyle(category)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Color(0xFFE5E7EB),
        unfocusedBorderColor = Color(0xFFE5E7EB)
    )

    fun resetForm() {
        source = ""
        amountText = ""
        note = ""
        category = categoryNames.first()
        account = accountNames.first()
        date = DateUtils.format(LocalDate.now())
        editingId = null
        sourceError = null
        amountError = null
        formError = null
    }

    fun startEdit(t: Transaction) {
        editingId = t.id
        source = t.title
        amountText = if (t.amount % 1.0 == 0.0) t.amount.toLong().toString() else t.amount.toString()
        category = t.category
        account = t.accountName
        date = DateUtils.parse(t.date)?.let { DateUtils.format(it) } ?: t.date
        note = t.note
        sourceError = null
        amountError = null
        formError = null
        showDialog = true
    }

    fun save() {
        val amount = amountText.replace(",", "").trim().toDoubleOrNull()
        sourceError = if (source.isBlank()) "Enter an income source" else null
        amountError = if (amount == null || amount <= 0.0) "Enter an amount greater than 0" else null
        formError = null
        if (sourceError != null || amountError != null || amount == null) return

        val old = incomeList.find { it.id == editingId }
        val tx = Transaction(
            id = old?.id ?: UUID.randomUUID().toString(),
            title = source.trim(),
            amount = amount,
            category = category,
            date = date,
            time = old?.time ?: SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
            type = TransactionType.INCOME,
            accountName = account,
            note = note.trim()
        )
        val saved = if (old != null) viewModel.updateTransaction(tx) else viewModel.addTransaction(tx)
        if (saved) {
            snackMessage = if (old != null) "Income updated" else "Income added: ${tx.title} +${money(amount)}"
            resetForm()
            showDialog = false
        } else {
            formError = "Could not save. Check details and try again."
        }
    }

    // Date Picker
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = incomeDateToMillis(date))
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { date = incomeMillisToDate(it) }
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

    // Delete confirmation
    pendingDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete income?") },
            text = { Text("This removes \"${t.title}\" (${money(t.amount)}) and updates your balances.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(t.id)
                    if (editingId == t.id) resetForm()
                    snackMessage = "Income deleted"
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

    // Add / Edit Income Dialog (triggered by top bar plus or bottom button)
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                resetForm()
            },
            title = {
                Text(if (editingId != null) "Edit Income" else "Add Income", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Source
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        IncomeFieldLabel("Income source")
                        OutlinedTextField(
                            value = source,
                            onValueChange = { if (it.length <= 40) source = it; sourceError = null },
                            placeholder = { Text("e.g. Salary") },
                            singleLine = true,
                            isError = sourceError != null,
                            supportingText = { sourceError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }

                    // Amount
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        IncomeFieldLabel("Amount")
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it; amountError = null },
                            placeholder = { Text("0") },
                            prefix = { Text(currency, fontWeight = FontWeight.Bold) },
                            singleLine = true,
                            isError = amountError != null,
                            supportingText = { amountError?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }

                    // Category
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        IncomeFieldLabel("Category")
                        Box {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoryMenuOpen = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(category, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = categoryMenuOpen,
                                onDismissRequest = { categoryMenuOpen = false }
                            ) {
                                categoryOptions.forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = { category = name; categoryMenuOpen = false }
                                    )
                                }
                            }
                        }
                    }

                    // Account
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        IncomeFieldLabel("Deposit to account")
                        Box {
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { accountMenuOpen = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(account, fontWeight = FontWeight.Medium)
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = accountMenuOpen,
                                onDismissRequest = { accountMenuOpen = false }
                            ) {
                                accountOptions.forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = { account = name; accountMenuOpen = false }
                                    )
                                }
                            }
                        }
                    }

                    // Date
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        IncomeFieldLabel("Date")
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(date, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    formError?.let {
                        Text(it, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { save() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Text(if (editingId != null) "Update" else "Save Income", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    resetForm()
                }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Income",
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
                    // Top Bar Right Plus Sign Button
                    IconButton(onClick = {
                        resetForm()
                        showDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Income",
                            tint = primaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundLight)
            )
        },
        bottomBar = {
            // Bottom Button for Adding Income
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            resetForm()
                            showDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Income",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
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
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. Overall Income Title Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B7A6E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Text(
                            text = "Total Income Recorded",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = money(totalIncome),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 32.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IncomeStat("This month", money(monthIncome))
                            IncomeStat("Records", incomeList.size.toString())
                            IncomeStat("Available balance", money(available))
                        }
                    }
                }
            }

            // 2. Income history header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Income history",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    )
                    Text(
                        text = "${incomeList.size} items",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }
            }

            if (incomeList.isEmpty()) {
                item {
                    Text(
                        text = "No income recorded yet. Tap 'Add Income' below to add one.",
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(incomeList, key = { it.id }) { t ->
                val (icon, color) = budgetCategoryStyle(t.category)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { startEdit(t) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (editingId == t.id) Color(0xFFE8F5E9) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = t.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                            )
                            Text(
                                text = "${t.category} · ${DateUtils.friendly(t.date)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF6B7280),
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+${money(t.amount)}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A)
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { pendingDelete = t },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = t.accountName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6B7280),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
    }
}
