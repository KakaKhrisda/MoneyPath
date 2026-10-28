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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.data.DateUtils
import com.example.moneypath.data.models.Goal
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

private fun goalDateToMillis(text: String): Long? =
    DateUtils.parse(text)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

private fun goalMillisToDate(millis: Long): String =
    DateUtils.format(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())

private fun goalNumberText(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    viewModel: MoneyPathViewModel,
    goalId: String? = null,
    onNavigateBack: () -> Unit
) {
    val existing = remember(goalId) {
        if (!goalId.isNullOrEmpty() && goalId != "new") {
            viewModel.goals.value.find { it.id == goalId }
        } else null
    }
    val isEditing = existing != null

    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }

    val originalDate = existing?.let { DateUtils.parse(it.targetDate)?.let(DateUtils::format) ?: it.targetDate }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var targetText by remember { mutableStateOf(existing?.let { goalNumberText(it.targetAmount) } ?: "") }
    var savedText by remember {
        mutableStateOf(existing?.let { if (it.currentAmount > 0) goalNumberText(it.currentAmount) else "" } ?: "")
    }
    var selectedDate by remember { mutableStateOf(originalDate ?: "") }
    var selectedIcon by remember {
        mutableStateOf(
            existing?.iconName?.let { if (it == "Computer") "Laptop" else it } ?: "Savings"
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var targetError by remember { mutableStateOf<String?>(null) }
    var savedError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Color(0xFFE5E7EB),
        unfocusedBorderColor = Color(0xFFE5E7EB)
    )

    // Calendar pop-up
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (selectedDate.isBlank()) null else goalDateToMillis(selectedDate)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        selectedDate = goalMillisToDate(it)
                        dateError = null
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

    if (showDeleteDialog && existing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete \"${existing.title}\"?") },
            text = { Text("This goal and its saved progress will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteGoal(existing.id)
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
                        text = if (isEditing) "Edit Goal" else "Create Goal",
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
            Text(
                text = "Set a target and watch your savings grow.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Goal name
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GoalFieldLabel("Goal name")
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= 40) title = it
                        nameError = null
                    },
                    placeholder = { Text("e.g. New phone", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )
            }

            // Target amount
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GoalFieldLabel("Target amount")
                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it
                        targetError = null
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
                    isError = targetError != null,
                    supportingText = { targetError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )
            }

            // Already saved
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GoalFieldLabel(if (isEditing) "Saved so far" else "Already saved (optional)")
                OutlinedTextField(
                    value = savedText,
                    onValueChange = {
                        savedText = it
                        savedError = null
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
                    isError = savedError != null,
                    supportingText = { savedError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )
            }

            // Target date
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GoalFieldLabel("Target date")
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(
                        1.dp,
                        if (dateError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB)
                    )
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
                                text = selectedDate.ifBlank { "Select date" },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedDate.isBlank()) Color(0xFF9CA3AF) else Color(0xFF111827)
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
                dateError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFEF4444)),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            // Icon selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GoalFieldLabel("Icon")
                goalIconOptions.chunked(4).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowOptions.forEach { (name, icon, colorHex) ->
                            GoalIconCard(
                                icon = icon,
                                iconBg = Color(colorHex),
                                selected = selectedIcon == name,
                                primaryGreen = primaryGreen,
                                onClick = { selectedIcon = name }
                            )
                        }
                    }
                }
            }

            formError?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save / Update
            Button(
                onClick = {
                    val targetNum = targetText.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                    val savedNum = if (savedText.isBlank()) 0.0
                    else savedText.replace(",", "").trim().toDoubleOrNull() ?: -1.0
                    val parsedDate = DateUtils.parse(selectedDate)
                    val dateChanged = !isEditing || selectedDate != originalDate

                    nameError = if (title.isBlank()) "Enter a goal name" else null
                    targetError = if (targetNum <= 0.0) "Enter a target amount greater than 0" else null
                    savedError = when {
                        savedNum < 0.0 -> "Enter a valid amount"
                        targetNum > 0.0 && savedNum > targetNum -> "Can't be more than the target amount"
                        else -> null
                    }
                    dateError = when {
                        parsedDate == null -> "Pick a target date"
                        dateChanged && parsedDate.isBefore(LocalDate.now()) -> "Target date can't be in the past"
                        else -> null
                    }
                    formError = null

                    if (nameError == null && targetError == null && savedError == null && dateError == null) {
                        val goal = Goal(
                            id = existing?.id ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            targetAmount = targetNum,
                            currentAmount = savedNum,
                            targetDate = selectedDate,
                            iconName = selectedIcon,
                            colorHex = goalColorFor(selectedIcon)
                        )
                        val saved = if (isEditing) viewModel.updateGoal(goal) else viewModel.addGoal(goal)
                        if (saved) onNavigateBack()
                        else formError = "Could not save the goal. Check the details and try again."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text(
                    text = if (isEditing) "Update Goal" else "Save Goal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }

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
                        text = "Delete Goal",
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

@Composable
private fun GoalFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF374151)
        )
    )
}

@Composable
fun GoalIconCard(
    icon: ImageVector,
    iconBg: Color,
    selected: Boolean,
    primaryGreen: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(70.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (selected) BorderStroke(2.dp, primaryGreen) else BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
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
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}