package com.example.labs

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView

    private val sharedPreferences by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Валидация email
        val email = emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            emailInputLayout.error = "Введите email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Введите корректный email"
            isValid = false
        } else {
            emailInputLayout.error = null
        }

        // Валидация пароля
        val password = passwordEditText.text.toString().trim()
        if (password.isEmpty()) {
            passwordInputLayout.error = "Введите пароль"
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = "Пароль должен содержать минимум 6 символов"
            isValid = false
        } else {
            passwordInputLayout.error = null
        }

        // Валидация подтверждения пароля
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Подтвердите пароль"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Пароли не совпадают"
            isValid = false
        } else {
            confirmPasswordInputLayout.error = null
        }

        return isValid
    }

    private fun registerUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Сохраняем данные пользователя в SharedPreferences
        with(sharedPreferences.edit()) {
            putString("user_email", email)
            putString("user_password", password)
            putBoolean("is_logged_in", true)
            apply()
        }

        // Показываем сообщение об успехе
        Snackbar.make(registerButton, "Регистрация прошла успешно!", Snackbar.LENGTH_LONG).show()

        // Переходим на главный экран
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}