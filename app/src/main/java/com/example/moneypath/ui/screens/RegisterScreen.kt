package com.example.moneypath.ui.screens

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MoneyPathViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

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
            verticalArrangement = Arrangement.Top
        ) {
            // Top Bar with Back Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToLogin, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111827)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Heading & Subtitle
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Create your account",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        fontSize = 28.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Join MoneyPath and start managing your finances.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (it.isNotBlank()) fullNameError = null
                },
                placeholder = { Text("Full name", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF6B7280)) },
                singleLine = true,
                isError = fullNameError != null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = if (fullNameError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                    unfocusedBorderColor = if (fullNameError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB)
                )
            )
            val fnErr = fullNameError
            if (fnErr != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(fnErr, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email or Phone Number Input
            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = {
                    emailOrPhone = it
                    if (it.isNotBlank()) emailError = null
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

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    if (it.isNotBlank()) confirmPasswordError = null
                },
                placeholder = { Text("Confirm password", color = Color(0xFF9CA3AF)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6B7280)) },
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                singleLine = true,
                isError = confirmPasswordError != null,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = if (confirmPasswordError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                    unfocusedBorderColor = if (confirmPasswordError != null) Color(0xFFEF4444) else Color(0xFFE5E7EB)
                )
            )
            val cpwErr = confirmPasswordError
            if (cpwErr != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(cpwErr, color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sign Up Button
            Button(
                onClick = {
                    var isValid = true
                    if (fullName.isBlank()) {
                        fullNameError = "Full name cannot be empty"
                        isValid = false
                    }

                    val input = emailOrPhone.trim()
                    if (input.isBlank()) {
                        emailError = "Email or phone number cannot be empty"
                        isValid = false
                    } else {
                        val isEmail = input.contains("@")
                        val isValidEmail = isEmail && Patterns.EMAIL_ADDRESS.matcher(input).matches()
                        val isValidPhone = !isEmail && input.any { it.isDigit() } && input.length >= 7

                        if (isEmail && !isValidEmail) {
                            emailError = "Please enter a valid email address"
                            isValid = false
                        } else if (!isEmail && !isValidPhone) {
                            emailError = "Please enter a valid phone number"
                            isValid = false
                        }
                    }

                    if (password.isBlank()) {
                        passwordError = "Password cannot be empty"
                        isValid = false
                    } else if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters"
                        isValid = false
                    }
                    if (confirmPassword.isBlank()) {
                        confirmPasswordError = "Please confirm your password"
                        isValid = false
                    } else if (password != confirmPassword) {
                        confirmPasswordError = "Passwords do not match"
                        isValid = false
                    }

                    if (isValid) {
                        val success = viewModel.registerAccount(fullName.trim(), input, password)
                        if (success) {
                            onRegisterSuccess()
                        } else {
                            emailError = "Registration failed. Please check your details."
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
                    text = "Sign up",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Login Link
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF6B7280), fontWeight = FontWeight.Normal)) {
                        append("Already have an account? ")
                    }
                    withStyle(style = SpanStyle(color = primaryGreen, fontWeight = FontWeight.Bold)) {
                        append("Log in")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}
