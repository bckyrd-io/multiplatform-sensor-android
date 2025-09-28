package com.example.nativesensor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nativesensor.ui.screens.*

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Registration : Screen("registration")
    object Dashboard : Screen("dashboard")
    object AdminDashboard : Screen("adminDashboard")
    object LogActivity : Screen("logActivity")
    object History : Screen("history")
    object Goals : Screen("goals")
    object Profile : Screen("profile")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Registration.route) {
            RegistrationScreen(navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController)
        }
        composable(Screen.LogActivity.route) {
            LogActivityScreen(navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController)
        }

        composable(Screen.Goals.route) {
            GoalsScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavGraphPreview() {
    AppNavGraph(navController = rememberNavController())
}
