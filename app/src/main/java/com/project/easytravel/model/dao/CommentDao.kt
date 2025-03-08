package com.project.easytravel.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*

import com.project.easytravel.model.Comment
import com.project.easytravel.model.User

@Dao
interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): LiveData<List<Comment>>

    @Delete
    suspend fun deleteComment(comment: Comment)

    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteAllCommentsForPost(postId: String)

   @Query("SELECT * FROM User")
   fun getAllUsers(): List<User>

   @Insert(onConflict = OnConflictStrategy.REPLACE)
   fun insertUser(user: User)
    }

