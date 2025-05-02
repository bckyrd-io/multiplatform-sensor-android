package com.example.nativesensor.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Image Placeholder
        Image(
            painter = painterResource(id = android.R.drawable.ic_media_play),
            contentDescription = "Welcome Image",
            modifier = Modifier
                .size(120.dp)
        )

        // Title and Subtitle
        Text(
            text = "Welcome to Fitness Tracker",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Track your fitness journey with ease",
            style = MaterialTheme.typography.bodyLarge
        )

        // Buttons
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Login")
        }

        Button(
            onClick = { navController.navigate("registration") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Sign Up")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(navController = NavController(LocalContext.current))
}
