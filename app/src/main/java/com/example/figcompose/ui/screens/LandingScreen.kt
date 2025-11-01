package com.example.figcompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.figcompose.ui.theme.TextPrimary
import com.example.figcompose.ui.theme.TextSecondary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.figcompose.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onLogin: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = {
//                    Text(
//                        text = "Performance Tracker",
//                        style = MaterialTheme.typography.titleMedium.copy(
//                            fontWeight = FontWeight.SemiBold,
//                            color = TextPrimary
//                        )
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { /* TODO */ }) {
//                        Icon(
//                            imageVector = Icons.Filled.CheckCircle,
//                            contentDescription = "App icon",
//                            tint = TextPrimary
//                        )
//                    }
//                }
//            )
//        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Centered content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.silver_strikers),
                    contentDescription = "Silver Strikers logo",
                    modifier = Modifier.size(175.dp)
                )
                Spacer(Modifier.height(175.dp))
//                Text(
//                    text = "Silver Strikers Fc",
//                    style = MaterialTheme.typography.headlineSmall.copy(
//                        fontWeight = FontWeight.ExtraBold,
//                        fontSize = 28.sp,
//                        color = TextPrimary
//                    ),
//                    textAlign = TextAlign.Center
//                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Elevate your game with real-time performance insights and personalized feedback.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Primary: Log In
                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Secondary: Sign Up
                OutlinedButton(
                    onClick = onSignUp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    MaterialTheme {
        LandingScreen()
    }
}
