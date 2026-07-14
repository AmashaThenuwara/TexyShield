package com.example.smartfactory.ui.worker.attendance


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

import androidx.core.content.ContextCompat

import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

import java.util.concurrent.Executors



@Composable
fun QRScanner(

    onQRScanned:(String)->Unit

){


    val context = LocalContext.current


    var hasPermission by remember {

        mutableStateOf(

            ContextCompat.checkSelfPermission(

                context,

                Manifest.permission.CAMERA

            ) == PackageManager.PERMISSION_GRANTED

        )

    }



    val permissionLauncher = rememberLauncherForActivityResult(

        ActivityResultContracts.RequestPermission()

    ){

        hasPermission = it

    }



    LaunchedEffect(Unit){

        if(!hasPermission){

            permissionLauncher.launch(

                Manifest.permission.CAMERA

            )

        }

    }



    if(hasPermission){


        AndroidView(

            factory = {


                val previewView = PreviewView(context)


                val cameraProviderFuture =

                    ProcessCameraProvider.getInstance(context)



                cameraProviderFuture.addListener({


                    val cameraProvider =

                        cameraProviderFuture.get()



                    val preview = Preview.Builder()

                        .build()



                    preview.setSurfaceProvider(

                        previewView.surfaceProvider

                    )



                    val scanner =

                        BarcodeScanning.getClient()



                    val analysis = ImageAnalysis.Builder()

                        .build()



                    analysis.setAnalyzer(

                        Executors.newSingleThreadExecutor()

                    ){imageProxy ->



                        val mediaImage =

                            imageProxy.image



                        if(mediaImage != null){



                            val image = InputImage.fromMediaImage(

                                mediaImage,

                                imageProxy.imageInfo.rotationDegrees

                            )



                            scanner.process(image)

                                .addOnSuccessListener {barcodes->



                                    for(barcode in barcodes){


                                        barcode.rawValue?.let{


                                            onQRScanned(it)


                                        }


                                    }


                                }



                                .addOnCompleteListener{


                                    imageProxy.close()

                                }



                        }

                        else{


                            imageProxy.close()


                        }



                    }




                    cameraProvider.unbindAll()



                    cameraProvider.bindToLifecycle(

                        context as androidx.lifecycle.LifecycleOwner,

                        CameraSelector.DEFAULT_BACK_CAMERA,

                        preview,

                        analysis

                    )



                },ContextCompat.getMainExecutor(context))



                previewView


            }

        )



    }



}