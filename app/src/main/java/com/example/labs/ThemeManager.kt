package com.example.labs

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.*

class ThemeManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    var isDarkTheme by mutableStateOf(
        sharedPreferences.getBoolean("is_dark_theme", false)
    )
        private set

    var currentLanguage by mutableStateOf(
        sharedPreferences.getString("language", "ru") ?: "ru"
    )
        private set

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        sharedPreferences.edit().putBoolean("is_dark_theme", isDarkTheme).apply()
    }

    fun setTheme(isDark: Boolean) {
        isDarkTheme = isDark
        sharedPreferences.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun setLanguage(language: String) {
        currentLanguage = language
        sharedPreferences.edit().putString("language", language).apply()
        updateAppLanguage(language)
    }

    private fun updateAppLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        }

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}