package com.project.easytravel.model



import com.project.easytravel.model.dao.AppLocalDb
import com.project.easytravel.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class Model private constructor() {

    private val database: AppLocalDbRepository = AppLocalDb.database
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        val shared = Model()
    }


    fun saveUserToRoom(user: User) {
        executor.execute {
            database.userDao().insertUser(user)
        }
    }

}