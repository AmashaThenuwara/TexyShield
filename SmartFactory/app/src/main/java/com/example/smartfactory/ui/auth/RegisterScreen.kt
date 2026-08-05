/*
 * File: RegisterScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.auth

// ============================================================
// RegisterScreen.kt
// Smart Garment Factory — Industry 4.0
// ============================================================
// Premium glassmorphism register / sign-up screen.
// Matches reference image: dark teal gradient bg, floating
// card, Full Name / Email / Set Password fields, eye toggle,
// Sign Up gradient button, Google & Apple buttons, bottom link.
// ============================================================

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartfactory.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Entry animation
    var visible by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700),
        label = "alpha"
    )
    val slideAnim by animateFloatAsState(
        targetValue = if (visible) 0f else 50f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "slide"
    )

    // Pulsing glow
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    LaunchedEffect(Unit) { delay(80); visible = true }

    // ── Background ────────────────────────────────────────────────────────
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF060A10), Color(0xFF091A1A), Color(0xFF06101A))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // Glow orbs
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(listOf(TealMint.copy(alpha = glowAlpha), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(listOf(EmeraldGlow.copy(alpha = glowAlpha * 0.8f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .alpha(alphaAnim)
                .graphicsLayer { translationY = slideAnim }
        ) {
            // ── Back button ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F2020))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TealMint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Glassmorphism Card ────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(TealMint.copy(alpha = 0.35f), EmeraldGlow.copy(alpha = 0.15f), Color.Transparent)
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0C1E2B).copy(alpha = 0.95f), Color(0xFF081218).copy(alpha = 0.98f))
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(28.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Icon badge
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(listOf(TealMint.copy(alpha = 0.15f), Color(0xFF081820)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏭", fontSize = 30.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Sign Up",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Create an account to continue",
                            fontSize = 13.sp,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Full Name ─────────────────────────────────────
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Full Name", fontSize = 13.sp, color = MutedText, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("Alex Lee", color = MutedText.copy(0.5f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, null, tint = TealMint, modifier = Modifier.size(20.dp))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0A1A20),
                                    unfocusedContainerColor = Color(0xFF080F14),
                                    focusedBorderColor = TealMint,
                                    unfocusedBorderColor = BorderColor,
                                    cursorColor = TealMint
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ── Email ─────────────────────────────────────────
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Email", fontSize = 13.sp, color = MutedText, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("example@email.com", color = MutedText.copy(0.5f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, null, tint = TealMint, modifier = Modifier.size(20.dp))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0A1A20),
                                    unfocusedContainerColor = Color(0xFF080F14),
                                    focusedBorderColor = TealMint,
                                    unfocusedBorderColor = BorderColor,
                                    cursorColor = TealMint
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ── Set Password ──────────────────────────────────
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Set Password", fontSize = 13.sp, color = MutedText, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("••••••••", color = MutedText.copy(0.5f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, null, tint = TealMint, modifier = Modifier.size(20.dp))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = MutedText,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0A1A20),
                                    unfocusedContainerColor = Color(0xFF080F14),
                                    focusedBorderColor = TealMint,
                                    unfocusedBorderColor = BorderColor,
                                    cursorColor = TealMint
                                )
                            )
                        }

                        // Forgot password hint
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = { }) {
                                Text("Forgot Password?", color = TealMint, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ── Sign Up Button ────────────────────────────────
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = TealMint, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    isLoading = true
                                    errorMessage = null
                                    FirebaseAuth.getInstance()
                                        .createUserWithEmailAndPassword(email.trim(), password)
                                        .addOnSuccessListener {
                                            val uid = FirebaseAuth.getInstance().currentUser!!.uid
                                            val database = FirebaseDatabase.getInstance(
                                                "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"
                                            ).reference
                                            val userData = mapOf(
                                                "name" to name,
                                                "email" to email.trim(),
                                                "role" to "worker"
                                            )
                                            database.child("Users").child(uid).setValue(userData)
                                                .addOnSuccessListener { isLoading = false; onRegisterSuccess() }
                                                .addOnFailureListener { isLoading = false; errorMessage = it.message }
                                        }
                                        .addOnFailureListener { isLoading = false; errorMessage = it.message }
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(listOf(TealMint, EmeraldGlow)),
                                            RoundedCornerShape(26.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Sign Up",
                                        color = Color(0xFF060A10),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        // Error message
                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(errorMessage!!, color = ErrorRed, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ── Divider ───────────────────────────────────────
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                            Text("  Or  ", color = MutedText, fontSize = 12.sp)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google button
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("G", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TealMint)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Continue with Google", fontSize = 14.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Apple button
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("🍎", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Continue with Apple", fontSize = 14.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bottom link
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Already have an account? ", color = MutedText, fontSize = 13.sp)
                            Text(
                                "Log In",
                                color = TealMint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { onLoginClick() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}