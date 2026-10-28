package com.example.moneypath.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MoneyPathViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val currency = userProfile.currencySymbol.ifBlank { "MWK " }

    var selectedTimeframe by remember { mutableStateOf("Month") }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)

    // Recalculate figures based on selected period (Week, Month, Year) per SUMMARY_PERIOD_002
    val multiplier = when (selectedTimeframe) {
        "Week" -> 0.25
        "Year" -> 12.0
        else -> 1.0
    }

    val spentVal = 42500.0 * multiplier
    val budgetVal = 100000.0 * multiplier
    val remainingVal = budgetVal - spentVal

    val foodVal = 15000.0 * multiplier
    val entertainVal = 10500.0 * multiplier
    val transportVal = 6000.0 * multiplier
    val billsVal = 3500.0 * multiplier

    fun fmt(v: Double): String = "${currency}${"%,.0f".format(v)}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Spending Summary",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Timeframe Segmented Pills (Week, Month, Year)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Week", "Month", "Year").forEach { timeframe ->
                        val isSelected = selectedTimeframe == timeframe
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = if (isSelected) primaryGreen else Color.White,
                            onClick = { selectedTimeframe = timeframe },
                            shadowElevation = 1.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = timeframe,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF374151)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Donut Chart & Budget Overview Card (Image 3)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Donut Chart
                        Box(
                            modifier = Modifier.size(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(120.dp)) {
                                val strokeWidth = 22f
                                drawArc(color = Color(0xFFF97316), startAngle = -90f, sweepAngle = 130f, useCenter = false, size = Size(size.width, size.height), style = Stroke(strokeWidth))
                                drawArc(color = Color(0xFFEF4444), startAngle = 40f, sweepAngle = 90f, useCenter = false, size = Size(size.width, size.height), style = Stroke(strokeWidth))
                                drawArc(color = Color(0xFF3B82F6), startAngle = 130f, sweepAngle = 70f, useCenter = false, size = Size(size.width, size.height), style = Stroke(strokeWidth))
                                drawArc(color = Color(0xFF8B5CF6), startAngle = 200f, sweepAngle = 70f, useCenter = false, size = Size(size.width, size.height), style = Stroke(strokeWidth))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Spent ($selectedTimeframe)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280), fontSize = 10.sp)
                                )
                                Text(
                                    text = fmt(spentVal),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 13.sp)
                                )
                            }
                        }

                        // Right details
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Total budget",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280), fontSize = 12.sp)
                                )
                                Text(
                                    text = fmt(budgetVal),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111827), fontSize = 16.sp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Remaining",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280), fontSize = 12.sp)
                                )
                                Text(
                                    text = fmt(remainingVal),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = primaryGreen, fontSize = 16.sp)
                                )
                            }
                        }
                    }
                }
            }

            // "By category" Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "By category ($selectedTimeframe)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                )
            }

            // Category breakdown items matching Image 3 with recalculated values
            item {
                SummaryCategoryCard(
                    title = "Food",
                    amount = fmt(foodVal),
                    progress = 0.5f,
                    icon = Icons.Default.Restaurant,
                    iconBg = Color(0xFFF97316),
                    barColor = Color(0xFFF97316)
                )
            }

            item {
                SummaryCategoryCard(
                    title = "Entertainment",
                    amount = fmt(entertainVal),
                    progress = 0.35f,
                    icon = Icons.Default.SportsEsports,
                    iconBg = Color(0xFFEF4444),
                    barColor = Color(0xFFEF4444)
                )
            }

            item {
                SummaryCategoryCard(
                    title = "Transport",
                    amount = fmt(transportVal),
                    progress = 0.2f,
                    icon = Icons.Default.DirectionsBus,
                    iconBg = Color(0xFF3B82F6),
                    barColor = Color(0xFF3B82F6)
                )
            }

            item {
                SummaryCategoryCard(
                    title = "Bills",
                    amount = fmt(billsVal),
                    progress = 0.12f,
                    icon = Icons.Default.Description,
                    iconBg = Color(0xFF8B5CF6),
                    barColor = Color(0xFF8B5CF6)
                )
            }
        }
    }
}

@Composable
fun SummaryCategoryCard(
    title: String,
    amount: String,
    progress: Float,
    icon: ImageVector,
    iconBg: Color,
    barColor: Color
) {
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    )
                }

                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                )
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
        }
    }
}
