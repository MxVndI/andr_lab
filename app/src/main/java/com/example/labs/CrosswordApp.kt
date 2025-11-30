package com.example.labs

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun CrosswordApp(themeManager: ThemeManager) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    // Всегда показываем экран входа
    val startDestination = "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("main") },
                onNavigateToRegister = { navController.navigate("register") },
                themeManager = themeManager
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("main") },
                onNavigateToLogin = { navController.navigate("login") },
                themeManager = themeManager
            )
        }
        composable("main") {
            MainCrosswordScreen(
                onLogout = {
                    authManager.logout()
                    navController.navigate("login")
                },
                onShowStats = { name, level ->
                    navController.navigate("stats/$name/$level")
                },
                themeManager = themeManager
            )
        }
        composable("stats/{name}/{level}") { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val level = backStackEntry.arguments?.getString("level") ?: "1"
            StatisticsScreen(
                userName = name,
                userLevel = level.toInt(),
                onBack = { navController.popBackStack() },
                themeManager = themeManager
            )
        }
    }
}