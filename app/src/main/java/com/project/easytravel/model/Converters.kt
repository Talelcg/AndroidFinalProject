package com.project.easytravel.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson() // Create a single Gson instance

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>()) // Ensure it never returns null
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        return if (json.isNullOrEmpty()) emptyList()
        else gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun fromCommentList(list: List<Comment>?): String {
        return gson.toJson(list ?: emptyList<String>()) // Ensure it never returns null
    }

    @TypeConverter
    fun toCommentList(json: String?): List<Comment> {
        return if (json.isNullOrEmpty()) emptyList()
        else gson.fromJson(json, object : TypeToken<List<Comment>>() {}.type)
    }
}