package com.example.labs

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.labs.ui.theme.LabsTheme

class MainActivity : ComponentActivity() {
    private var keepSplashOnScreen = true
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        setContent {
            val context = LocalContext.current
            val themeManager = remember { ThemeManager(context) }

            var dbInitialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                Log.d(TAG, "Starting database initialization...")
                val success = initializeDatabase(context)
                if (success) {
                    Log.d(TAG, "Database initialized successfully")
                    dbInitialized = true
                } else {
                    Log.e(TAG, "Database initialization failed")
                }
                keepSplashOnScreen = false // Скрыть нативный сплэш-скрин
            }

            LabsTheme(darkTheme = themeManager.isDarkTheme) {
                if (dbInitialized) {
                    CrosswordApp(themeManager)
                } else {
                    // Показать экран ошибки после скрытия сплэш-скрина
                    ErrorContent("Failed to initialize database")
                }
            }
        }
    }

    private suspend fun initializeDatabase(context: android.content.Context): Boolean {
        return try {
            val database = CrosswordDatabase.getInstance(context)
            val wordsRepository = WordsRepository(database.wordsDao())
            wordsRepository.initializeWords()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Database initialization error: ${e.message}", e)
            false
        }
    }

    @Composable
    fun ErrorContent(message: String?) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message ?: "Unknown error",
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}