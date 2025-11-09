package com.example.labs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun ThemeSwitchButton(themeManager: ThemeManager) {
    IconButton(onClick = { themeManager.toggleTheme() }) {
        Icon(
            imageVector = if (themeManager.isDarkTheme) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (themeManager.isDarkTheme) "Светлая тема" else "Темная тема"
        )
    }
}