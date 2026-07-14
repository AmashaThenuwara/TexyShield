package com.example.smartfactory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.smartfactory.ui.admin.AdminDashboard
import com.example.smartfactory.ui.login.LoginScreen
import com.example.smartfactory.ui.theme.SmartFactoryTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Single setContent call — the previous code had TWO nested setContent {}
        // which caused an immediate crash on launch.
        setContent {
            SmartFactoryTheme {

                // Check if the user is already signed in from a previous session.
                // FirebaseAuth.currentUser is non-null if a valid session exists.
                var isLoggedIn by remember {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }

                if (isLoggedIn) {
                    // User is authenticated — show the dashboard
                    AdminDashboard()
                } else {
                    // User is not authenticated — show the login screen
                    LoginScreen(
                        onLoginSuccess = {
                            // Switch to dashboard after successful login
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }
}