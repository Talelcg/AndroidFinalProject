package com.project.easytravel.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.easytravel.base.MyApplication
import com.project.easytravel.model.Comment
import com.project.easytravel.model.Post
import com.project.easytravel.model.User

@Database(entities = [User::class, Post::class,Comment::class], version = 7)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
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