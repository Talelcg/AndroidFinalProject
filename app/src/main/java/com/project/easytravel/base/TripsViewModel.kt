package com.project.easytravel.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.project.easytravel.model.AppDatabase
import com.project.easytravel.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TripsViewModel(application: Application) : AndroidViewModel(application) {
    private val tripDao = AppDatabase.getDatabase(application).tripDao()
    val allTrips: LiveData<List<Trip>> = tripDao.getAllTrips()

    fun insertTrip(trip: Trip) {
        viewModelScope.launch(Dispatchers.IO) {
            tripDao.insertTrip(trip)
        }
    }


}