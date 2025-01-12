package com.project.easytravel.model



import android.os.Looper
import androidx.core.os.HandlerCompat
import com.project.easytravel.base.EmptyCallback
import com.project.easytravel.base.UsersCallback
import com.project.easytravel.model.dao.AppLocalDb
import com.project.easytravel.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

interface GetAllUsersListener {
    fun onCompletion(users: List<User>)
}

class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    private val firebaseModel = FirebaseModel()

    companion object {
        val shared = Model()
    }

    fun getAllUsers(callback: UsersCallback) {
        firebaseModel.getAllUsers(callback)

    }

    fun add(user: User, callback: EmptyCallback) {
        firebaseModel.add(user, callback)

    }
}