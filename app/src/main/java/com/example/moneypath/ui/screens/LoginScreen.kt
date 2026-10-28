package com.example.moneypath.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneypath.ui.MoneyPathViewModel
import com.example.moneypath.ui.theme.PrimaryGreen

@Composable
fun LoginScreen(
    viewModel: MoneyPathViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val primaryGreen = PrimaryGreen
    val backgroundLight = Color(0xFFF4F7F5)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Logo & Brand (Using exact same custom MoneyPathLogo)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                MoneyPathLogo(logoSize = 40.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "MoneyPath",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    fontSize = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Heading & Subtitle
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        fontSize = 26.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Log in to your account",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val lErr = loginError
            if (lErr != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = lErr,
                        color = Color(0xFFB91C1C),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Email or Phone Number Input
            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = {
                    emailOrPhone = it
                    if (it.isNotBlank()) emailError = null
                    loginError = null
                },
                placeholder = { Text("Email or phone number", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF6B7280)) },
                singleLine = true,
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = if (emailError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                    unfocusedBorderColor = if (emailError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB)
                )
            )
            val emErr = emailError
            if (emErr != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(emErr, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (it.isNotBlank()) passwordError = null
                    loginError = null
                },
                placeholder = { Text("Password", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6B7280)) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                singleLine = true,
                isError = passwordError != null,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = if (passwordError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                    unfocusedBorderColor = if (passwordError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB)
                )
            )
            val pwErr = passwordError
            if (pwErr != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(pwErr, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = primaryGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log In Button
            Button(
                onClick = {
                    var isValid = true
                    if (emailOrPhone.isBlank()) {
                        emailError = "Email or phone number cannot be empty"
                        isValid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Password cannot be empty"
                        isValid = false
                    }

                    if (isValid) {
                        val success = viewModel.loginAccount(emailOrPhone.trim(), password)
                        if (success) {
                            onLoginSuccess()
                        } else {
                            loginError = "Invalid credentials or account not registered. Please check your email and password."
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
                    text = "Log in",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Register Prompt
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF6B7280), fontWeight = FontWeight.Normal)) {
                        append("Don't have an account? ")
                    }
                    withStyle(style = SpanStyle(color = primaryGreen, fontWeight = FontWeight.Bold)) {
                        append("Sign up")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}
