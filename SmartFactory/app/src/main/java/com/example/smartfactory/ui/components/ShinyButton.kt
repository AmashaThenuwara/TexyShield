/*
 * File: ShinyButton.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.components

// ============================================================
// ShinyButton.kt
// Smart Garment Factory - Industry 4.0
// ============================================================
// A premium gradient button (Indigo → Neon Cyan) used
// consistently throughout the entire app for shiny interactive UI.
// ============================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.EmeraldGlow

@Composable
fun ShinyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mint -> Emerald linear gradient
    val gradient = Brush.linearGradient(
        colors = listOf(
            TealMint,
            EmeraldGlow
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(gradient)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Subtle top shine overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 1.sp
        )
    }
}
