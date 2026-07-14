package com.example.smartfactory.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.*

import com.example.smartfactory.firebase.UserManager

import com.example.smartfactory.ui.admin.AdminDashboard
import com.example.smartfactory.ui.auth.RegisterScreen
import com.example.smartfactory.ui.auth.LoginScreen
import com.example.smartfactory.ui.worker.WorkerDashboard
import com.example.smartfactory.ui.worker.attendance.QRAttendanceScreen


@Composable
fun AppNavigation(){


    val navController = rememberNavController()



    NavHost(

        navController = navController,

        startDestination = "login"

    ){



        composable("login"){



            LoginScreen(


                onLoginSuccess = {email->



                    if (email.lowercase() == "admin@gmail.com") {
                        navController.navigate("admin")
                    } else {
                        // For any other email, we check the database (or default to worker)
                        UserManager.getUserByEmail(email) { user ->
                            if (user?.role == "admin") {
                                navController.navigate("admin")
                            } else {
                                navController.navigate("worker")
                            }
                        }
                    }



                },



                onRegisterClick={


                    navController.navigate("register")


                }


            )


        }




        composable("register"){



            RegisterScreen(


                onRegisterSuccess={


                    navController.navigate("login"){


                        popUpTo("register"){

                            inclusive=true

                        }


                    }


                }


            )


        }




        composable("admin"){


            AdminDashboard()


        }



        composable("worker"){

            WorkerDashboard(

                navController = navController

            )

        }

        composable("attendance"){

            QRAttendanceScreen()

        }


        composable("report"){
            com.example.smartfactory.ui.worker.report.EmergencyReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("ppe-camera"){
            com.example.smartfactory.ui.camera.CameraPermission {
                com.example.smartfactory.ui.camera.PPECameraScreen()
            }
        }
        
        composable("worker-health"){
            com.example.smartfactory.ui.camera.CameraPermission {
                com.example.smartfactory.ui.health.WorkerHealthScreen()
            }
        }

        composable("profile"){
            com.example.smartfactory.ui.worker.profile.ProfileScreen(
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("worker") { inclusive = true }
                        popUpTo("admin") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }



}