package com.project.easytravel.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.easytravel.model.AppDatabase
import com.project.easytravel.model.Comment
import com.project.easytravel.model.FirebaseModel
import com.project.easytravel.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val postDao = AppDatabase.getDatabase(application).postDao()
    private val commentDao = AppDatabase.getDatabase(application).commentDao()
    private val firebaseModel = FirebaseModel()

    val allPosts: MutableLiveData<List<Post>> = MutableLiveData()

    fun loadPosts() {
        viewModelScope.launch {
            firebaseModel.getAllPosts { firebasePosts ->
                viewModelScope.launch(Dispatchers.IO) {
                    postDao.insertAll(firebasePosts)
                    allPosts.postValue(firebasePosts)
                }
            }
        }
    }

    fun toggleLike(post: Post) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updatedLikes = post.likes.toMutableList()

        if (updatedLikes.contains(userId)) {
            updatedLikes.remove(userId)
        } else {
            updatedLikes.add(userId)
        }

        val updatedPost = post.copy(likes = updatedLikes)

        viewModelScope.launch(Dispatchers.IO) {
            postDao.updatePost(updatedPost)
            firebaseModel.updatePost(updatedPost)
            loadPosts()
        }
    }

    fun getCommentsForPost(postId: String): LiveData<List<Comment>> {
        return commentDao.getCommentsForPost(postId)
    }

    fun addComment(comment: Comment) {
        viewModelScope.launch(Dispatchers.IO) {
            commentDao.insertComment(comment)
        }
    }
}

