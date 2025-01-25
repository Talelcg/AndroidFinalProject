package com.project.easytravel.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.easytravel.base.MyApplication
import com.project.easytravel.model.User

@Database(entities = [User::class], version = 2)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun userDao(): UserDao
}
object AppLocalDb {
    val database: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.context ?: throw IllegalStateException("Application context is missing")
        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "dbFileName.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}