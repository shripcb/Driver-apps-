package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,               // "Delivery" or "Ride"
    val pickupAddress: String,
    val dropoffAddress: String,
    val payout: Double,
    val distanceMiles: Double,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val partnerName: String,        // Passenger name or order details
    val status: String = "Completed",
    val rating: Float = 5.0f
)
