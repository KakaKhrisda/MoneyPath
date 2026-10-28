package com.example.moneypath.ui.screens

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MoneyPathViewModel,
    onNavigateToGoals: () -> Unit,
    onNavigateToAddIncome: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCurrency: () -> Unit,
    onNavigateToOfflineData: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelpSupport: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)

    Scaffold(
        containerColor = backgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Centered Profile Avatar & Name (Supports uploaded profile picture)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(primaryGreen),
                contentAlignment = Alignment.Center
            ) {
                if (userProfile.profileImageUri.isNotBlank()) {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        },
                        update = { iv ->
                            iv.setImageURI(Uri.parse(userProfile.profileImageUri))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = userProfile.name.take(1).uppercase().ifBlank { "J" },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 28.sp
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = userProfile.name.ifBlank { "John Doe" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        fontSize = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = userProfile.email.ifBlank { "john@example.com" },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MANAGE Section Card (4 parts in exact requested order: Goals, Categories, Spending Summary, Bills)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "MANAGE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        SettingRowItem(
                            icon = Icons.Default.Savings,
                            iconBg = Color(0xFF10B981),
                            title = "Goals",
                            onClick = onNavigateToGoals
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        SettingRowItem(
                            icon = Icons.Default.AddCircle,
                            iconBg = Color(0xFF16A34A),
                            title = "Add Income",
                            onClick = onNavigateToAddIncome
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        SettingRowItem(
                            icon = Icons.Default.Category,
                            iconBg = Color(0xFFF97316),
                            title = "Categories",
                            onClick = onNavigateToCategories
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        SettingRowItem(
                            icon = Icons.Default.PieChart,
                            iconBg = Color(0xFF3B82F6),
                            title = "Spending Summary",
                            onClick = onNavigateToAnalytics
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        SettingRowItem(
                            icon = Icons.Default.Receipt,
                            iconBg = Color(0xFF8B5CF6),
                            title = "Bills",
                            onClick = onNavigateToBills
                        )
                    }
                }
            }

            // PREFERENCES Section Card
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        SettingRowItem(
                            icon = Icons.Default.Notifications,
                            iconBg = Color(0xFFEF4444),
                            title = "Notifications",
                            onClick = onNavigateToNotifications
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        SettingRowItem(
                            icon = Icons.Default.CurrencyExchange,
                            iconBg = Color(0xFF8B5CF6),
                            title = "Currency",
                            trailingText = userProfile.currencyCode.ifBlank { "MWK" },
                            onClick = onNavigateToCurrency
                        )
                        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                        SettingRowItem(
                            icon = Icons.Default.CloudOff,
                            iconBg = Color(0xFF0D9488),
                            title = "Offline Data",
                            onClick = onNavigateToOfflineData
                        )
                    }
                }
            }

            // OTHERS / SETTINGS Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    SettingRowItem(
                        icon = Icons.Default.Settings,
                        iconBg = Color(0xFF4B5563),
                        title = "Settings",
                        onClick = onNavigateToSettings
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    SettingRowItem(
                        icon = Icons.Default.Help,
                        iconBg = Color(0xFF3B82F6),
                        title = "Help and Support",
                        onClick = onNavigateToHelpSupport
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    SettingRowItem(
                        icon = Icons.Default.Info,
                        iconBg = primaryGreen,
                        title = "About MoneyPath",
                        onClick = onNavigateToAbout
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    SettingRowItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        iconBg = Color(0xFFEF4444),
                        title = "Log Out",
                        titleColor = Color(0xFFEF4444),
                        onClick = onLogout
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingRowItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    trailingText: String? = null,
    titleColor: Color = Color(0xFF111827),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
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
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    fontSize = 15.sp
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
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
