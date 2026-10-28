package com.example.moneypath.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: MoneyPathViewModel,
    onNavigateBack: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        focusedBorderColor = Color(0xFFE5E7EB),
        unfocusedBorderColor = Color(0xFFE5E7EB)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Change Password",
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter your current password and choose a secure new password (at least 6 characters).",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
            )

            // Current Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Current Password", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF374151)))
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; currentError = null; errorMessage = null },
                    placeholder = { Text("Enter current password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = currentError != null,
                    supportingText = { currentError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )
            }

            // New Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("New Password", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF374151)))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; newError = null; errorMessage = null },
                    placeholder = { Text("Enter new password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = newError != null,
                    supportingText = { newError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )
            }

            // Confirm New Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Confirm New Password", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF374151)))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmError = null; errorMessage = null },
                    placeholder = { Text("Confirm new password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = confirmError != null,
                    supportingText = { confirmError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = fieldColors
                )
            }

            errorMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFEF4444), fontWeight = FontWeight.Medium))
            }
            successMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF16A34A), fontWeight = FontWeight.Medium))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    currentError = if (currentPassword.isBlank()) "Enter current password" else null
                    newError = if (newPassword.length < 6) "New password must be at least 6 characters" else null
                    confirmError = if (newPassword != confirmPassword) "Passwords do not match" else null

                    if (currentError == null && newError == null && confirmError == null) {
                        val updated = viewModel.updatePassword(currentPassword, newPassword)
                        if (updated) {
                            successMessage = "Password updated successfully!"
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        } else {
                            errorMessage = "Failed to update password. Check your current password."
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
                    text = "Update Password",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}
