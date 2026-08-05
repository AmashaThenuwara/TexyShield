/*
 * File: MainActivity.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.smartfactory.ui.navigation.AppNavigation
import com.example.smartfactory.ui.theme.SmartFactoryTheme

/**
 * MainActivity is the single entry point for our entire app.
 * It uses AppNavigation to route between Login, Admin, and Worker dashboards.
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        setContent {
            SmartFactoryTheme {
                AppNavigation()
            }
        }
    }
}