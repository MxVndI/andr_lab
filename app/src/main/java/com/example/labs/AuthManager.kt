package com.example.labs

import android.content.Context
import android.content.SharedPreferences
import com.example.labs.database.AppDatabase
import com.example.labs.database.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AuthManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val userDao = AppDatabase.getDatabase(context).userDao()

    // ========== РЕГИСТРАЦИЯ ==========
    fun registerUser(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) return false

        val existingEmail = sharedPreferences.getString("email", null)
        if (existingEmail == email) return false

        // 1️⃣ Сохраняем в SharedPreferences
        with(sharedPreferences.edit()) {
            putString("email", email)
            putString("password", password)
            putString("user_name", email.substringBefore("@"))
            putBoolean("is_logged_in", true)
            apply()
        }

        // 2️⃣ ДОБАВЛЯЕМ В ROOM
        CoroutineScope(Dispatchers.IO).launch {
            userDao.insert(
                UserEntity(
                    email = email,
                    password = password,
                    coins = 50 // стартовый баланс
                )
            )
        }

        return true
    }

    // ========== ЛОГИН ==========
    fun loginUser(email: String, password: String): Boolean {
        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

        val success = savedEmail == email && savedPassword == password
        if (success) {
            sharedPreferences.edit().putBoolean("is_logged_in", true).apply()

            // ✅ ГАРАНТИРУЕМ, ЧТО ПОЛЬЗОВАТЕЛЬ ЕСТЬ В БД
            CoroutineScope(Dispatchers.IO).launch {
                val user = userDao.getUser(email).firstOrNull()
                if (user == null) {
                    userDao.insert(
                        UserEntity(
                            email = email,
                            password = password,
                            coins = 50
                        )
                    )
                }
            }
        }
        return success
    }

    fun getCurrentUser(): String? {
        return sharedPreferences.getString("email", null)
    }

    fun getUserName(): String {
        return sharedPreferences.getString("user_name", "Пользователь") ?: "Пользователь"
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false) &&
                sharedPreferences.getString("email", null) != null
    }

    fun logout() {
        sharedPreferences.edit().putBoolean("is_logged_in", false).apply()
    }

    fun getCurrentEmail(): String? {
        return sharedPreferences.getString("email", null)
    }

    fun saveAvatarPath(path: String) {
        sharedPreferences.edit().putString("avatar_path", path).apply()
    }

    fun getAvatarPath(): String? {
        return sharedPreferences.getString("avatar_path", null)
    }

    fun removeAvatar() {
        sharedPreferences.edit().remove("avatar_path").apply()
    }
}
