package com.project.easytravel.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.auth.FirebaseAuth
import com.project.easytravel.base.Constants
import com.project.easytravel.base.EmptyCallback
import com.project.easytravel.base.UsersCallback

class FirebaseModel {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        database.firestoreSettings = settings
    }


    fun getAllUsers(callback: UsersCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val users: MutableList<User> = mutableListOf()
                    for (document in task.result!!) {
                        users.add(User.fromJSON(document.data))
                    }
                    callback(users)
                } else {
                    Log.e("FirebaseModel", "Error getting users: ${task.exception?.message}")
                    callback(listOf())
                }
            }
    }


    fun add(user: User, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.id)
            .set(user.json)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback()
                } else {
                    Log.e("FirebaseModel", "Error adding user: ${task.exception?.message}")
                    callback()
                }
            }
    }

}
