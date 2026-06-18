package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Trip
import com.example.data.repository.TripRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class ActiveJobState {
    object Idle : ActiveJobState()
    data class Requested(val job: JobRequest, val timeLeftSeconds: Int) : ActiveJobState()
    data class Accepted(val job: JobRequest) : ActiveJobState()
    data class EnRouteToPickup(val job: JobRequest, val progress: Float) : ActiveJobState()
    data class ArrivedAtPickup(val job: JobRequest) : ActiveJobState()
    data class EnRouteToDropoff(val job: JobRequest, val progress: Float) : ActiveJobState() // progress 0.0f to 1.0f
    data class ArrivedAtDropoff(val job: JobRequest) : ActiveJobState()
    data class CompletedGreeting(val job: JobRequest) : ActiveJobState()
}

data class JobRequest(
    val id: String,
    val type: String, // "Ride" or "Delivery"
    val partnerName: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val payout: Double,
    val distanceMiles: Double,
    val durationMinutes: Int,
    val pickupX: Float, // Map coordinates (0f to 300f relative)
    val pickupY: Float,
    val dropoffX: Float,
    val dropoffY: Float
)

data class DriverProfile(
    val name: String,
    val vehicleModel: String,
    val licensePlate: String,
    val rating: Float,
    val level: String
)

class DriverViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TripRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TripRepository(database.tripDao())
    }

    // Trip logs from database
    val completedTrips: StateFlow<List<Trip>> = repository.allTrips
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Online Status
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Online Timer
    private val _onlineSeconds = MutableStateFlow(0L)
    val onlineSeconds: StateFlow<Long> = _onlineSeconds.asStateFlow()

    // Active Job State
    private val _activeJobState = MutableStateFlow<ActiveJobState>(ActiveJobState.Idle)
    val activeJobState: StateFlow<ActiveJobState> = _activeJobState.asStateFlow()

    // Driver Profile Info
    private val _profile = MutableStateFlow(
        DriverProfile(
            name = "Alex Carter",
            vehicleModel = "Toyota Prius Prime (Silver)",
            licensePlate = "DRV-137X",
            rating = 4.95f,
            level = "Gold Partner"
        )
    )
    val profile: StateFlow<DriverProfile> = _profile.asStateFlow()

    // Core Timers / Coroutine jobs
    private var onlineTimerJob: Job? = null
    private var offerGeneratorJob: Job? = null
    private var driveSimulationJob: Job? = null
    private var offerCountdownJob: Job? = null

    init {
        // Pre-populate database with some realistic historical records on empty start
        viewModelScope.launch {
            repository.allTrips.collect { list ->
                if (list.isEmpty()) {
                    val defaultTrips = listOf(
                        Trip(
                            type = "Ride",
                            pickupAddress = "528 Pine Street, Downtown",
                            dropoffAddress = "Grand Central Terminal",
                            payout = 18.50,
                            distanceMiles = 4.2,
                            durationMinutes = 14,
                            rating = 5.0f,
                            partnerName = "Sarah Collins",
                            timestamp = System.currentTimeMillis() - 4 * 3600 * 1000
                        ),
                        Trip(
                            type = "Delivery",
                            pickupAddress = "Organic Greens Wholesale",
                            dropoffAddress = "1205 North Broadway Ave",
                            payout = 12.75,
                            distanceMiles = 3.1,
                            durationMinutes = 10,
                            rating = 4.8f,
                            partnerName = "Local Grocery Order #4102",
                            timestamp = System.currentTimeMillis() - 24 * 3600 * 1000
                        ),
                        Trip(
                            type = "Ride",
                            pickupAddress = "Metropolitan Arts Museum",
                            dropoffAddress = "Ritz-Carlton Hotel",
                            payout = 26.30,
                            distanceMiles = 7.8,
                            durationMinutes = 22,
                            rating = 5.0f,
                            partnerName = "Marcus Aurelius",
                            timestamp = System.currentTimeMillis() - 36 * 3600 * 1000
                        )
                    )
                    defaultTrips.forEach { repository.insert(it) }
                }
            }
        }
    }

    fun toggleOnline() {
        val newOnline = !_isOnline.value
        _isOnline.value = newOnline
        if (newOnline) {
            startOnlineTimers()
        } else {
            stopOnlineTimers()
            cancelCurrentSimulation()
            if (_activeJobState.value !is ActiveJobState.CompletedGreeting) {
                _activeJobState.value = ActiveJobState.Idle
            }
        }
    }

    private fun startOnlineTimers() {
        onlineTimerJob?.cancel()
        onlineTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _onlineSeconds.value += 1
            }
        }

        // Automatic random offer generator (e.g. periodically offers a job every 20-30 seconds if idle)
        offerGeneratorJob?.cancel()
        offerGeneratorJob = viewModelScope.launch {
            while (true) {
                delay(15000) // check more often to keep standard demo snappy
                if (_isOnline.value && _activeJobState.value is ActiveJobState.Idle) {
                    // 30% chance to spawn an unsolicited offer naturally
                    if ((1..100).random() < 40) {
                        spawnRandomOffer()
                    }
                }
            }
        }
    }

    private fun stopOnlineTimers() {
        onlineTimerJob?.cancel()
        onlineTimerJob = null
        offerGeneratorJob?.cancel()
        offerGeneratorJob = null
    }

    fun spawnRandomOffer() {
        val types = listOf("Ride", "Delivery")
        val jobType = types.random()
        
        val job = if (jobType == "Ride") {
            val names = listOf("Chloe Sterling", "David Miller", "Lucas Vance", "Sophia Reed", "James Chen")
            val pickups = listOf("Union Square West", "Broadway Theatre District", "West Side Marina", "Hilton Executive Suites", "Central Park Zoo Entrance")
            val dropoffs = listOf("LaGuardia Airport Terminal B", "Madison Square Garden", "Brooklyn Tech Hub", "Financial District Plaza", "St. Jude Clinic")
            val distance = (1.5 + (0.1 * (10..150).random())) // 1.5 to 16.5 miles
            val payout = (6.0 + distance * 2.2).let { Math.round(it * 100) / 100.0 }
            val minutes = (distance * 2.8 + (2..7).random()).toInt()

            JobRequest(
                id = "RIDE-${(1000..9999).random()}",
                type = "Ride",
                partnerName = names.random(),
                pickupAddress = pickups.random(),
                dropoffAddress = dropoffs.random(),
                payout = payout,
                distanceMiles = (Math.round(distance * 10) / 10.0),
                durationMinutes = minutes,
                pickupX = (50..250).random().toFloat(),
                pickupY = (50..250).random().toFloat(),
                dropoffX = (50..250).random().toFloat(),
                dropoffY = (50..250).random().toFloat()
            )
        } else {
            val merchants = listOf("Bistro Gourmet Restaurant", "Whole Foods Market #08", "CVS Pharmacy Store", "Home Depot Express", "Corner Bakery & Delicatessen")
            val pickups = listOf("Avenue of the Americas Plaza", "7th Ave Shopping Center", "Lexington Retail Row", "Port Authority Delivery Deck", "Greenwich Village Warehouse")
            val dropoffs = listOf("342 Lexington Apartment 12B", "901 Riverside Dr Townhouse", "East Village Condominium #4A", "Sutton Place Residence", "Gramercy Park Office")
            val distance = (0.8 + (0.1 * (5..80).random())) // 0.8 to 8.8 miles
            val payout = (4.5 + distance * 1.8).let { Math.round(it * 100) / 100.0 }
            val minutes = (distance * 3.1 + (3..8).random()).toInt()

            JobRequest(
                id = "DELV-${(1000..9999).random()}",
                type = "Delivery",
                partnerName = "Order containing: " + merchants.random(),
                pickupAddress = pickups.random(),
                dropoffAddress = dropoffs.random(),
                payout = payout,
                distanceMiles = (Math.round(distance * 10) / 10.0),
                durationMinutes = minutes,
                pickupX = (50..250).random().toFloat(),
                pickupY = (50..250).random().toFloat(),
                dropoffX = (50..250).random().toFloat(),
                dropoffY = (50..250).random().toFloat()
            )
        }

        _activeJobState.value = ActiveJobState.Requested(job, 15)

        // Countdown timer for offer
        offerCountdownJob?.cancel()
        offerCountdownJob = viewModelScope.launch {
            var timeLeft = 15
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
                val currentState = _activeJobState.value
                if (currentState is ActiveJobState.Requested && currentState.job.id == job.id) {
                    _activeJobState.value = ActiveJobState.Requested(job, timeLeft)
                } else {
                    break
                }
            }
            // Auto decline if countdown reaches 0
            if (_activeJobState.value is ActiveJobState.Requested && (_activeJobState.value as ActiveJobState.Requested).timeLeftSeconds == 0) {
                declineOffer()
            }
        }
    }

    fun acceptOffer(job: JobRequest) {
        offerCountdownJob?.cancel()
        offerCountdownJob = null
        _activeJobState.value = ActiveJobState.Accepted(job)
    }

    fun declineOffer() {
        offerCountdownJob?.cancel()
        offerCountdownJob = null
        _activeJobState.value = ActiveJobState.Idle
    }

    fun startDriveToPickup(job: JobRequest) {
        driveSimulationJob?.cancel()
        driveSimulationJob = viewModelScope.launch {
            _activeJobState.value = ActiveJobState.EnRouteToPickup(job, 0.0f)
            val totalSteps = 20
            for (step in 1..totalSteps) {
                delay(180) // ~3.6 seconds total
                val progress = step.toFloat() / totalSteps
                val currentState = _activeJobState.value
                if (currentState is ActiveJobState.EnRouteToPickup && currentState.job.id == job.id) {
                    _activeJobState.value = ActiveJobState.EnRouteToPickup(job, progress)
                } else {
                    break
                }
            }
            _activeJobState.value = ActiveJobState.ArrivedAtPickup(job)
        }
    }

    fun triggerPickupArrived(job: JobRequest) {
        driveSimulationJob?.cancel()
        _activeJobState.value = ActiveJobState.ArrivedAtPickup(job)
    }

    fun startTripAndEnRoute(job: JobRequest) {
        driveSimulationJob?.cancel()
        driveSimulationJob = viewModelScope.launch {
            _activeJobState.value = ActiveJobState.EnRouteToDropoff(job, 0.0f)
            
            // Simulating continuous car journey progress!
            val totalSteps = 40
            for (step in 1..totalSteps) {
                delay(150) // total ~6 seconds journey
                val currentProgress = step / totalSteps.toFloat()
                
                val currentJobState = _activeJobState.value
                if (currentJobState is ActiveJobState.EnRouteToDropoff && currentJobState.job.id == job.id) {
                    _activeJobState.value = ActiveJobState.EnRouteToDropoff(job, currentProgress)
                } else {
                    break
                }
            }
            
            _activeJobState.value = ActiveJobState.ArrivedAtDropoff(job)
        }
    }

    fun completeTrip(job: JobRequest) {
        driveSimulationJob?.cancel()
        driveSimulationJob = null
        
        viewModelScope.launch {
            val randomRating = listOf(4.5f, 4.8f, 5.0f, 5.0f, 5.0f).random()
            val finalTrip = Trip(
                type = job.type,
                pickupAddress = job.pickupAddress,
                dropoffAddress = job.dropoffAddress,
                payout = job.payout,
                distanceMiles = job.distanceMiles,
                durationMinutes = job.durationMinutes,
                rating = randomRating,
                partnerName = job.partnerName,
                timestamp = System.currentTimeMillis()
            )
            repository.insert(finalTrip)
            _activeJobState.value = ActiveJobState.CompletedGreeting(job)
        }
    }

    fun dismissGreeting() {
        _activeJobState.value = ActiveJobState.Idle
    }

    fun updateProfile(name: String, vehicle: String, plate: String) {
        val currentProfile = _profile.value
        _profile.value = currentProfile.copy(
            name = name,
            vehicleModel = vehicle,
            licensePlate = plate
        )
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun insertMockTrip(trip: Trip) {
        viewModelScope.launch {
            repository.insert(trip)
        }
    }

    private fun cancelCurrentSimulation() {
        driveSimulationJob?.cancel()
        driveSimulationJob = null
        offerCountdownJob?.cancel()
        offerCountdownJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopOnlineTimers()
        cancelCurrentSimulation()
    }
}
