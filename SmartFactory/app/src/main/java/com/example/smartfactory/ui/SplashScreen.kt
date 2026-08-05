/*
 * File: SplashScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ============================================================
// SplashScreen.kt
// Smart Garment Factory - Industry 4.0
// ============================================================
// Displays a premium branded splash screen for 2.5 seconds
// before routing the user to the Login screen.
// ============================================================

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    // Animation state — starts at 0, animates to 1
    var startAnim by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.7f,
        animationSpec = tween(durationMillis = 1200),
        label = "scale"
    )

    // Trigger animation then navigate away
    LaunchedEffect(Unit) {
        startAnim = true
        delay(2500) // Show splash for 2.5 seconds
        onSplashFinished()
    }

    // Dark gradient background: charcoal black -> deep dark teal hint
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            com.example.smartfactory.ui.theme.DeepDarkBg,
            Color(0xFF0C1014)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            // App Icon with glowing emerald background
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                com.example.smartfactory.ui.theme.TealMint.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    com.example.smartfactory.ui.theme.TealMint.copy(alpha = 0.18f),
                                    Color(0xFF081A1A)
                                )
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛡️", fontSize = 52.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Text(
                text = "TexyShield",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.smartfactory.ui.theme.TealMint,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Industry 4.0 Safety & Monitoring",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = com.example.smartfactory.ui.theme.MutedText
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "POWERED BY AI & BLOCKCHAIN",
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                color = com.example.smartfactory.ui.theme.MutedText.copy(alpha = 0.5f)
            )
        }
    }
}
