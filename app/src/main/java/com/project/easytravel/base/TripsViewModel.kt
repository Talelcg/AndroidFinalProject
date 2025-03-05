package com.project.easytravel.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.easytravel.model.AppDatabase
import com.project.easytravel.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TripsViewModel(application: Application) : AndroidViewModel(application) {
    private val postDao = AppDatabase.getDatabase(application).postDao()
    val allTrips: LiveData<List<Post>> = postDao.getAllPosts()

    fun insertTrip(trip: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            postDao.insertPost(trip)
        }
    }


}