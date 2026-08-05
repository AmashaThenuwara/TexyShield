/*
 * File: LandingScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui

// ============================================================
// LandingScreen.kt
// Smart Garment Factory — Industry 4.0
// ============================================================
// Premium animated landing page shown after Splash.
// Routes: "Create Account" → register | "Login" → login
// Design: Dark teal glassmorphism matching reference image
// ============================================================

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartfactory.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LandingScreen(
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    // Entry animations
    var visible by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "alpha"
    )
    val slideAnim by animateFloatAsState(
        targetValue = if (visible) 0f else 60f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "slide"
    )

    // Subtle floating / pulsing glow
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    // Background
    val bg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF060A10),
            Color(0xFF091A1A),
            Color(0xFF060A10)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        // Ambient glow blob — top right
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .graphicsLayer { scaleX = glowScale; scaleY = glowScale }
                .background(
                    Brush.radialGradient(
                        listOf(
                            TealMint.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        // Ambient glow blob — bottom left
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .graphicsLayer { scaleX = glowScale; scaleY = glowScale }
                .background(
                    Brush.radialGradient(
                        listOf(
                            EmeraldGlow.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .alpha(alphaAnim)
                .graphicsLayer { translationY = slideAnim },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Hero Icon — Shield with Factory glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                TealMint.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F2A2A), Color(0xFF081818))
                            )
                        )
                ) {
                    Text("🛡️", fontSize = 52.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name & tagline
            Text(
                text = "TexyShield",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Industry 4.0 Safety & Monitoring",
                fontSize = 14.sp,
                color = TealMint,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hero tagline
            Text(
                text = "Work Smarter,\nLive Safer.",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "🛡 Smart textile factory protection\n" +
                        "⛓ Blockchain ledger • 🤖 AI hazard alerts • 📡 IoT",
                fontSize = 13.sp,
                color = MutedText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Feature Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureChip("🤖 AI Alerts", modifier = Modifier.weight(1f))
                FeatureChip("📡 Live IoT", modifier = Modifier.weight(1f))
                FeatureChip("⛓ Blockchain", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // CTA Buttons

            // Create Account — Gradient primary button
            Button(
                onClick = onCreateAccountClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(27.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(TealMint, EmeraldGlow, Color(0xFF00B8A0))
                            ),
                            shape = RoundedCornerShape(27.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Create Account",
                            color = Color(0xFF060A10),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Login — Outlined secondary button
            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(TealMint.copy(alpha = 0.6f), EmeraldGlow.copy(alpha = 0.6f)))
                ),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TealMint)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔑", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Login to Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                Text(
                    "  OR  ",
                    color = MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google sign-in style button
            OutlinedButton(
                onClick = { /* Simulated */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("G", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealMint)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Continue with Google", fontSize = 14.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Text(
                text = "POWERED BY AI & BLOCKCHAIN",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = MutedText.copy(alpha = 0.45f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureChip(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F2020))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TealMint,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
