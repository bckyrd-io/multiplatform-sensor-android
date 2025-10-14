package com.example.wear.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController

@Composable
fun WearNavigation() {
    val navController = rememberSwipeDismissableNavController()
    
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "userSelection"
    ) {
        composable("userSelection") {
            UserSelectionScreen(navController = navController)
        }
        
        composable("sessionSelection/{playerId}/{playerName}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")?.toIntOrNull() ?: 0
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            SessionSelectionScreen(
                navController = navController,
                playerId = playerId,
                playerName = playerName
            )
        }
        
        composable("userMetrics/{playerId}/{playerName}/{sessionId}/{sessionTitle}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")?.toIntOrNull() ?: 0
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            val sessionId = backStackEntry.arguments?.getString("sessionId")?.toIntOrNull() ?: 0
            val sessionTitle = backStackEntry.arguments?.getString("sessionTitle") ?: ""
            UserMetricsScreen(
                navController = navController,
                playerId = playerId,
                playerName = playerName,
                sessionId = sessionId,
                sessionTitle = sessionTitle
            )
        }
    }
}
