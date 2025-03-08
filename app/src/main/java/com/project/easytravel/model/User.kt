package com.project.easytravel.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User (
    @PrimaryKey val id: String = "",
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    var profileimage: String = ""
) {
    // 🔹 קונסטרקטור ריק נחוץ ל-Firebase
    constructor() : this("", "", "", "", "")

    companion object {
        private const val ID_KEY = "id"
        private const val EMAIL_KEY = "email"
        private const val NAME_KEY = "name"
        private const val PROFILEIMAGE_URL_KEY = "profileimage"
        private const val BIO_KEY = "bio"

        fun fromJSON(json: Map<String, Any>): User {
            val id = json[ID_KEY] as? String ?: ""
            val email = json[EMAIL_KEY] as? String ?: ""
            val name = json[NAME_KEY] as? String ?: ""
            val bio = json[BIO_KEY] as? String ?: ""
            val profileimage = json[PROFILEIMAGE_URL_KEY] as? String ?: ""

            return User(id, email, name, bio, profileimage)
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            EMAIL_KEY to email,
            NAME_KEY to name,
            BIO_KEY to bio,
            PROFILEIMAGE_URL_KEY to profileimage
        )
}
