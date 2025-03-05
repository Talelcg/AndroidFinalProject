package com.project.easytravel.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.easytravel.model.dao.PostDao
import com.project.easytravel.model.dao.UserDao

@Database(entities = [User::class,Post::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "posts_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}