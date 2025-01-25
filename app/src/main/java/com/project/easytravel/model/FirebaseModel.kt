package com.project.easytravel.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.project.easytravel.base.Constants
import com.project.easytravel.base.EmptyCallback

class FirebaseModel {

    private val database: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        database.firestoreSettings = settings
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

    fun update(user: User, callback: EmptyCallback) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(user.id)
            .update(user.json)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback()
                } else {
                    Log.e("FirebaseModel", "Error updating user: ${task.exception?.message}")
                    callback()
                }
            }
    }

    fun updateUserDetails(userId: String, updatedName: String, updatedBio: String, callback: (Boolean) -> Unit) {
        val userMap: Map<String, Any> = hashMapOf(
            "name" to updatedName,
            "bio" to updatedBio

        )

        database.collection("users").document(userId)
            .update(userMap)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                callback(false)
                Log.e("Firestore", "Error updating user details", exception)
            }
    }
    fun updateUserProfileImage(userId: String, profileImageUrl: String, callback: (Boolean) -> Unit) {
        val updates = mapOf("profileimage" to profileImageUrl)
        database.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
        database.collection(Constants.COLLECTIONS.USERS)
            .document(userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val user = User.fromJSON(document.data ?: mapOf())
                        callback(user)
                    } else {
                        callback(null)
                    }
                } else {
                    Log.e("FirebaseModel", "Error getting user: ${task.exception?.message}")
                    callback(null)
                }
            }
    }
}
