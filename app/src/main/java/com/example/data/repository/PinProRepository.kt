package com.example.data.repository

import android.util.Log
import com.example.data.model.Alert
import com.example.data.model.AlertCategory
import com.example.data.model.AlertLocation
import com.example.data.model.AlertStatus
import com.example.data.model.Barangay
import com.example.data.model.DemoLocationOverride
import com.example.data.model.LatLng
import com.example.data.model.Tanod
import com.example.data.model.TanodAvailability
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.data.model.AnimalReportPin
import com.example.data.model.AnimalType
import com.example.data.model.PinStatus
import com.example.data.model.CyberPin
import com.example.data.model.isPointInPolygon
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class PinProRepository(
    private val firestore: FirebaseFirestore? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _barangays = MutableStateFlow<List<Barangay>>(emptyList())
    val barangays: StateFlow<List<Barangay>> = _barangays.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _tanods = MutableStateFlow<List<Tanod>>(emptyList())
    val tanods: StateFlow<List<Tanod>> = _tanods.asStateFlow()

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _locationOverride = MutableStateFlow(DemoLocationOverride.SAMPLE_HALL)
    val locationOverride: StateFlow<DemoLocationOverride> = _locationOverride.asStateFlow()

    // Dangerous Animal Report Pins
    private val _animalPins = MutableStateFlow<List<AnimalReportPin>>(emptyList())
    val animalPins: StateFlow<List<AnimalReportPin>> = _animalPins.asStateFlow()
    val animalReportPins: StateFlow<List<AnimalReportPin>> = _animalPins.asStateFlow()
    val cyberPins: StateFlow<List<AnimalReportPin>> = _animalPins.asStateFlow()

    private val _filterOnlyPending = MutableStateFlow(false)
    val filterOnlyPending: StateFlow<Boolean> = _filterOnlyPending.asStateFlow()
    val isPendingOnlyFilterActive: StateFlow<Boolean> = _filterOnlyPending.asStateFlow()
    val isSuntukanFilterActive: StateFlow<Boolean> = _filterOnlyPending.asStateFlow()

    fun toggleFilterOnlyPending() {
        _filterOnlyPending.value = !_filterOnlyPending.value
    }
    fun togglePendingOnlyFilter() = toggleFilterOnlyPending()
    fun toggleSuntukanFilter() = toggleFilterOnlyPending()

    fun addAnimalReportPin(
        animalType: AnimalType,
        label: String,
        description: String,
        xRatio: Float,
        yRatio: Float,
        locationName: String = "Spotted in Neighborhood"
    ) {
        val newPin = AnimalReportPin(
            id = "pin_${System.currentTimeMillis()}",
            animalType = animalType,
            label = label.ifBlank { "${animalType.displayName} Sighted" },
            description = description.ifBlank { "Dangerous ${animalType.displayName.lowercase()} reported in area. Stay cautious." },
            locationName = locationName,
            xRatio = xRatio.coerceIn(0.08f, 0.92f),
            yRatio = yRatio.coerceIn(0.08f, 0.92f),
            status = PinStatus.PENDING,
            reportedBy = _currentUser.value?.name ?: "Resident",
            isPrimary = false,
            category = animalType.displayName
        )
        _animalPins.value = listOf(newPin) + _animalPins.value

        // Persist to Cloud Firestore if enabled
        val currentUserId = if (firestore != null) {
            try {
                Firebase.auth.currentUser?.uid
            } catch (_: Exception) {
                null
            }
        } else null
        if (firestore != null && currentUserId != null) {
            val pinData = hashMapOf(
                "id" to newPin.id,
                "animalType" to animalType.name,
                "label" to newPin.label,
                "description" to newPin.description,
                "locationName" to newPin.locationName,
                "xRatio" to newPin.xRatio.toDouble(),
                "yRatio" to newPin.yRatio.toDouble(),
                "status" to newPin.status.name,
                "reportedBy" to newPin.reportedBy,
                "userId" to currentUserId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("pins").document(newPin.id).set(pinData)
                .addOnFailureListener { e ->
                    Log.w("PinProRepository", "Failed to sync pin to Firestore: ${e.message}")
                }
        }
    }

    fun addCyberPin(label: String, xRatio: Float, yRatio: Float, description: String = "Dangerous Animal Sighting") {
        val lower = label.lowercase()
        val type = when {
            lower.contains("dog") -> AnimalType.AGGRESSIVE_DOG
            lower.contains("snake") || lower.contains("cobra") || lower.contains("python") -> AnimalType.SNAKE
            else -> AnimalType.SNAKE
        }
        addAnimalReportPin(type, label, description, xRatio, yRatio)
    }

    fun markPinAsDone(id: String) {
        _animalPins.value = _animalPins.value.map {
            if (it.id == id) it.copy(status = PinStatus.DONE, resolvedAt = System.currentTimeMillis()) else it
        }
        firestore?.collection("pins")?.document(id)?.update(
            "status", "DONE",
            "updatedAt", FieldValue.serverTimestamp()
        )
    }

    fun markPinAsPending(id: String) {
        _animalPins.value = _animalPins.value.map {
            if (it.id == id) it.copy(status = PinStatus.PENDING, resolvedAt = null) else it
        }
        firestore?.collection("pins")?.document(id)?.update(
            "status", "PENDING",
            "updatedAt", FieldValue.serverTimestamp()
        )
    }

    fun togglePinStatus(id: String) {
        var updatedStatus = PinStatus.PENDING
        _animalPins.value = _animalPins.value.map {
            if (it.id == id) {
                if (it.status == PinStatus.PENDING) {
                    updatedStatus = PinStatus.DONE
                    it.copy(status = PinStatus.DONE, resolvedAt = System.currentTimeMillis())
                } else {
                    updatedStatus = PinStatus.PENDING
                    it.copy(status = PinStatus.PENDING, resolvedAt = null)
                }
            } else it
        }
        firestore?.collection("pins")?.document(id)?.update(
            "status", updatedStatus.name,
            "updatedAt", FieldValue.serverTimestamp()
        )
    }

    fun removeCyberPin(id: String) {
        _animalPins.value = _animalPins.value.filter { it.id != id }
        firestore?.collection("pins")?.document(id)?.delete()
    }

    private var movementJob: Job? = null

    init {
        seedDemoData()
        listenToFirestore()
    }

    private fun listenToFirestore() {
        if (firestore == null) return
        try {
            firestore.collection("pins").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("PinProRepository", "Firestore snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val firestorePins = snapshot.documents.mapNotNull { doc ->
                        try {
                            val typeStr = doc.getString("animalType") ?: "SNAKE"
                            val animalType = try { AnimalType.valueOf(typeStr) } catch (_: Exception) { AnimalType.SNAKE }
                            val statusStr = doc.getString("status") ?: "PENDING"
                            val status = try { PinStatus.valueOf(statusStr) } catch (_: Exception) { PinStatus.PENDING }
                            AnimalReportPin(
                                id = doc.id,
                                animalType = animalType,
                                label = doc.getString("label") ?: "${animalType.displayName} Sighted",
                                description = doc.getString("description") ?: "",
                                locationName = doc.getString("locationName") ?: "Neighborhood Sector",
                                xRatio = (doc.getDouble("xRatio") ?: 0.5).toFloat(),
                                yRatio = (doc.getDouble("yRatio") ?: 0.5).toFloat(),
                                status = status,
                                reportedBy = doc.getString("reportedBy") ?: "Resident",
                                timestamp = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (firestorePins.isNotEmpty()) {
                        _animalPins.value = firestorePins
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("PinProRepository", "Error setting up Firestore listener", e)
        }
    }

    fun setLocationOverride(override: DemoLocationOverride) {
        _locationOverride.value = override
    }

    fun getCurrentDeviceLocation(): LatLng {
        return _locationOverride.value.latLng
    }

    fun loginAs(user: User) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }

    fun registerResident(
        name: String,
        mobile: String,
        address: String,
        homeBarangayId: String
    ): User {
        val newUser = User(
            uid = "res_${System.currentTimeMillis()}",
            role = UserRole.RESIDENT,
            name = name,
            mobile = mobile,
            status = UserStatus.ACTIVE,
            address = address,
            homeBarangayId = homeBarangayId,
            createdAt = System.currentTimeMillis()
        )
        _users.value = _users.value + newUser
        _currentUser.value = newUser
        return newUser
    }

    fun updateUserProfile(name: String, address: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(name = name, address = address)
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.uid == current.uid) updated else it }
    }

    fun setResidentStatus(userId: String, status: UserStatus) {
        _users.value = _users.value.map { user ->
            if (user.uid == userId) user.copy(status = status) else user
        }
        if (_currentUser.value?.uid == userId) {
            _currentUser.value = _currentUser.value?.copy(status = status)
        }
    }

    /**
     * Check which barangay the point physically resides in using point-in-polygon.
     */
    fun findPhysicalBarangay(point: LatLng): Barangay? {
        return _barangays.value.firstOrNull { barangay ->
            isPointInPolygon(point, barangay.boundaryPolygon)
        }
    }

    /**
     * Creates an alert based on point-in-polygon check.
     */
    fun createAlert(
        resident: User,
        category: AlertCategory,
        note: String,
        photoUrl: String? = null,
        shareLiveLocation: Boolean = true,
        forceHomeBarangay: Boolean = false
    ): Alert {
        val physicalLoc = getCurrentDeviceLocation()
        val physicalBarangay = findPhysicalBarangay(physicalLoc)

        val receivingBarangayId = when {
            forceHomeBarangay -> resident.homeBarangayId
            physicalBarangay != null -> physicalBarangay.id
            else -> resident.homeBarangayId
        }

        val fromOutsideHome = (receivingBarangayId != resident.homeBarangayId)

        val newAlert = Alert(
            id = "ALT-${System.currentTimeMillis().toString().takeLast(5)}",
            residentId = resident.uid,
            residentName = resident.name,
            residentMobile = resident.mobile,
            receivingBarangayId = receivingBarangayId,
            fromOutsideHome = fromOutsideHome,
            category = category,
            note = note,
            photoUrl = photoUrl,
            location = AlertLocation(
                lat = physicalLoc.lat,
                lng = physicalLoc.lng,
                address = "${resident.address} (Near ${physicalBarangay?.name ?: "Barangay Border"})"
            ),
            shareLiveLocation = shareLiveLocation,
            status = AlertStatus.RECEIVED,
            createdAt = System.currentTimeMillis()
        )

        _alerts.value = listOf(newAlert) + _alerts.value
        return newAlert
    }

    fun cancelAlert(alertId: String) {
        _alerts.value = _alerts.value.map { alert ->
            if (alert.id == alertId) alert.copy(status = AlertStatus.CANCELLED) else alert
        }
    }

    fun assignTanod(alertId: String, tanodId: String) {
        val tanod = _tanods.value.firstOrNull { it.uid == tanodId } ?: return
        val alert = _alerts.value.firstOrNull { it.id == alertId } ?: return

        // Generate realistic waypoints from Tanod location to Alert location
        val route = generateRoute(tanod.currentLocation, LatLng(alert.location.lat, alert.location.lng))

        _tanods.value = _tanods.value.map {
            if (it.uid == tanodId) it.copy(availability = TanodAvailability.BUSY) else it
        }

        _alerts.value = _alerts.value.map {
            if (it.id == alertId) {
                it.copy(
                    status = AlertStatus.ASSIGNED,
                    assignedTanodId = tanodId,
                    assignedAt = System.currentTimeMillis(),
                    etaMinutes = 7,
                    route = route
                )
            } else it
        }
    }

    fun updateAlertStatus(alertId: String, newStatus: AlertStatus) {
        val alert = _alerts.value.firstOrNull { it.id == alertId } ?: return

        _alerts.value = _alerts.value.map {
            if (it.id == alertId) {
                when (newStatus) {
                    AlertStatus.ON_THE_WAY -> {
                        it.copy(status = AlertStatus.ON_THE_WAY, etaMinutes = 5)
                    }
                    AlertStatus.ARRIVED -> {
                        it.copy(status = AlertStatus.ARRIVED, etaMinutes = 0, arrivedAt = System.currentTimeMillis())
                    }
                    AlertStatus.RESOLVED -> {
                        it.copy(status = AlertStatus.RESOLVED, resolvedAt = System.currentTimeMillis())
                    }
                    else -> it.copy(status = newStatus)
                }
            } else it
        }

        if (newStatus == AlertStatus.ON_THE_WAY && alert.assignedTanodId != null) {
            startTanodMovementSimulation(alertId, alert.assignedTanodId)
        }

        if (newStatus == AlertStatus.RESOLVED && alert.assignedTanodId != null) {
            _tanods.value = _tanods.value.map {
                if (it.uid == alert.assignedTanodId) {
                    it.copy(
                        availability = TanodAvailability.AVAILABLE,
                        responseCountToday = it.responseCountToday + 1
                    )
                } else it
            }
        }
    }

    fun rateAlert(alertId: String, rating: Int) {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(rating = rating) else it
        }
    }

    fun toggleTanodAvailability(tanodId: String, availability: TanodAvailability) {
        _tanods.value = _tanods.value.map {
            if (it.uid == tanodId) it.copy(availability = availability) else it
        }
    }

    fun addTanod(barangayId: String, name: String, badgeNumber: String) {
        val bHall = _barangays.value.firstOrNull { it.id == barangayId }?.hallLocation ?: LatLng(14.6538, 121.0505)
        val newTanod = Tanod(
            uid = "tanod_${System.currentTimeMillis()}",
            barangayId = barangayId,
            name = name,
            badgeNumber = badgeNumber,
            availability = TanodAvailability.AVAILABLE,
            currentLocation = LatLng(bHall.lat + 0.001, bHall.lng + 0.001),
            responseCountToday = 0
        )
        _tanods.value = _tanods.value + newTanod
    }

    /**
     * Real-time Grab/Lalamove Movement Simulation.
     * Gradually animates the Tanod's position along the route polyline towards the resident,
     * writing the updated location every second, updating etaMinutes downwards.
     */
    fun startTanodMovementSimulation(alertId: String, tanodId: String) {
        movementJob?.cancel()
        movementJob = scope.launch {
            val alert = _alerts.value.firstOrNull { it.id == alertId } ?: return@launch
            val route = alert.route.ifEmpty {
                val tanod = _tanods.value.firstOrNull { it.uid == tanodId } ?: return@launch
                generateRoute(tanod.currentLocation, LatLng(alert.location.lat, alert.location.lng))
            }
            if (route.isEmpty()) return@launch

            val steps = route.size
            for (i in 0 until steps) {
                val currentStepLoc = route[i]
                val remainingRatio = (steps - 1 - i).toFloat() / (steps - 1).coerceAtLeast(1)
                val remainingEta = (remainingRatio * 6).toInt().coerceAtLeast(if (i == steps - 1) 0 else 1)

                // Update Tanod location in state
                _tanods.value = _tanods.value.map {
                    if (it.uid == tanodId) it.copy(currentLocation = currentStepLoc) else it
                }

                // Update Alert ETA and route progress
                _alerts.value = _alerts.value.map {
                    if (it.id == alertId) it.copy(etaMinutes = remainingEta) else it
                }

                delay(1200L) // 1.2s per step for smooth realistic tracking
            }

            // Arrived at destination
            updateAlertStatus(alertId, AlertStatus.ARRIVED)
        }
    }

    fun simulateIncomingAlert(barangayId: String) {
        val barangay = _barangays.value.firstOrNull { it.id == barangayId } ?: return
        val resident = _users.value.filter { it.role == UserRole.RESIDENT && it.homeBarangayId == barangayId }.randomOrNull()
            ?: _users.value.first { it.role == UserRole.RESIDENT }

        val categories = AlertCategory.values()
        val randomCategory = categories[Random.nextInt(categories.size)]

        val notes = mapOf(
            AlertCategory.MEDICAL to "Elderly resident experiencing dizziness and shortness of breath.",
            AlertCategory.FIRE to "Smoke observed coming from secondary kitchen in compound.",
            AlertCategory.CRIME to "Loud disturbance and argument on street corner, peace & order assistance needed.",
            AlertCategory.FLOOD to "Flash flood water rising rapidly near drainage culvert.",
            AlertCategory.OTHER to "Fallen tree branch blocking barangay access road."
        )

        // Point near barangay hall
        val alertLoc = LatLng(
            barangay.hallLocation.lat + (Random.nextDouble(-0.003, 0.003)),
            barangay.hallLocation.lng + (Random.nextDouble(-0.003, 0.003))
        )

        val newAlert = Alert(
            id = "ALT-${System.currentTimeMillis().toString().takeLast(5)}",
            residentId = resident.uid,
            residentName = resident.name,
            residentMobile = resident.mobile,
            receivingBarangayId = barangayId,
            fromOutsideHome = false,
            category = randomCategory,
            note = notes[randomCategory] ?: "Emergency assistance requested.",
            location = AlertLocation(
                lat = alertLoc.lat,
                lng = alertLoc.lng,
                address = "${resident.address}, ${barangay.name}"
            ),
            shareLiveLocation = true,
            status = AlertStatus.RECEIVED,
            createdAt = System.currentTimeMillis()
        )

        _alerts.value = listOf(newAlert) + _alerts.value
    }

    private fun generateRoute(start: LatLng, end: LatLng): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val totalSteps = 16
        // Create an organic route simulating Manila/Pasig street navigation (slight doglegs)
        val midLat = (start.lat + end.lat) / 2 + 0.0006
        val midLng = (start.lng + end.lng) / 2 - 0.0004

        for (i in 0..totalSteps) {
            val t = i.toDouble() / totalSteps
            val lat: Double
            val lng: Double
            if (t < 0.5) {
                val subT = t * 2
                lat = start.lat + (midLat - start.lat) * subT
                lng = start.lng + (midLng - start.lng) * subT
            } else {
                val subT = (t - 0.5) * 2
                lat = midLat + (end.lat - midLat) * subT
                lng = midLng + (end.lng - midLng) * subT
            }
            points.add(LatLng(lat, lng))
        }
        return points
    }

    /**
     * Seeds complete realistic Philippine barangay demo data.
     */
    fun seedDemoData() {
        movementJob?.cancel()

        // 1. Barangays
        val bSampleHall = Barangay(
            id = "sample_hall",
            name = "Barangay Sample Hall",
            hallLocation = LatLng(14.6538, 121.0505),
            hallContact = "(02) 8876-1234 / 0917-888-HALL",
            boundaryPolygon = listOf(
                LatLng(14.6590, 121.0440),
                LatLng(14.6590, 121.0550),
                LatLng(14.6480, 121.0550),
                LatLng(14.6480, 121.0440)
            )
        )

        val bRiverside = Barangay(
            id = "riverside",
            name = "Barangay Riverside",
            hallLocation = LatLng(14.6538, 121.0610),
            hallContact = "(02) 8876-5678 / 0918-999-RIVR",
            boundaryPolygon = listOf(
                LatLng(14.6590, 121.0550),
                LatLng(14.6590, 121.0660),
                LatLng(14.6480, 121.0660),
                LatLng(14.6480, 121.0550)
            )
        )
        _barangays.value = listOf(bSampleHall, bRiverside)

        // 2. Users
        val seededUsers = listOf(
            User(
                uid = "res_maria",
                role = UserRole.RESIDENT,
                name = "Maria Santos",
                mobile = "+63 917 123 4567",
                status = UserStatus.ACTIVE,
                address = "12 Acacia St., Purok 3",
                homeBarangayId = "sample_hall"
            ),
            User(
                uid = "res_jose",
                role = UserRole.RESIDENT,
                name = "Jose Reyes",
                mobile = "+63 920 234 5678",
                status = UserStatus.ACTIVE,
                address = "45 Narra Ave., Purok 1",
                homeBarangayId = "sample_hall"
            ),
            User(
                uid = "res_liza",
                role = UserRole.RESIDENT,
                name = "Liza Ramos",
                mobile = "+63 918 345 6789",
                status = UserStatus.ACTIVE,
                address = "88 Riverside Blvd., Purok 5",
                homeBarangayId = "riverside"
            ),
            User(
                uid = "admin_ramon",
                role = UserRole.ADMIN,
                name = "Kapitan Ramon Villanueva",
                mobile = "+63 917 999 1111",
                officialId = "KAP-01-SH",
                status = UserStatus.ACTIVE,
                address = "Barangay Hall Complex",
                homeBarangayId = "sample_hall",
                barangayId = "sample_hall"
            ),
            User(
                uid = "admin_elena",
                role = UserRole.ADMIN,
                name = "Kapitana Elena Cruz",
                mobile = "+63 918 888 2222",
                officialId = "KAP-02-RV",
                status = UserStatus.ACTIVE,
                address = "Riverside Barangay Center",
                homeBarangayId = "riverside",
                barangayId = "riverside"
            ),
            User(
                uid = "tanod_berto",
                role = UserRole.TANOD,
                name = "Berto Aquino",
                mobile = "+63 921 555 1201",
                officialId = "TND-SH-04",
                status = UserStatus.ACTIVE,
                address = "Barangay Outpost 1",
                homeBarangayId = "sample_hall",
                barangayId = "sample_hall"
            ),
            User(
                uid = "tanod_lito",
                role = UserRole.TANOD,
                name = "Lito Mercado",
                mobile = "+63 921 555 1202",
                officialId = "TND-SH-07",
                status = UserStatus.ACTIVE,
                address = "Barangay Outpost 2",
                homeBarangayId = "sample_hall",
                barangayId = "sample_hall"
            ),
            User(
                uid = "tanod_dodong",
                role = UserRole.TANOD,
                name = "Dodong Salazar",
                mobile = "+63 919 555 2301",
                officialId = "TND-RV-01",
                status = UserStatus.ACTIVE,
                address = "Riverside Outpost East",
                homeBarangayId = "riverside",
                barangayId = "riverside"
            )
        )
        _users.value = seededUsers

        // 3. Tanods
        val seededTanods = listOf(
            Tanod(
                uid = "tanod_berto",
                barangayId = "sample_hall",
                name = "Berto Aquino",
                badgeNumber = "TND-04",
                availability = TanodAvailability.AVAILABLE,
                currentLocation = LatLng(14.6520, 121.0490),
                responseCountToday = 3,
                contact = "0921-555-1201"
            ),
            Tanod(
                uid = "tanod_lito",
                barangayId = "sample_hall",
                name = "Lito Mercado",
                badgeNumber = "TND-07",
                availability = TanodAvailability.AVAILABLE,
                currentLocation = LatLng(14.6550, 121.0520),
                responseCountToday = 2,
                contact = "0921-555-1202"
            ),
            Tanod(
                uid = "tanod_cardo",
                barangayId = "sample_hall",
                name = "Cardo Dalisay",
                badgeNumber = "TND-09",
                availability = TanodAvailability.OFF_DUTY,
                currentLocation = LatLng(14.6538, 121.0505),
                responseCountToday = 4,
                contact = "0921-555-1203"
            ),
            Tanod(
                uid = "tanod_dodong",
                barangayId = "riverside",
                name = "Dodong Salazar",
                badgeNumber = "TND-11",
                availability = TanodAvailability.AVAILABLE,
                currentLocation = LatLng(14.6540, 121.0590),
                responseCountToday = 3,
                contact = "0919-555-2301"
            ),
            Tanod(
                uid = "tanod_junjun",
                barangayId = "riverside",
                name = "Junjun Perez",
                badgeNumber = "TND-15",
                availability = TanodAvailability.AVAILABLE,
                currentLocation = LatLng(14.6525, 121.0620),
                responseCountToday = 1,
                contact = "0919-555-2302"
            )
        )
        _tanods.value = seededTanods

        // 4. Past Resolved Alerts (History)
        val now = System.currentTimeMillis()
        val dayMillis = 24 * 60 * 60 * 1000L
        val pastAlerts = listOf(
            Alert(
                id = "ALT-88421",
                residentId = "res_maria",
                residentName = "Maria Santos",
                residentMobile = "+63 917 123 4567",
                receivingBarangayId = "sample_hall",
                category = AlertCategory.MEDICAL,
                note = "BP spike and severe headache, first-aid needed.",
                location = AlertLocation(14.6530, 121.0500, "12 Acacia St., Purok 3"),
                status = AlertStatus.RESOLVED,
                assignedTanodId = "tanod_berto",
                createdAt = now - (dayMillis * 3),
                resolvedAt = now - (dayMillis * 3) + 25 * 60 * 1000,
                rating = 5
            ),
            Alert(
                id = "ALT-76192",
                residentId = "res_maria",
                residentName = "Maria Santos",
                residentMobile = "+63 917 123 4567",
                receivingBarangayId = "sample_hall",
                category = AlertCategory.CRIME,
                note = "Suspicious vehicle parked blocking gate past midnight.",
                location = AlertLocation(14.6530, 121.0500, "12 Acacia St., Purok 3"),
                status = AlertStatus.RESOLVED,
                assignedTanodId = "tanod_lito",
                createdAt = now - (dayMillis * 8),
                resolvedAt = now - (dayMillis * 8) + 18 * 60 * 1000,
                rating = 4
            ),
            Alert(
                id = "ALT-54120",
                residentId = "res_jose",
                residentName = "Jose Reyes",
                residentMobile = "+63 920 234 5678",
                receivingBarangayId = "sample_hall",
                category = AlertCategory.FLOOD,
                note = "Drainage clogged, gutter overflow entering garage.",
                location = AlertLocation(14.6515, 121.0480, "45 Narra Ave., Purok 1"),
                status = AlertStatus.RESOLVED,
                assignedTanodId = "tanod_berto",
                createdAt = now - (dayMillis * 5),
                resolvedAt = now - (dayMillis * 5) + 40 * 60 * 1000,
                rating = 5
            ),
            Alert(
                id = "ALT-92314",
                residentId = "res_liza",
                residentName = "Liza Ramos",
                residentMobile = "+63 918 345 6789",
                receivingBarangayId = "riverside",
                category = AlertCategory.OTHER,
                note = "Streetlight flickering and sparked during afternoon rain.",
                location = AlertLocation(14.6540, 121.0600, "88 Riverside Blvd."),
                status = AlertStatus.RESOLVED,
                assignedTanodId = "tanod_dodong",
                createdAt = now - (dayMillis * 2),
                resolvedAt = now - (dayMillis * 2) + 30 * 60 * 1000,
                rating = 5
            )
        )
        _alerts.value = pastAlerts

        // Default Dangerous Animal Alert Pins
        _animalPins.value = listOf(
            AnimalReportPin(
                id = "pin_snake_cobra",
                animalType = AnimalType.SNAKE,
                label = "Cobra Sighted near Garden Path",
                description = "Large dark cobra spotted slithering into overgrown bush near perimeter fence.",
                locationName = "12 Acacia St. Alley",
                xRatio = 0.50f,
                yRatio = 0.44f,
                status = PinStatus.PENDING,
                reportedBy = "Maria Santos",
                timestamp = System.currentTimeMillis() - (15 * 60 * 1000),
                isPrimary = true,
                category = "Snake"
            ),
            AnimalReportPin(
                id = "pin_aggressive_dog_corner",
                animalType = AnimalType.AGGRESSIVE_DOG,
                label = "Aggressive Stray Dog Pack",
                description = "Two aggressive medium-sized stray dogs barking and lunging at passing pedestrians.",
                locationName = "Corner Purok 3 & Narra",
                xRatio = 0.30f,
                yRatio = 0.65f,
                status = PinStatus.PENDING,
                reportedBy = "Jose Reyes",
                timestamp = System.currentTimeMillis() - (45 * 60 * 1000),
                isPrimary = false,
                category = "Aggressive Dog"
            ),
            AnimalReportPin(
                id = "pin_dog_secured",
                animalType = AnimalType.AGGRESSIVE_DOG,
                label = "Aggressive Rottweiler Mix (Secured)",
                description = "Loose aggressive dog reported. Safely captured and leashed by animal control team.",
                locationName = "Riverside Blvd. Block 4",
                xRatio = 0.74f,
                yRatio = 0.30f,
                status = PinStatus.DONE,
                reportedBy = "Liza Ramos",
                timestamp = System.currentTimeMillis() - (3 * 3600 * 1000),
                resolvedAt = System.currentTimeMillis() - (1 * 3600 * 1000),
                isPrimary = false,
                category = "Aggressive Dog"
            ),
            AnimalReportPin(
                id = "pin_python_handled",
                animalType = AnimalType.SNAKE,
                label = "Reticulated Python in Culvert (Cleared)",
                description = "7-foot python discovered in open drainage. Retrieved and transferred safely to wildlife rescue.",
                locationName = "Drainage Gate Sector 2",
                xRatio = 0.22f,
                yRatio = 0.26f,
                status = PinStatus.DONE,
                reportedBy = "Barangay Tanod Team",
                timestamp = System.currentTimeMillis() - (6 * 3600 * 1000),
                resolvedAt = System.currentTimeMillis() - (4 * 3600 * 1000),
                isPrimary = false,
                category = "Snake"
            )
        )

        // Default login: Maria Santos (Resident)
        _currentUser.value = seededUsers.first { it.uid == "res_maria" }
    }
}
