package com.example.moneypath.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.moneypath.data.models.BillItem
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

// Label and colours for the chip shown beside a bill. Also used on Home.
data class BillStatus(val label: String, val bg: Color, val fg: Color)

fun billStatus(dueDate: String, isPaid: Boolean): BillStatus {
    if (isPaid) return BillStatus("Paid", Color(0xFFF0FDF4), Color(0xFF15803D))

    // Older bills saved as plain text (e.g. "Due in 3 days") cannot be counted down
    val due = DateUtils.parse(dueDate)
        ?: return BillStatus(
            dueDate.ifBlank { "No date" },
            Color(0xFFEFF6FF), Color(0xFF1D4ED8)
        )

    val days = ChronoUnit.DAYS.between(LocalDate.now(), due)
    return when {
        days < 0 -> BillStatus("Overdue by ${-days} d", Color(0xFFFEF2F2), Color(0xFFB91C1C))
        days == 0L -> BillStatus("Due today", Color(0xFFFEF2F2), Color(0xFFB91C1C))
        days == 1L -> BillStatus("Due tomorrow", Color(0xFFFFF7ED), Color(0xFFC2410C))
        days <= 7 -> BillStatus("Due in $days days", Color(0xFFFFF7ED), Color(0xFFC2410C))
        else -> BillStatus("Due in $days days", Color(0xFFEFF6FF), Color(0xFF1D4ED8))
    }
}

private fun billPickerMillis(text: String): Long =
    (DateUtils.parse(text) ?: LocalDate.now())
        .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun billPickerText(millis: Long): String =
    DateUtils.format(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: MoneyPathViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }
    val bills by viewModel.bills.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newDueDate by remember { mutableStateOf("") }
    val newCategory = "Bills & Utilities"
    var addError by remember { mutableStateOf<String?>(null) }
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

    // Unpaid bills first, soonest due date at the top
    val sortedBills = bills.sortedWith(
        compareBy({ it.isPaid }, { DateUtils.parse(it.dueDate) ?: LocalDate.MAX })
    )

    fun resetForm() {
        newTitle = ""
        newAmount = ""
        newDueDate = ""
        addError = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manage Bills",
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
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Bill",
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
            item {
                Text(
                    text = "Scheduled Bill Reminders & Alerts",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                )
            }

            if (sortedBills.isEmpty()) {
                item {
                    Text(
                        text = "No upcoming bills. Tap '+' to add a bill reminder.",
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            items(sortedBills, key = { it.id }) { bill ->
                val status = billStatus(bill.dueDate, bill.isPaid)
                val dueText = DateUtils.parse(bill.dueDate)?.let { DateUtils.format(it) } ?: bill.dueDate

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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(primaryGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = bill.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                )
                                Text(
                                    text = "Due $dueText",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF6B7280),
                                        fontSize = 12.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(status.bg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = status.fg,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currency + "%,.0f".format(bill.amount),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                            )
                            TextButton(
                                onClick = {
                                    viewModel.toggleBillPaid(bill.id)
                                    snackMessage = if (bill.isPaid)
                                        "${bill.title} marked as unpaid"
                                    else "${bill.title} marked as paid"
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text(
                                    text = if (bill.isPaid) "Undo" else "Mark Paid",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (bill.isPaid) Color(0xFF6B7280) else primaryGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Bill Reminder",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // Add Bill Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    resetForm()
                },
                title = { Text("Create Bill Reminder") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = {
                                newTitle = it
                                addError = null
                            },
                            label = { Text("Bill name (e.g. Electricity)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = newAmount,
                            onValueChange = {
                                newAmount = it
                                addError = null
                            },
                            label = { Text("Amount ($currency)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (newDueDate.isBlank()) "Select due date" else "Due $newDueDate")
                        }
                        addError?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val amt = newAmount.replace(",", "").trim().toDoubleOrNull()
                            val due = DateUtils.parse(newDueDate)
                            when {
                                newTitle.isBlank() -> addError = "Enter a bill name"
                                amt == null || amt <= 0.0 -> addError = "Enter a valid amount"
                                due == null -> addError = "Select a due date"
                                due.isBefore(LocalDate.now()) ->
                                    addError = "Due date must be today or later"
                                else -> {
                                    viewModel.addBill(
                                        BillItem(
                                            id = UUID.randomUUID().toString(),
                                            title = newTitle.trim(),
                                            amount = amt,
                                            dueDate = DateUtils.format(due),
                                            category = newCategory,
                                            isPaid = false
                                        )
                                    )
                                    snackMessage = "Bill reminder saved"
                                    showAddDialog = false
                                    resetForm()
                                }
                            }
                        }
                    ) {
                        Text("Create", fontWeight = FontWeight.Bold, color = primaryGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        resetForm()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Calendar: only today or later can be picked
        if (showDatePicker) {
            val todayMillis = LocalDate.now()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis =
                    if (newDueDate.isBlank()) todayMillis else billPickerMillis(newDueDate),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        utcTimeMillis >= todayMillis
                }
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        pickerState.selectedDateMillis?.let {
                            newDueDate = billPickerText(it)
                            addError = null
                        }
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
    }
}