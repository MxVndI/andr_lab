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

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView

    private val sharedPreferences by lazy {
        getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            if (validateInputs()) {
                attemptLogin()
            }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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

        return isValid
    }

    private fun attemptLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Получаем сохраненные данные пользователя
        val savedEmail = sharedPreferences.getString("user_email", "")
        val savedPassword = sharedPreferences.getString("user_password", "")

        if (email == savedEmail && password == savedPassword) {
            // Успешный вход
            Snackbar.make(loginButton, "Вход выполнен успешно!", Snackbar.LENGTH_LONG).show()

            // Переходим на главный экран
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            with(sharedPreferences.edit()) {
                putBoolean("is_logged_in", true)
                apply()
            }
            finish()
        } else {
            // Ошибка входа
            Snackbar.make(loginButton, "Неверный email или пароль", Snackbar.LENGTH_LONG).show()
        }
    }
}