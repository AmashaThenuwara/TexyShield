package com.example.smartfactory.ui.worker.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smartfactory.firebase.FirebaseAuthManager
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUserEmail = FirebaseAuthManager.getCurrentUserEmail() ?: "Unknown User"
    val currentUserUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    
    var profileBase64 by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Fetch existing profile photo on load
    LaunchedEffect(currentUserUid) {
        if (currentUserUid != null) {
            val db = FirebaseDatabase.getInstance("https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("ProfilePhotos").child(currentUserUid)
            
            db.get().addOnSuccessListener { snapshot ->
                val base64 = snapshot.getValue(String::class.java)
                if (base64 != null) {
                    profileBase64 = base64
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && currentUserUid != null) {
            isUploading = true
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                
                // Scale down to save database space (approx 200x200)
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 200, 200, true)
                
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                
                // Save to Firebase
                val db = FirebaseDatabase.getInstance("https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .reference.child("ProfilePhotos").child(currentUserUid)
                
                db.setValue(base64String).addOnSuccessListener {
                    profileBase64 = base64String
                    isUploading = false
                }.addOnFailureListener {
                    isUploading = false
                }
            } catch (e: Exception) {
                isUploading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            // Fix: Decouple bitmap processing from UI emission to avoid try-catch around composables
            val bitmap = remember(profileBase64) {
                profileBase64?.let {
                    try {
                        val decodedBytes = Base64.decode(it, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Icon",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (isUploading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Tap to change photo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Worker Profile",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Logged in as:", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = currentUserEmail, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                FirebaseAuthManager.logout()
                onLogoutClick()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}
