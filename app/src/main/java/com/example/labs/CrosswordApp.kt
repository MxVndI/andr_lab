// CrosswordApp.kt
package com.example.labs

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.labs.LoginScreen
import com.example.labs.MainCrosswordScreen
import com.example.labs.RegisterScreen

@Composable
fun CrosswordApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("main") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("main") },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable("main") {
            MainCrosswordScreen(
                onLogout = { navController.navigate("login") }
            )
        }
    }
}