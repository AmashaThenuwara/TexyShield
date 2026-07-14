package com.example.smartfactory.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

import com.example.smartfactory.firebase.FirebaseAuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


@Composable
fun RegisterScreen(

    onRegisterSuccess: () -> Unit

) {


    var name by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }


    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }



    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement = Arrangement.Center

    ) {



        Text(

            text = "Create Account",

            style = MaterialTheme.typography.headlineMedium

        )


        Spacer(modifier = Modifier.height(20.dp))



        OutlinedTextField(

            value = name,

            onValueChange = { name = it },

            label = { Text("Name") },

            modifier = Modifier.fillMaxWidth()

        )


        Spacer(modifier = Modifier.height(10.dp))



        OutlinedTextField(

            value = email,

            onValueChange = { email = it },

            label = { Text("Email") },

            modifier = Modifier.fillMaxWidth()

        )



        Spacer(modifier = Modifier.height(10.dp))



        OutlinedTextField(

            value = password,

            onValueChange = { password = it },

            label = { Text("Password") },

            visualTransformation = PasswordVisualTransformation(),

            modifier = Modifier.fillMaxWidth()

        )



        Spacer(modifier = Modifier.height(20.dp))



        Button(

            onClick = {


                isLoading = true

                errorMessage = null



                FirebaseAuth.getInstance()

                    .createUserWithEmailAndPassword(

                        email.trim(),

                        password

                    )

                    .addOnSuccessListener {


                        val uid = FirebaseAuth.getInstance()

                            .currentUser!!

                            .uid



                        val database = FirebaseDatabase.getInstance(

                            "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"

                        )

                            .reference



                        val userData = mapOf(

                            "name" to name,

                            "email" to email.trim(),

                            "role" to "worker"

                        )



                        database.child("Users")

                            .child(uid)

                            .setValue(userData)



                            .addOnSuccessListener {


                                isLoading = false

                                onRegisterSuccess()


                            }



                    }


                    .addOnFailureListener {


                        isLoading = false

                        errorMessage = it.message


                    }


            },

            enabled = !isLoading,

            modifier = Modifier.fillMaxWidth()

        ) {


            if(isLoading){

                CircularProgressIndicator(

                    modifier = Modifier.size(20.dp)

                )

            }

            else{

                Text("Register")

            }


        }



        if(errorMessage != null){


            Spacer(modifier = Modifier.height(10.dp))


            Text(

                text = errorMessage!!,

                color = MaterialTheme.colorScheme.error

            )


        }


    }


}