package com.example.smartfactory.ui.worker.attendance


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.smartfactory.firebase.AttendanceManager



@Composable
fun QRAttendanceScreen(){



    var message by remember {

        mutableStateOf(
            "Scan Factory QR"
        )

    }



    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)

    ){



        Text(

            text="📷 QR Attendance",

            style=MaterialTheme.typography.headlineMedium

        )



        Spacer(

            modifier=Modifier.height(20.dp)

        )



        QRScanner(

            onQRScanned = {qrValue->



                if(qrValue=="SMART_FACTORY"){



                    AttendanceManager.saveAttendance(


                        onSuccess={


                            message =
                                "✅ Attendance Marked"


                        },


                        onFailure={

                            message=it

                        }


                    )


                }

                else{


                    message =
                        "❌ Invalid Factory QR"


                }



            }

        )



        Spacer(

            modifier=Modifier.height(20.dp)

        )



        Text(message)



    }



}