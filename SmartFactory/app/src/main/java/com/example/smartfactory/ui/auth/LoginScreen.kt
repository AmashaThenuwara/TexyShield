package com.example.smartfactory.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

import com.example.smartfactory.firebase.FirebaseAuthManager


@Composable
fun LoginScreen(

    onLoginSuccess:(String)->Unit,

    onRegisterClick:()->Unit

){


    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var loading by remember { mutableStateOf(false) }



    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement = Arrangement.Center

    ){


        Text(

            "Smart Factory Login",

            style = MaterialTheme.typography.headlineMedium

        )


        Spacer(modifier = Modifier.height(20.dp))



        OutlinedTextField(

            value=email,

            onValueChange={email=it},

            label={Text("Email")},

            modifier=Modifier.fillMaxWidth()

        )



        Spacer(modifier = Modifier.height(10.dp))



        OutlinedTextField(

            value=password,

            onValueChange={password=it},

            label={Text("Password")},

            visualTransformation=PasswordVisualTransformation(),

            modifier=Modifier.fillMaxWidth()

        )



        Spacer(modifier = Modifier.height(20.dp))



        Button(

            onClick={


                loading=true


                FirebaseAuthManager.login(

                    email.trim(),

                    password,


                    onSuccess={


                        loading=false


                        onLoginSuccess(email.trim())


                    },


                    onFailure={


                        loading=false

                        errorMessage=it


                    }


                )


            },

            modifier=Modifier.fillMaxWidth()

        ){

            if(loading)

                CircularProgressIndicator()

            else

                Text("Login")


        }



        Spacer(modifier = Modifier.height(15.dp))



        TextButton(

            onClick = onRegisterClick

        ){

            Text("Don't have an account? Register")

        }



        if(errorMessage!=null){

            Text(

                text=errorMessage!!,

                color=MaterialTheme.colorScheme.error

            )

        }


    }


}