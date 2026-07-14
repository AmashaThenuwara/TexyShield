package com.example.smartfactory.firebase


import com.example.smartfactory.model.User
import com.google.firebase.database.*


object UserManager {


    private val database = FirebaseDatabase.getInstance(

        "https://smartfactory-8dbd8-default-rtdb.asia-southeast1.firebasedatabase.app"

    )
        .reference
        .child("Users")



    fun getUserByEmail(

        email: String,

        onResult: (User?) -> Unit

    ) {


        database.get()

            .addOnSuccessListener { snapshot ->


                var foundUser: User? = null


                for(child in snapshot.children){


                    val user = child.getValue(User::class.java)


                    if(user?.email == email){


                        foundUser = user

                        break

                    }

                }


                onResult(foundUser)


            }


            .addOnFailureListener {


                onResult(null)


            }


    }


}