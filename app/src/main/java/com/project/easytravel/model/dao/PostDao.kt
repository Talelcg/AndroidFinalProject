
package com.project.easytravel.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.easytravel.model.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<Post>)

    @Query("SELECT * FROM Post WHERE id = :postId")
    suspend fun getPostById(postId: String): Post?

    @Update
    suspend fun updatePost(post: Post)

    @Query("SELECT * FROM Post")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("DELETE FROM Post")
    suspend fun deleteAllPosts()
}

