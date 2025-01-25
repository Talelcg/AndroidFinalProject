package com.project.easytravel.model



import android.graphics.Bitmap
import com.idz.colman24class2.model.CloudinaryModel
import com.project.easytravel.base.EmptyCallback
import com.project.easytravel.model.dao.AppLocalDb
import com.project.easytravel.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()
    private val cloudinaryModel = CloudinaryModel()

    private val firebaseModel = FirebaseModel()

    companion object {
        val shared = Model()
    }


    fun add(user: User, callback: EmptyCallback) {
        firebaseModel.add(user, callback)

    }
    fun saveUserToRoom(user: User) {
        executor.execute {
            database.userDao().insertUser(user)
        }
    }
    private fun uploadImageToCloudinary(image: Bitmap, name: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        cloudinaryModel.uploadBitmap(
            bitmap = image,
            onSuccess = onSuccess,
            onError = onError
        )
    }

}