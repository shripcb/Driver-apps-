package com.example.data.repository

import com.example.data.database.TripDao
import com.example.data.model.Trip
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()

    suspend fun insert(trip: Trip) {
        tripDao.insertTrip(trip)
    }

    suspend fun delete(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    suspend fun clearAll() {
        tripDao.clearAll()
    }
}
