/*
 * File: LoginScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.auth

// ============================================================
// LoginScreen.kt
// Smart Garment Factory — Industry 4.0
// ============================================================
// Premium glassmorphism login screen.
// Matches reference image: dark teal gradient bg, floating
// card panel, proper input fields, Google button, teal CTA.
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
import com.example.smartfactory.firebase.FirebaseAuthManager
import com.example.smartfactory.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

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
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
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
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(listOf(TealMint.copy(alpha = glowAlpha), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 60.dp)
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
            // ── Top back button ───────────────────────────────────────────
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

            Spacer(modifier = Modifier.height(24.dp))

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
                            Text("🔑", fontSize = 30.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Welcome Back",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Login to your account to continue",
                            fontSize = 13.sp,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Email field ───────────────────────────────────
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Email", fontSize = 13.sp, color = MutedText, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("worker@factory.com", color = MutedText.copy(0.5f), fontSize = 14.sp) },
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Password field ────────────────────────────────
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Password", fontSize = 13.sp, color = MutedText, fontWeight = FontWeight.Medium)
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
                                            contentDescription = "Toggle password",
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

                        // Forgot password
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = { }) {
                                Text("Forgot Password?", color = TealMint, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ── Login Button ──────────────────────────────────
                        if (loading) {
                            Box(modifier = Modifier.fillMaxWidth().height(52.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = TealMint, modifier = Modifier.size(32.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    loading = true
                                    errorMessage = null
                                    FirebaseAuthManager.login(
                                        email.trim(), password,
                                        onSuccess = { loading = false; onLoginSuccess(email.trim()) },
                                        onFailure = { loading = false; errorMessage = it }
                                    )
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
                                        "Login",
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

                        Spacer(modifier = Modifier.height(24.dp))

                        // ── Divider ───────────────────────────────────────
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                            Text("  Or  ", color = MutedText, fontSize = 12.sp)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

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

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bottom link
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Don't have an account? ", color = MutedText, fontSize = 13.sp)
                            Text(
                                "Sign Up",
                                color = TealMint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable { onRegisterClick() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}