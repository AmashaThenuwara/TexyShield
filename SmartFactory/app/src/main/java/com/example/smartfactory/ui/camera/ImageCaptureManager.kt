package com.example.smartfactory.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

object ImageCaptureManager {
    fun takePhoto(
        imageCapture: ImageCapture,
        context: Context,
        executor: Executor,
        onImageCaptured: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        
                        // Handle rotation
                        val matrix = Matrix()
                        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
                        
                        val rotatedBitmap = Bitmap.createBitmap(
                            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                        )
                        
                        val outputStream = ByteArrayOutputStream()
                        // Compress the image to save bandwidth (JPEG, 80% quality)
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val byteArray = outputStream.toByteArray()
                        
                        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                        
                        image.close()
                        
                        onImageCaptured(base64String)
                    } catch (e: Exception) {
                        image.close()
                        onError(e)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
}
