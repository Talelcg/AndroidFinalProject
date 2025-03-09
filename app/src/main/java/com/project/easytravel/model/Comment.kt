package com.project.easytravel.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import java.util.UUID

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey @PropertyName("id") val id: String = UUID.randomUUID().toString(), // Unique ID for each comment
    @PropertyName("postId") val postId: String,  // ID of the post the comment belongs to
    @PropertyName("userId") val userId: String,  // ID of the user who made the comment
    @PropertyName("text") val text: String,      // Comment text
    @PropertyName("timestamp") val timestamp: Long = System.currentTimeMillis() // Timestamp for sorting
){
    constructor() : this("", "", "", "", 0) // בנאי ריק
}