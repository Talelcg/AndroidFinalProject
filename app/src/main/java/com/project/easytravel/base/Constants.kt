package com.project.easytravel.base


import com.project.easytravel.model.User

typealias UsersCallback = (List<User>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object COLLECTIONS {
        const val USERS = "users"
    }
}