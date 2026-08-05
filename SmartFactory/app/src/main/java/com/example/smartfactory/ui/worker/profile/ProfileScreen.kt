/*
 * File: ProfileScreen.kt
 * Includes UI components and functionality for the Smart Factory Android app.
 */
package com.example.smartfactory.ui.worker.profile

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartfactory.firebase.FirebaseAuthManager
import com.example.smartfactory.ui.theme.DeepDarkBg
import com.example.smartfactory.ui.theme.SurfaceDark
import com.example.smartfactory.ui.theme.BorderDark
import com.example.smartfactory.ui.theme.TealMint
import com.example.smartfactory.ui.theme.MutedText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUserEmail = FirebaseAuthManager.getCurrentUserEmail() ?: "Unknown Worker"
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: "UNKNOWN_UID"

    var profileBase64 by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Pulsing animation for glow
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val qrDataString = "WORKER_QR|$currentUserUid|$currentUserEmail"
    val qrBitmap = remember(qrDataString) { generateQrCode(qrDataString) }

    LaunchedEffect(currentUserUid) {
        if (currentUserUid != "UNKNOWN_UID") {
            val db = FirebaseDatabase.getInstance("https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("ProfilePhotos").child(currentUserUid)
            db.get().addOnSuccessListener { snapshot ->
                val base64 = snapshot.getValue(String::class.java)
                if (base64 != null) profileBase64 = base64
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && currentUserUid != "UNKNOWN_UID") {
            isUploading = true
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                if (originalBitmap != null) {
                    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 200, 200, true)
                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    val db = FirebaseDatabase.getInstance("https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app")
                        .reference.child("ProfilePhotos").child(currentUserUid)
                    db.setValue(base64String).addOnSuccessListener {
                        profileBase64 = base64String
                        isUploading = false
                    }.addOnFailureListener { isUploading = false }
                } else isUploading = false
            } catch (e: Exception) { isUploading = false }
        }
    }

    val profileBitmap = remember(profileBase64) {
        profileBase64?.let {
            try {
                val decodedBytes = Base64.decode(it, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) { null }
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF050C10), DeepDarkBg, Color(0xFF071118))
    )
    val tealColor = Color(0xFF14B8A6)
    val glowColor = tealColor.copy(alpha = glowAlpha * 0.6f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Background glow orbs
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-60).dp, y = (-30).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(tealColor.copy(alpha = 0.07f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF6366F1).copy(alpha = 0.06f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // ── Header ──────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 28.dp)
                        .background(
                            Brush.verticalGradient(listOf(tealColor, Color(0xFF6366F1))),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WORKER IDENTITY",
                        color = tealColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Profile Card",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Premium Glowing Avatar ────────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(166.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(tealColor.copy(alpha = glowAlpha * 0.5f), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                // Gradient ring border
                Box(
                    modifier = Modifier
                        .size(154.dp)
                        .background(
                            Brush.sweepGradient(
                                listOf(tealColor, Color(0xFF6366F1), tealColor)
                            ),
                            CircleShape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(148.dp)
                            .align(Alignment.Center)
                            .background(Color(0xFF0F1923), CircleShape)
                            .clip(CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = tealColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                        if (isUploading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = tealColor, strokeWidth = 3.dp)
                            }
                        }
                    }
                }

                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .background(tealColor, CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = currentUserEmail.substringBefore("@").uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = currentUserEmail,
                color = MutedText,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Premium ID Card ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0D1F2D), Color(0xFF0A1520), Color(0xFF091825)),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(tealColor.copy(alpha = 0.8f), Color(0xFF6366F1).copy(alpha = 0.4f), tealColor.copy(alpha = 0.2f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Card header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SMART FACTORY",
                                color = tealColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Industry 4.0",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(tealColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, tealColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = tealColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = BorderDark
                    )

                    // Worker details grid
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileInfoRow(icon = Icons.Default.AccountBox, label = "WORKER ID", value = currentUserUid.take(12) + "...")
                        ProfileInfoRow(icon = Icons.Default.Email, label = "EMAIL", value = currentUserEmail)
                        ProfileInfoRow(icon = Icons.Default.Build, label = "DEPARTMENT", value = "Production Floor")
                        ProfileInfoRow(icon = Icons.Default.Security, label = "ACCESS LEVEL", value = "Worker")
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        color = BorderDark
                    )

                    // QR Section
                    Text(
                        text = "ATTENDANCE QR CODE",
                        color = MutedText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (qrBitmap != null) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(tealColor.copy(alpha = glowAlpha * 0.2f), Color.Transparent),
                                        radius = 200f
                                    )
                                )
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Worker QR Code",
                                    modifier = Modifier.size(180.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan to log attendance · Immutable blockchain record",
                        color = MutedText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Download PDF Button
                    Button(
                        onClick = {
                            scope.launch {
                                val success = saveIDCardAsPdf(context, profileBase64, qrBitmap, currentUserEmail, currentUserUid)
                                if (success) Toast.makeText(context, "ID Card PDF saved to Downloads!", Toast.LENGTH_LONG).show()
                                else Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = tealColor),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download PDF Card", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Action Buttons ────────────────────────────────────────────
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Dashboard", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    FirebaseAuthManager.logout()
                    onLogoutClick()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A0A0A)
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C1823), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color(0xFF14B8A6).copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF14B8A6), modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = MutedText, fontSize = 10.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Helper Functions ────────────────────────────────────────────────────────

fun generateQrCode(text: String): Bitmap? {
    return try {
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 400, 400)
    } catch (e: Exception) { null }
}

suspend fun saveIDCardAsPdf(
    context: Context,
    profileBase64: String?,
    qrBitmap: Bitmap?,
    email: String,
    uid: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // 1. Page Title & Header
        val pageTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0C1014")
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("SMART FACTORY SYSTEMS", 170f, 70f, pageTitlePaint)
        
        val pageSubtitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#5A6D80")
            textSize = 12f
            isAntiAlias = true
        }
        canvas.drawText("Official Employee Access & Identification Document", 150f, 90f, pageSubtitlePaint)
        
        // Page Divider Line
        val pageDividerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#D9E2EC")
            strokeWidth = 2f
        }
        canvas.drawLine(50f, 110f, 545f, 110f, pageDividerPaint)

        // 2. Define Card Coordinates & Shapes (Centered CR-80 style landscape card)
        val cardLeft = 97.5f
        val cardTop = 150f
        val cardRight = cardLeft + 400f
        val cardBottom = cardTop + 260f
        
        val bgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0C1014") // Sleek dark surface
            style = Paint.Style.FILL
        }
        
        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#00E676") // TealMint glow border
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        
        // Draw Card Container
        canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 16f, 16f, bgPaint)
        canvas.drawRoundRect(cardLeft, cardTop, cardRight, cardBottom, 16f, 16f, borderPaint)

        // 3. Card Header
        val cardHeaderPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("SMART FACTORY SYSTEMS", cardLeft + 20f, cardTop + 35f, cardHeaderPaint)
        
        val cardSubheaderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#00E676")
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("SECURE IDENTITY ACCESS PASS", cardLeft + 20f, cardTop + 50f, cardSubheaderPaint)
        
        val cardDividerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1A2A4A")
            strokeWidth = 2.5f
        }
        canvas.drawLine(cardLeft + 20f, cardTop + 62f, cardRight - 20f, cardTop + 62f, cardDividerPaint)

        // 4. Card Body: Worker Details (Left Column)
        val labelPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#8E9AA8") // Clean gray label
            textSize = 9f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        val startX = cardLeft + 20f
        var startY = cardTop + 85f
        
        // Employee Name
        val displayName = email.substringBefore("@").uppercase()
        canvas.drawText("EMPLOYEE NAME", startX, startY, labelPaint)
        canvas.drawText(displayName, startX, startY + 15f, valuePaint)
        
        // Email
        startY += 40f
        canvas.drawText("CONTACT EMAIL", startX, startY, labelPaint)
        canvas.drawText(email, startX, startY + 15f, valuePaint)
        
        // UID
        startY += 40f
        canvas.drawText("SYSTEM ACCESS UID", startX, startY, labelPaint)
        canvas.drawText(uid.take(24) + if (uid.length > 24) "..." else "", startX, startY + 15f, valuePaint)
        
        // Privilege Level
        startY += 40f
        canvas.drawText("SECURITY PRIVILEGE", startX, startY, labelPaint)
        canvas.drawText("VERIFIED FACTORY OPERATOR", startX, startY + 15f, valuePaint)

        // 5. Card Body: Images (Right Column - Stacked Profile & QR Code)
        val framePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1A2A4A")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        
        val rightX = cardLeft + 295f
        val photoY = cardTop + 80f
        val photoSize = 75f
        
        // Decode Profile Photo directly on background thread with software config option
        val decodedProfileBitmap = if (!profileBase64.isNullOrEmpty()) {
            try {
                val decodedBytes = Base64.decode(profileBase64, Base64.DEFAULT)
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, opts)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        decodedProfileBitmap?.let {
            val scaledProfile = Bitmap.createScaledBitmap(it, photoSize.toInt(), photoSize.toInt(), true)
            canvas.drawBitmap(scaledProfile, rightX, photoY, Paint().apply { isFilterBitmap = true })
            canvas.drawRect(rightX, photoY, rightX + photoSize, photoY + photoSize, framePaint)
        } ?: run {
            // Draw vector avatar placeholder if no profile picture is selected
            val placeholderBg = Paint().apply {
                color = android.graphics.Color.parseColor("#141D26")
                style = Paint.Style.FILL
            }
            canvas.drawRect(rightX, photoY, rightX + photoSize, photoY + photoSize, placeholderBg)
            canvas.drawRect(rightX, photoY, rightX + photoSize, photoY + photoSize, framePaint)
            
            val avatarPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#3A4D5F")
                isAntiAlias = true
            }
            canvas.drawCircle(rightX + 37.5f, photoY + 28f, 13f, avatarPaint) // Head
            canvas.drawCircle(rightX + 37.5f, photoY + 68f, 22f, avatarPaint) // Shoulders
        }
        
        // QR Code Pass
        val qrY = cardTop + 165f
        val qrSize = 75f
        qrBitmap?.let {
            val scaledQr = Bitmap.createScaledBitmap(it, qrSize.toInt(), qrSize.toInt(), false)
            canvas.drawBitmap(scaledQr, rightX, qrY, null)
            canvas.drawRect(rightX, qrY, rightX + qrSize, qrY + qrSize, framePaint)
        }

        // 6. Page Level Setup & Instructions (Bottom)
        val instrTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0C1014")
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Instructions for Card Setup:", 97.5f, 480f, instrTitlePaint)
        
        val instrTextPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334E68")
            textSize = 11f
            isAntiAlias = true
        }
        canvas.drawText("1. Print this document on high-quality paper or cardstock (100% scale).", 97.5f, 510f, instrTextPaint)
        canvas.drawText("2. Cut out the card carefully along the green glowing border lines.", 97.5f, 535f, instrTextPaint)
        canvas.drawText("3. Laminate the printed card for physical durability at factory gates.", 97.5f, 560f, instrTextPaint)
        canvas.drawText("4. Present the QR code on the card to the ESP32-CAM for automated attendance.", 97.5f, 585f, instrTextPaint)
        
        // Page Footer
        val footerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#9AA8B6")
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText("Generated automatically by Smart Factory Monitoring Node. Cryptographically Secured.", 110f, 780f, footerPaint)

        pdfDocument.finishPage(page)
        val fileName = "WorkerID_$uid.pdf"
        var outputStream: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) outputStream = resolver.openOutputStream(uri)
        } else {
            @Suppress("DEPRECATION")
            val targetFile = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            outputStream = java.io.FileOutputStream(targetFile)
        }
        outputStream?.use { pdfDocument.writeTo(it) }
        pdfDocument.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
