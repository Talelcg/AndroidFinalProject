package com.project.easytravel.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(Converters::class)
data class Post(
    @PrimaryKey val id: String,
    val title: String = "",
    val place: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val rating: Float = 0f,
    val likes: MutableList<String> = mutableListOf(),
    val comments: MutableList<String> = mutableListOf(),
    val userId: String = ""
)