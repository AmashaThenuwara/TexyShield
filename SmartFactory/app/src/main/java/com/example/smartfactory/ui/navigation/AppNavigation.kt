/*
 * File: AppNavigation.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.navigation

// ============================================================
// AppNavigation.kt
// Smart Garment Factory — Industry 4.0
// ============================================================
// Navigation flow:
//   splash → landing → login ─┐─→ worker dashboard + sub-pages
//                    → register ─┘─→ admin dashboard + sub-pages
// ============================================================

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.smartfactory.firebase.UserManager
import com.example.smartfactory.ui.LandingScreen
import com.example.smartfactory.ui.SplashScreen
import com.example.smartfactory.ui.admin.AdminDashboard
import com.example.smartfactory.ui.auth.RegisterScreen
import com.example.smartfactory.ui.auth.LoginScreen
import com.example.smartfactory.ui.worker.WorkerDashboard
import com.example.smartfactory.ui.worker.attendance.QRAttendanceScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // Splash Screen
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("landing") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Landing Page
        composable("landing") {
            LandingScreen(
                onCreateAccountClick = {
                    navController.navigate("register")
                },
                onLoginClick = {
                    navController.navigate("login")
                }
            )
        }

        // Login Screen
        composable("login") {
            LoginScreen(
                onLoginSuccess = { email ->
                    if (email.lowercase() == "admin@gmail.com") {
                        navController.navigate("admin") {
                            popUpTo("landing") { inclusive = false }
                        }
                    } else {
                        UserManager.getUserByEmail(email) { user ->
                            if (user?.role == "admin") {
                                navController.navigate("admin") {
                                    popUpTo("landing") { inclusive = false }
                                }
                            } else {
                                navController.navigate("worker") {
                                    popUpTo("landing") { inclusive = false }
                                }
                            }
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Register Screen
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Admin Dashboard
        composable("admin") {
            AdminDashboard(navController = navController)
        }

        // Worker Dashboard
        composable("worker") {
            WorkerDashboard(navController = navController)
        }

        // QR Attendance
        composable("attendance") {
            QRAttendanceScreen()
        }

        // Emergency Report
        composable("report") {
            com.example.smartfactory.ui.worker.report.EmergencyReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Garment PPE Camera
        composable("ppe-camera") {
            com.example.smartfactory.ui.worker.camera.CameraPermission {
                com.example.smartfactory.ui.worker.camera.PPECameraScreen()
            }
        }

        // Worker Ergonomics / Health Camera
        composable("worker-health") {
            com.example.smartfactory.ui.worker.camera.CameraPermission {
                com.example.smartfactory.ui.worker.health.WorkerHealthScreen()
            }
        }

        // Worker Profile
        composable("profile") {
            com.example.smartfactory.ui.worker.profile.ProfileScreen(
                onLogoutClick = {
                    navController.navigate("landing") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // AR Machine Assistant
        composable("ar-assistant") {
            com.example.smartfactory.ui.worker.camera.CameraPermission {
                com.example.smartfactory.ui.worker.ARAssistantScreen()
            }
        }

        // Blockchain Safety Ledger (Admin)
        composable("blockchain-ledger") {
            com.example.smartfactory.ui.admin.BlockchainLedgerScreen(navController)
        }

        // Attendance Blockchain Ledger (Admin)
        composable("attendance-ledger") {
            com.example.smartfactory.ui.admin.AttendanceLedgerScreen(navController)
        }
    }
}