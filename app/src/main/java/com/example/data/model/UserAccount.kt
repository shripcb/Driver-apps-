package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val passwordKey: String,
    val name: String,
    val vehicleModel: String,
    val licensePlate: String,
    val rating: Float = 4.95f,
    val level: String = "Gold Badge Partner"
)
