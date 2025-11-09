package com.example.labs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    themeManager: ThemeManager // Добавлен параметр
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Кнопки настроек в правом верхнем углу
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ThemeSwitchButton(themeManager = themeManager)
                Spacer(modifier = Modifier.width(8.dp))
                LanguageSwitchButton(themeManager = themeManager)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logo and title card - исправлено (была лишняя скобка)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Логотип",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Кроссворд Генератор",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Войдите в систему",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                            loginError = false
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, "Email")
                        },
                        isError = emailError,
                        trailingIcon = {
                            if (emailError) Icon(
                                Icons.Default.Close,
                                "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        supportingText = {
                            if (emailError) {
                                Text("Введите email")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                            loginError = false
                        },
                        label = { Text("Пароль") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, "Пароль")
                        },
                        isError = passwordError,
                        trailingIcon = {
                            if (passwordError) Icon(
                                Icons.Default.Close,
                                "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        supportingText = {
                            if (passwordError) {
                                Text("Введите пароль")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (loginError) {
                        Text(
                            "Неверный email или пароль",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            emailError = email.isEmpty()
                            passwordError = password.isEmpty()

                            if (!emailError && !passwordError) {
                                val success = authManager.loginUser(email, password)
                                if (success) {
                                    onLoginSuccess()
                                } else {
                                    loginError = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Войти",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Нет аккаунта? Зарегистрируйтесь")
                    }
                }
            }
        }
    }
}