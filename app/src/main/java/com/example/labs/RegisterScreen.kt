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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    themeManager: ThemeManager
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf("") }
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Регистрация",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Создать аккаунт",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Присоединяйтесь к игре!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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
                            registerError = ""
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, "Email")
                        },
                        isError = emailError,
                        supportingText = {
                            if (emailError) {
                                Text("Введите корректный email")
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
                            registerError = ""
                        },
                        label = { Text("Пароль") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, "Пароль")
                        },
                        isError = passwordError,
                        supportingText = {
                            if (passwordError) {
                                Text("Пароль должен содержать минимум 6 символов")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordError = false
                            registerError = ""
                        },
                        label = { Text("Подтвердите пароль") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, "Подтверждение пароля")
                        },
                        isError = confirmPasswordError,
                        supportingText = {
                            if (confirmPasswordError) {
                                Text("Пароли не совпадают")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (registerError.isNotEmpty()) {
                        Text(
                            registerError,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            emailError = email.isEmpty() || !email.contains("@")
                            passwordError = password.length < 6
                            confirmPasswordError = password != confirmPassword

                            if (!emailError && !passwordError && !confirmPasswordError) {
                                val success = authManager.registerUser(email, password)
                                if (success) {
                                    onRegisterSuccess()
                                } else {
                                    registerError = "Пользователь с таким email уже существует"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Зарегистрироваться",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Уже есть аккаунт? Войдите")
                    }
                }
            }
        }
    }
}