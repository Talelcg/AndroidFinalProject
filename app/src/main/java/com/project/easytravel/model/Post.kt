

package com.project.easytravel.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(Converters::class)
data class Post(
    @PrimaryKey(autoGenerate = false) // Firebase uses string IDs
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val rating: Float = 0.0f,
    // Corrected floating-point literal (must be `0.0f` instead of `0f`)
//    val likes: String ="",
    val likes: MutableList<String> = mutableListOf(),
    val comments: List<Comment> = emptyList(),

    val userId: String = "",
    val place: String = "" // Assuming you added a place field
) {
    // Required no-argument constructor for Firebase
    constructor() : this("", "", "", "", 0.0f, mutableListOf(), mutableListOf(), "", "")
}