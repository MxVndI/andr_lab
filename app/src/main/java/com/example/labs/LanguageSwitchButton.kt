package com.example.labs

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun LanguageSwitchButton(themeManager: ThemeManager) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Edit, "Сменить язык")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Русский") },
                onClick = {
                    themeManager.setLanguage("ru")
                    expanded = false
                    // Активность пересоздастся автоматически через MainActivity
                    (context as? android.app.Activity)?.recreate()
                }
            )
            DropdownMenuItem(
                text = { Text("English") },
                onClick = {
                    themeManager.setLanguage("en")
                    expanded = false
                    // Активность пересоздастся автоматически через MainActivity
                    (context as? android.app.Activity)?.recreate()
                }
            )
        }
    }
}