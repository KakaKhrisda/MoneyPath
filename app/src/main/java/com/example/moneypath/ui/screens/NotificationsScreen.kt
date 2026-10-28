package com.example.moneypath.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.data.DateUtils
import com.example.moneypath.data.models.NotificationItem
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen
import java.time.Instant
import java.time.ZoneId

// Icon and colour by what the alert is about and how serious it is
private fun notificationStyle(item: NotificationItem): Pair<ImageVector, Color> {
    val red = Color(0xFFEF4444)
    val amber = Color(0xFFF59E0B)
    val green = Color(0xFF16A34A)
    val blue = Color(0xFF3B82F6)
    val k = item.key
    return when {
        k.startsWith("budget_over") || k.startsWith("total_over") -> Icons.Default.Warning to red
        k.startsWith("budget_warn") -> Icons.Default.Warning to amber
        k.startsWith("acct_neg") -> Icons.Default.AccountBalance to red
        k.startsWith("acct_low") -> Icons.Default.AccountBalance to amber
        k.startsWith("goal_overdue") -> Icons.Default.Flag to red
        k.startsWith("goal_due") -> Icons.Default.Flag to amber
        k.startsWith("goal_done") -> Icons.Default.EmojiEvents to green
        k.startsWith("goal_progress") -> Icons.Default.Flag to green
        k.startsWith("big_tx") -> Icons.Default.Receipt to amber
        k.startsWith("summary") -> Icons.Default.BarChart to blue
        k.startsWith("no_spend") -> Icons.Default.Receipt to blue
        else -> Icons.Default.Notifications to Color(0xFF6B7280)
    }
}

// What the tap will open, shown as a small hint
private fun targetLabel(target: String): String? = when {
    target == "budgets" -> "Open Budget"
    target == "savings_goals" -> "Open Goals"
    target == "accounts" -> "Open Accounts"
    target == "analytics" -> "Open Summary"
    target == "transactions" -> "Open Expenses"
    target.startsWith("transaction_detail/") -> "Open expense"
    target.startsWith("add_transaction") -> "Add expense"
    else -> null
}

private fun timeText(item: NotificationItem): String {
    if (item.createdAt <= 0L) return item.timeAgo
    val minutes = (System.currentTimeMillis() - item.createdAt) / 60000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 1440 -> "${minutes / 60} hr ago"
        minutes < 10080 -> "${minutes / 1440} d ago"
        else -> DateUtils.format(
            Instant.ofEpochMilli(item.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: MoneyPathViewModel,
    onNavigateBack: () -> Unit,
    onOpenTarget: (String) -> Unit = {}
) {
    val notifications by viewModel.notifications.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // Mark notifications as read when viewed so the badge resets
    LaunchedEffect(Unit) {
        viewModel.markAllNotificationsAsRead()
    }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
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
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                            Text("Mark all read", color = primaryGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAllNotifications() }) {
                            Text("Clear all", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundLight)
            )
        },
        containerColor = backgroundLight
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (userProfile.isNotificationsEnabled) "You're all caught up."
                        else "Notifications are turned off in Settings.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF6B7280))
                    )
                    if (userProfile.isNotificationsEnabled) {
                        Text(
                            text = "Budget, goal and account alerts will appear here.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationCardItem(
                        item = item,
                        primaryGreen = primaryGreen,
                        onClick = {
                            viewModel.markNotificationAsRead(item.id)
                            if (item.target.isNotBlank()) onOpenTarget(item.target)
                        },
                        onDismiss = { viewModel.dismissNotification(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCardItem(
    item: NotificationItem,
    primaryGreen: Color,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, color) = notificationStyle(item)
    val hint = targetLabel(item.target)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) Color.White else Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(primaryGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF374151),
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeText(item),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280))
                    )
                    if (hint != null) {
                        Text(
                            text = "$hint >",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = primaryGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}