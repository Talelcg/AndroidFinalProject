package com.project.easytravel.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(Converters::class)
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val rating: Float = 0f,
    val likes: MutableList<String> = mutableListOf(),
    val comments: MutableList<String> = mutableListOf(),
    val userId: String = ""
)