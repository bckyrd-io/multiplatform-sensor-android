package com.example.figcompose.util

/**
 * Network configuration for the app
 */
object NetworkConfig {
    // For Android Emulator: 10.0.2.2 is the special IP address to connect to your host machine's localhost
    // For physical device: Use your computer's local IP address (e.g., 192.168.x.x)
    var baseUrl = "http://192.168.1.178:4000/"
        set(value) {
            // Ensure the URL ends with a slash
            field = if (value.endsWith("/")) value else "$value/"
        }
    
    // Network timeout in seconds
    const val TIMEOUT_SECONDS = 30L
}
