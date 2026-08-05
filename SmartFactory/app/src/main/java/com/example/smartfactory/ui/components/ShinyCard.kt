/*
 * File: ShinyCard.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.EmeraldGlow
import com.example.smartfactory.ui.theme.DeepDarkSurface

/**
 * A sleek, dark theme card with a glowing emerald border.
 */
@Composable
fun ShinyCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradientBorder = Brush.linearGradient(
        colors = listOf(EmeraldGlow, TealMint)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepDarkSurface),
        border = BorderStroke(1.dp, gradientBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}
