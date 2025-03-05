//package com.project.easytravel.model.dao

//import androidx.lifecycle.LiveData
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import androidx.room.Update
//import com.project.easytravel.model.Post
//
//@Dao
//interface PostDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertPost(post: Post)
//
//    @Query("SELECT * FROM Post WHERE id = :postId LIMIT 1")
//    fun getPostById(postId: String): Post?
//
//    @Update
//    fun updatePost(post: Post)
//    abstract fun getAllPosts(): LiveData<List<Post>>
//}
package com.project.easytravel.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.easytravel.model.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM Post WHERE id = :postId")
    suspend fun getPostById(postId: String): Post?

    @Query("SELECT * FROM Post")
    fun getAllPosts(): LiveData<List<Post>> // ✅ Add @Query annotation

    @Query("DELETE FROM Post")
    suspend fun deleteAllPosts()
}
