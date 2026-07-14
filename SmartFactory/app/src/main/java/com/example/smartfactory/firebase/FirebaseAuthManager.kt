package com.example.smartfactory.firebase

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password)

            .addOnSuccessListener {

                onSuccess()

            }

            .addOnFailureListener {

                onFailure(it.message ?: "Login Failed")

            }

    }

}