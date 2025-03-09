package com.project.easytravel.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
    fun createPost(post: Post, callback: (Boolean) -> Unit) {
        val postRef = database.collection("posts").document() // Firebase auto-generates ID
        val postId = postRef.id  // Get the auto-generated ID

        val postMap = post.copy(id = postId)  // Assign Firebase ID

        postRef.set(postMap)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                callback(false)
                Log.e("FirebaseModel", "Error creating post: ${exception.message}")
            }
    }
    fun getAllPosts(callback: (List<Post>) -> Unit) {
        database.collection("posts").get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                callback(posts)
            }
    }

    fun updatePost(post: Post) {
        database.collection("posts").document(post.id).set(post)
    }
    fun getPostById(postId: String, callback: (Post?) -> Unit) {
        database.collection("posts")
            .document(postId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val post = document.toObject(Post::class.java)
                        callback(post)
                    } else {
                        callback(null)
                    }
                } else {
                    Log.e("FirebaseModel", "Error getting post: ${task.exception?.message}")
                    callback(null)
                }
            }
    }

    fun addComment(comment: Comment, postId: String) {
        val commentId = comment.id

        // עדכון ה-comments של הפוסט
        val postRef = database.collection("posts").document(postId)
        postRef.update("comments", FieldValue.arrayUnion(commentId))
            .addOnSuccessListener {
                Log.d("FirebaseModel", "Comment ID added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseModel", "Error adding comment ID: ${e.message}")
            }

        // הוספת התגובה גם לקולקציה של תגובות
        database.collection("comments")
            .document(comment.id)
            .set(comment)
            .addOnSuccessListener {
                Log.d("FirebaseModel", "Comment added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseModel", "Error adding comment: ${e.message}")
            }
    }




    // Get comments for a specific post
    fun getCommentsForPost(postId: String, callback: (List<Comment>) -> Unit) {
        database.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                val comments = result.toObjects(Comment::class.java)
                callback(comments)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseModel", "Error fetching comments: ${e.message}")
                callback(emptyList()) // Return an empty list if fetch fails
            }
    }
    fun getAllUsers(callback: (List<User>) -> Unit) {
        val usersRef = FirebaseFirestore.getInstance().collection("users")
        usersRef.get().addOnSuccessListener { result ->
            val users = result.mapNotNull { it.toObject(User::class.java) }
            callback(users)
        }.addOnFailureListener {
            callback(emptyList()) // במקרה של כשלון, מחזירים רשימה ריקה
        }
    }

}
