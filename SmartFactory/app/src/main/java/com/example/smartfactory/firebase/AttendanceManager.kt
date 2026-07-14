package com.example.smartfactory.firebase


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase



object AttendanceManager {



    private val database = FirebaseDatabase.getInstance(

        "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"

    )

        .reference

        .child("Attendance")





    fun saveAttendance(

        onSuccess:()->Unit,

        onFailure:(String)->Unit

    ){



        val user = FirebaseAuth.getInstance().currentUser



        if(user == null){

            onFailure("User not logged in")

            return

        }




        val uid = user.uid



        val time = System.currentTimeMillis()



        val attendance = mapOf(

            "userId" to uid,

            "date" to "2026-07-14",

            "time" to time.toString(),

            "status" to "Present"

        )



        database

            .child(uid)

            .push()

            .setValue(attendance)



            .addOnSuccessListener {

                onSuccess()

            }



            .addOnFailureListener {


                onFailure(

                    it.message ?: "Error"

                )


            }



    }


}