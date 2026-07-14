package com.example.smartfactory.firebase

import com.google.firebase.auth.FirebaseAuth

/**
 * FirebaseAuthManager handles all user authentication operations.
 * It provides a clean, reusable way to interact with Firebase Auth
 * from anywhere in our app without writing duplicate code.
 */
object FirebaseAuthManager {

    // Instance of Firebase Auth to manage users
    private val auth = FirebaseAuth.getInstance()

    /**
     * Attempts to log a user in with their email and password.
     * 
     * @param email The user's email address (e.g., admin@gmail.com)
     * @param password The user's password
     * @param onSuccess Callback triggered when login succeeds
     * @param onFailure Callback triggered with an error message when login fails
     */
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Firebase built-in function to sign in
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Login was successful!
                onSuccess()
            }
            .addOnFailureListener { exception ->
                // Login failed (wrong password, no internet, etc.)
                // We pass the error message back to the UI
                onFailure(exception.localizedMessage ?: "Login Failed")
            }
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Checks if a user is currently logged in.
     * @return true if a user is signed in, false otherwise
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Gets the currently logged in user's email.
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}