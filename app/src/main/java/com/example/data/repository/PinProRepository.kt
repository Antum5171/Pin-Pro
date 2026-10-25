/**
 * PinProRepository.kt
 *
 * Primary data repository and state manager for PinPro. Manages active incident reports,
 * status state transitions (REPORTED -> ACCEPTED -> ON_THE_WAY -> ARRIVED -> RESOLVED),
 * real-time notification dispatching to residents and officials, official location tracking,
 * and archived records storage.
 */
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
import com.example.data.model.AppRole
import com.example.data.model.ReportCategory
import com.example.data.model.ReportStatus
import com.example.data.model.IncidentReport
import com.example.data.model.ReportEvent
import com.example.data.model.OfficialLocation
import com.example.data.model.AppNotification
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

    // Incident Reports (0 mock reports - clean Firestore records only)
    private val _animalPins = MutableStateFlow<List<AnimalReportPin>>(emptyList())
    val animalPins: StateFlow<List<AnimalReportPin>> = _animalPins.asStateFlow()
    val animalReportPins: StateFlow<List<AnimalReportPin>> = _animalPins.asStateFlow()
    val cyberPins: StateFlow<List<AnimalReportPin>> = _animalPins.asStateFlow()

    // Audit Trail Events (`report_events`)
    private val _reportEvents = MutableStateFlow<List<ReportEvent>>(emptyList())
    val reportEvents: StateFlow<List<ReportEvent>> = _reportEvents.asStateFlow()

    // Live Official Locations (`official_locations`)
    private val _officialLocations = MutableStateFlow<List<OfficialLocation>>(emptyList())
    val officialLocations: StateFlow<List<OfficialLocation>> = _officialLocations.asStateFlow()

    // Real-time Notifications (`notifications`)
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // User Confirmed GPS Location & Assigned Barangay Jurisdiction
    private val _confirmedLocation = MutableStateFlow<LatLng?>(null)
    val confirmedLocation: StateFlow<LatLng?> = _confirmedLocation.asStateFlow()

    private val _confirmedBarangay = MutableStateFlow<Barangay?>(null)
    val confirmedBarangay: StateFlow<Barangay?> = _confirmedBarangay.asStateFlow()

    private val _isLocationConfirmed = MutableStateFlow(false)
    val isLocationConfirmed: StateFlow<Boolean> = _isLocationConfirmed.asStateFlow()

    // Role Switcher for Testing (Resident / Official)
    private val _currentRole = MutableStateFlow(AppRole.RESIDENT)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    fun setDeveloperRole(role: AppRole) {
        _currentRole.value = role
    }

    fun toggleDeveloperRole() {
        _currentRole.value = if (_currentRole.value == AppRole.RESIDENT) AppRole.OFFICIAL else AppRole.RESIDENT
    }

    // Push / In-App Notification alert state
    private val _latestResolvedNotification = MutableStateFlow<IncidentReport?>(null)
    val latestResolvedNotification: StateFlow<IncidentReport?> = _latestResolvedNotification.asStateFlow()

    fun dismissResolvedNotification() {
        _latestResolvedNotification.value = null
    }

    companion object {
        // Balanga City, Bataan Map Boundaries
        const val MAP_MIN_LAT = 14.6600
        const val MAP_MAX_LAT = 14.6950
        const val MAP_MIN_LNG = 120.5200
        const val MAP_MAX_LNG = 120.5600

        const val BALANGA_MIN_LAT = 14.6400
        const val BALANGA_MAX_LAT = 14.7100
        const val BALANGA_MIN_LNG = 120.5000
        const val BALANGA_MAX_LNG = 120.5700

        fun isWithinBalangaCity(latLng: LatLng): Boolean {
            return latLng.lat in BALANGA_MIN_LAT..BALANGA_MAX_LAT &&
                   latLng.lng in BALANGA_MIN_LNG..BALANGA_MAX_LNG
        }

        fun ratioToLatLng(xRatio: Float, yRatio: Float): LatLng {
            val lat = MAP_MAX_LAT - yRatio.coerceIn(0f, 1f) * (MAP_MAX_LAT - MAP_MIN_LAT)
            val lng = MAP_MIN_LNG + xRatio.coerceIn(0f, 1f) * (MAP_MAX_LNG - MAP_MIN_LNG)
            return LatLng(lat, lng)
        }

        fun latLngToRatio(latLng: LatLng): Pair<Float, Float> {
            val x = ((latLng.lng - MAP_MIN_LNG) / (MAP_MAX_LNG - MAP_MIN_LNG)).toFloat().coerceIn(0.05f, 0.95f)
            val y = ((MAP_MAX_LAT - latLng.lat) / (MAP_MAX_LAT - MAP_MIN_LAT)).toFloat().coerceIn(0.05f, 0.95f)
            return Pair(x, y)
        }
    }

    fun confirmUserLocation(latLng: LatLng, barangay: Barangay, address: String = "") {
        _confirmedLocation.value = latLng
        _confirmedBarangay.value = barangay
        _isLocationConfirmed.value = true
        _currentUser.value = _currentUser.value?.copy(
            homeBarangayId = barangay.id,
            address = address.ifBlank { "Registered in ${barangay.name}" }
        )
    }

    fun requestLocationReSelection() {
        _isLocationConfirmed.value = false
    }

    fun isPointInUserBarangay(xRatio: Float, yRatio: Float): Pair<Boolean, Barangay?> {
        val point = ratioToLatLng(xRatio, yRatio)
        val physical = findPhysicalBarangay(point)
        val assigned = _confirmedBarangay.value
        if (assigned == null) {
            return Pair(true, physical)
        }
        return Pair(physical?.id == assigned.id, physical)
    }

    private val _filterOnlyPending = MutableStateFlow(false)
    val filterOnlyPending: StateFlow<Boolean> = _filterOnlyPending.asStateFlow()
    val isPendingOnlyFilterActive: StateFlow<Boolean> = _filterOnlyPending.asStateFlow()

    fun toggleFilterOnlyPending() {
        _filterOnlyPending.value = !_filterOnlyPending.value
    }
    fun togglePendingOnlyFilter() = toggleFilterOnlyPending()

    /**
     * Submit Incident Report with real Firestore persistence and audit event log.
     */
    fun submitReport(
        title: String,
        category: ReportCategory,
        description: String,
        latLng: LatLng,
        address: String
    ) {
        val currentUid = currentAuthUid()
        val currentName = _currentUser.value?.name.orEmpty().ifBlank { "Resident" }
        val reportId = "rep_${System.currentTimeMillis()}"
        val (xRatio, yRatio) = latLngToRatio(latLng)

        val newReport = IncidentReport(
            id = reportId,
            title = title.ifBlank { "${category.displayName} Report" },
            category = category,
            animalType = if (category == ReportCategory.DANGEROUS_ANIMALS) AnimalType.SNAKE else AnimalType.OTHER_DANGEROUS,
            label = title.ifBlank { "${category.displayName} Reported" },
            description = description,
            locationName = address.ifBlank { "Neighborhood Sector" },
            latLng = latLng,
            xRatio = xRatio,
            yRatio = yRatio,
            status = ReportStatus.PENDING,
            residentId = currentUid,
            reportedBy = currentName,
            barangayId = _confirmedBarangay.value?.id ?: "sample_hall",
            timestamp = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )

        // Add to local state flow instantly
        _animalPins.value = listOf(newReport) + _animalPins.value

        // Log submitted event
        val event = ReportEvent(
            id = "evt_${System.currentTimeMillis()}_sub",
            reportId = reportId,
            type = "submitted",
            actorId = currentUid,
            actorName = currentName,
            actorRole = "RESIDENT",
            timestamp = System.currentTimeMillis(),
            note = "Submitted $title report at $address"
        )
        _reportEvents.value = listOf(event) + _reportEvents.value

        // Add notification for officials
        val notif = AppNotification(
            id = "notif_${System.currentTimeMillis()}",
            recipientId = "OFFICIAL",
            reportId = reportId,
            type = "new_report",
            title = "New report: ${category.displayName}",
            message = "${newReport.title} • ${address.ifBlank { "Neighborhood Sector" }} • just now",
            read = false,
            createdAt = System.currentTimeMillis()
        )
        _notifications.value = listOf(notif) + _notifications.value

        // Sync to Firestore
        if (firestore != null) {
            val reportMap = hashMapOf(
                "id" to reportId,
                "title" to newReport.title,
                "category" to category.name,
                "animalType" to newReport.animalType.name,
                "label" to newReport.label,
                "description" to description,
                "locationName" to newReport.locationName,
                "lat" to latLng.lat,
                "lng" to latLng.lng,
                "xRatio" to xRatio.toDouble(),
                "yRatio" to yRatio.toDouble(),
                "status" to "pending",
                "residentId" to currentUid,
                "reportedBy" to currentName,
                "barangayId" to newReport.barangayId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("reports").document(reportId).set(reportMap)
                .addOnFailureListener { e -> Log.w("PinProRepository", "Failed to sync report: ${e.message}") }

            val eventMap = hashMapOf(
                "id" to event.id,
                "reportId" to reportId,
                "type" to "submitted",
                "actorId" to currentUid,
                "actorName" to currentName,
                "actorRole" to "RESIDENT",
                "timestamp" to FieldValue.serverTimestamp(),
                "note" to event.note
            )
            firestore.collection("report_events").document(event.id).set(eventMap)

            val notifMap = hashMapOf(
                "id" to notif.id,
                "recipientId" to "OFFICIAL",
                "reportId" to reportId,
                "type" to notif.type,
                "title" to notif.title,
                "message" to notif.message,
                "read" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("notifications").document(notif.id).set(notifMap)
        }
    }

    fun addIncidentReport(
        category: ReportCategory,
        label: String,
        description: String,
        latLng: LatLng,
        locationName: String = "Neighborhood Sector"
    ) {
        submitReport(label, category, description, latLng, locationName)
    }

    fun addAnimalReportPin(
        animalType: AnimalType,
        label: String,
        description: String,
        xRatio: Float,
        yRatio: Float,
        locationName: String = "Spotted in Neighborhood"
    ) {
        val latLng = ratioToLatLng(xRatio, yRatio)
        submitReport(label, ReportCategory.DANGEROUS_ANIMALS, description, latLng, locationName)
    }

    fun addCyberPin(label: String, xRatio: Float, yRatio: Float, description: String = "Dangerous Incident") {
        val latLng = ratioToLatLng(xRatio, yRatio)
        submitReport(label, ReportCategory.FIGHTS, description, latLng, "Neighborhood Sector")
    }

    /**
     * Official Response Flow: Accept report -> status becomes ACCEPTED / ON_THE_WAY
     */
    fun acceptReport(reportId: String, officialName: String = "Officer Berto Aquino") {
        val officialId = currentAuthUid().ifBlank { "official_berto" }
        val report = _animalPins.value.firstOrNull { it.id == reportId } ?: return
        val startLoc = _confirmedBarangay.value?.hallLocation ?: LatLng(14.6762, 120.5375)
        val route = generateRoute(startLoc, report.latLng)

        val acceptedTime = System.currentTimeMillis()

        _animalPins.value = _animalPins.value.map {
            if (it.id == reportId) {
                it.copy(
                    status = ReportStatus.ACCEPTED,
                    acceptedAt = acceptedTime,
                    assignedOfficialId = officialId,
                    assignedOfficialName = officialName,
                    routeToIncident = route,
                    officialLocation = startLoc,
                    officialEtaMinutes = 5
                )
            } else it
        }

        // Write accepted event
        val event = ReportEvent(
            id = "evt_${System.currentTimeMillis()}_acc",
            reportId = reportId,
            type = "accepted",
            actorId = officialId,
            actorName = officialName,
            actorRole = "OFFICIAL",
            timestamp = acceptedTime,
            note = "Official $officialName accepted dispatch response"
        )
        _reportEvents.value = listOf(event) + _reportEvents.value

        // Notify Resident
        val notif = AppNotification(
            id = "notif_${System.currentTimeMillis()}",
            recipientId = report.residentId,
            reportId = reportId,
            type = "accepted",
            title = "Report Accepted",
            message = "Official $officialName accepted your report and is responding.",
            read = false,
            createdAt = System.currentTimeMillis()
        )
        _notifications.value = listOf(notif) + _notifications.value

        if (firestore != null) {
            firestore.collection("reports").document(reportId).update(
                "status", "accepted",
                "assignedOfficialId", officialId,
                "assignedOfficialName", officialName,
                "acceptedAt", FieldValue.serverTimestamp(),
                "updatedAt", FieldValue.serverTimestamp()
            )

            val eventMap = hashMapOf(
                "id" to event.id,
                "reportId" to reportId,
                "type" to "accepted",
                "actorId" to officialId,
                "actorName" to officialName,
                "actorRole" to "OFFICIAL",
                "timestamp" to FieldValue.serverTimestamp(),
                "note" to event.note
            )
            firestore.collection("report_events").document(event.id).set(eventMap)

            val notifMap = hashMapOf(
                "id" to notif.id,
                "recipientId" to report.residentId,
                "reportId" to reportId,
                "type" to notif.type,
                "title" to notif.title,
                "message" to notif.message,
                "read" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("notifications").document(notif.id).set(notifMap)
        }

        // Initialize Official Location
        updateOfficialLocation(
            officialId = officialId,
            name = officialName,
            lat = startLoc.lat,
            lng = startLoc.lng,
            heading = 0f,
            reportId = reportId,
            writeEvent = true
        )
    }

    /**
     * Update Official Live GPS location in `official_locations` and optionally log location_update event.
     */
    fun updateOfficialLocation(
        officialId: String,
        name: String,
        lat: Double,
        lng: Double,
        heading: Float = 0f,
        reportId: String? = null,
        writeEvent: Boolean = false
    ) {
        val updatedLoc = OfficialLocation(
            officialId = officialId,
            name = name,
            lat = lat,
            lng = lng,
            heading = heading,
            reportId = reportId,
            updatedAt = System.currentTimeMillis()
        )

        _officialLocations.value = _officialLocations.value.filter { it.officialId != officialId } + updatedLoc

        if (reportId != null) {
            _animalPins.value = _animalPins.value.map {
                if (it.id == reportId) {
                    val currentRoute = it.routeToIncident
                    val newRoute = if (currentRoute.isEmpty()) generateRoute(LatLng(lat, lng), it.latLng) else currentRoute
                    it.copy(
                        officialLocation = LatLng(lat, lng),
                        routeToIncident = newRoute,
                        status = if (it.status == ReportStatus.ACCEPTED) ReportStatus.ON_THE_WAY else it.status
                    )
                } else it
            }
        }

        if (firestore != null) {
            val locMap = hashMapOf(
                "officialId" to officialId,
                "name" to name,
                "lat" to lat,
                "lng" to lng,
                "heading" to heading,
                "reportId" to reportId,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("official_locations").document(officialId).set(locMap)

            if (writeEvent && reportId != null) {
                val eventId = "evt_${System.currentTimeMillis()}_loc"
                val eventMap = hashMapOf(
                    "id" to eventId,
                    "reportId" to reportId,
                    "type" to "location_update",
                    "actorId" to officialId,
                    "actorName" to name,
                    "actorRole" to "OFFICIAL",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "note" to "GPS coordinates updated: (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})"
                )
                firestore.collection("report_events").document(eventId).set(eventMap)
            }
        }
    }

    /**
     * Official Response Flow: Mark Arrived
     */
    fun markArrived(reportId: String, officialName: String = "Officer Berto Aquino") {
        val officialId = currentAuthUid().ifBlank { "official_berto" }
        val arrivedTime = System.currentTimeMillis()
        var residentUid = ""

        _animalPins.value = _animalPins.value.map {
            if (it.id == reportId) {
                residentUid = it.residentId
                it.copy(
                    status = ReportStatus.ARRIVED,
                    arrivedAt = arrivedTime,
                    officialEtaMinutes = 0
                )
            } else it
        }

        val event = ReportEvent(
            id = "evt_${System.currentTimeMillis()}_arr",
            reportId = reportId,
            type = "arrived",
            actorId = officialId,
            actorName = officialName,
            actorRole = "OFFICIAL",
            timestamp = arrivedTime,
            note = "Official $officialName arrived at the incident scene"
        )
        _reportEvents.value = listOf(event) + _reportEvents.value

        val notif = AppNotification(
            id = "notif_${System.currentTimeMillis()}",
            recipientId = residentUid,
            reportId = reportId,
            type = "arrived",
            title = "Official Arrived",
            message = "Official $officialName has arrived at your location.",
            read = false,
            createdAt = System.currentTimeMillis()
        )
        _notifications.value = listOf(notif) + _notifications.value

        if (firestore != null) {
            firestore.collection("reports").document(reportId).update(
                "status", "arrived",
                "arrivedAt", FieldValue.serverTimestamp(),
                "updatedAt", FieldValue.serverTimestamp()
            )

            val eventMap = hashMapOf(
                "id" to event.id,
                "reportId" to reportId,
                "type" to "arrived",
                "actorId" to officialId,
                "actorName" to officialName,
                "actorRole" to "OFFICIAL",
                "timestamp" to FieldValue.serverTimestamp(),
                "note" to event.note
            )
            firestore.collection("report_events").document(event.id).set(eventMap)

            val notifMap = hashMapOf(
                "id" to notif.id,
                "recipientId" to residentUid,
                "reportId" to reportId,
                "type" to notif.type,
                "title" to notif.title,
                "message" to notif.message,
                "read" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("notifications").document(notif.id).set(notifMap)
        }
    }

    /**
     * Official Response Flow: Resolve report with optional note
     */
    fun resolveReport(
        reportId: String,
        officialName: String = "Officer Berto Aquino",
        note: String? = null
    ) {
        val officialId = currentAuthUid().ifBlank { "official_berto" }
        val resolvedTime = System.currentTimeMillis()
        var resolvedReport: IncidentReport? = null

        _animalPins.value = _animalPins.value.map {
            if (it.id == reportId) {
                val updated = it.copy(
                    status = ReportStatus.RESOLVED,
                    resolvedAt = resolvedTime,
                    officialEtaMinutes = 0
                )
                resolvedReport = updated
                updated
            } else it
        }

        resolvedReport?.let {
            _latestResolvedNotification.value = it
        }

        val event = ReportEvent(
            id = "evt_${System.currentTimeMillis()}_res",
            reportId = reportId,
            type = "resolved",
            actorId = officialId,
            actorName = officialName,
            actorRole = "OFFICIAL",
            timestamp = resolvedTime,
            note = note ?: "Incident officially resolved and secured"
        )
        _reportEvents.value = listOf(event) + _reportEvents.value

        val residentUid = resolvedReport?.residentId.orEmpty()
        val notif = AppNotification(
            id = "notif_${System.currentTimeMillis()}",
            recipientId = residentUid,
            reportId = reportId,
            type = "resolved",
            title = "Report Resolved",
            message = "Your report has been marked resolved by $officialName.",
            read = false,
            createdAt = System.currentTimeMillis()
        )
        _notifications.value = listOf(notif) + _notifications.value

        if (firestore != null) {
            firestore.collection("reports").document(reportId).update(
                "status", "resolved",
                "resolvedAt", FieldValue.serverTimestamp(),
                "updatedAt", FieldValue.serverTimestamp()
            )

            val eventMap = hashMapOf(
                "id" to event.id,
                "reportId" to reportId,
                "type" to "resolved",
                "actorId" to officialId,
                "actorName" to officialName,
                "actorRole" to "OFFICIAL",
                "timestamp" to FieldValue.serverTimestamp(),
                "note" to event.note
            )
            firestore.collection("report_events").document(event.id).set(eventMap)

            val notifMap = hashMapOf(
                "id" to notif.id,
                "recipientId" to residentUid,
                "reportId" to reportId,
                "type" to notif.type,
                "title" to notif.title,
                "message" to notif.message,
                "read" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("notifications").document(notif.id).set(notifMap)

            // Clear reportId from official_locations
            firestore.collection("official_locations").document(officialId).update("reportId", null)
        }
    }

    fun markPinAsDone(id: String) {
        resolveReport(id)
    }

    fun markPinAsPending(id: String) {
        _animalPins.value = _animalPins.value.map {
            if (it.id == id) it.copy(status = ReportStatus.PENDING, resolvedAt = null, routeToIncident = emptyList(), officialLocation = null, officialEtaMinutes = null) else it
        }
        firestore?.collection("reports")?.document(id)?.update(
            "status", "pending",
            "updatedAt", FieldValue.serverTimestamp()
        )
    }

    fun togglePinStatus(id: String) {
        val target = _animalPins.value.firstOrNull { it.id == id } ?: return
        if (target.status == ReportStatus.PENDING) {
            acceptReport(id)
        } else if (target.status == ReportStatus.ACCEPTED || target.status == ReportStatus.ON_THE_WAY) {
            markArrived(id)
        } else if (target.status == ReportStatus.ARRIVED) {
            resolveReport(id)
        } else {
            markPinAsPending(id)
        }
    }

    fun removeCyberPin(id: String) {
        _animalPins.value = _animalPins.value.filter { it.id != id }
        firestore?.collection("reports")?.document(id)?.delete()
    }

    private var movementStepIndex = 0

    /**
     * Simulate Movement Button logic for Officials testing en-route tracking.
     * Moves official's marker along route toward incident in steps.
     */
    fun simulateMovementStep(reportId: String) {
        val report = _animalPins.value.firstOrNull { it.id == reportId } ?: return
        val route = report.routeToIncident.ifEmpty {
            val startLoc = _confirmedBarangay.value?.hallLocation ?: LatLng(14.6762, 120.5375)
            generateRoute(startLoc, report.latLng)
        }
        if (route.isEmpty()) return

        movementStepIndex = (movementStepIndex + 1) % route.size
        val nextLoc = route[movementStepIndex]
        val officialName = _currentUser.value?.name ?: "Officer Berto Aquino"
        val officialId = currentAuthUid().ifBlank { "official_berto" }

        val isLastStep = (movementStepIndex == route.size - 1)

        updateOfficialLocation(
            officialId = officialId,
            name = officialName,
            lat = nextLoc.lat,
            lng = nextLoc.lng,
            heading = 45f,
            reportId = reportId,
            writeEvent = (movementStepIndex % 3 == 0 || isLastStep)
        )

        if (isLastStep) {
            markArrived(reportId, officialName)
        }
    }

    fun notifyResidentOnTheWay(reportId: String, officialName: String = "Officer Berto Aquino", etaMinutes: Int = 3) {
        val report = _animalPins.value.firstOrNull { it.id == reportId } ?: return
        val residentUid = report.residentId
        val existingNotif = _notifications.value.firstOrNull { it.reportId == reportId && it.type == "on_the_way" }
        if (existingNotif != null) return

        val notif = AppNotification(
            id = "notif_${System.currentTimeMillis()}_otw",
            recipientId = residentUid,
            reportId = reportId,
            type = "on_the_way",
            title = "Official On The Way",
            message = "Official $officialName is on the way. ETA $etaMinutes mins.",
            read = false,
            createdAt = System.currentTimeMillis()
        )
        _notifications.value = listOf(notif) + _notifications.value

        if (firestore != null) {
            val notifMap = hashMapOf(
                "id" to notif.id,
                "recipientId" to residentUid,
                "reportId" to reportId,
                "type" to notif.type,
                "title" to notif.title,
                "message" to notif.message,
                "read" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("notifications").document(notif.id).set(notifMap)
        }
    }

    fun markNotificationRead(notificationId: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(read = true) else it
        }
        firestore?.collection("notifications")?.document(notificationId)?.update("read", true)
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(read = true) }
        firestore?.collection("notifications")?.get()?.addOnSuccessListener { snapshot ->
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "read", true)
            }
            batch.commit()
        }
    }

    private fun currentAuthUid(): String {
        return if (firestore != null) {
            try {
                Firebase.auth.currentUser?.uid ?: _currentUser.value?.uid.orEmpty()
            } catch (_: Exception) {
                _currentUser.value?.uid.orEmpty()
            }
        } else {
            _currentUser.value?.uid.orEmpty()
        }
    }

    init {
        seedDemoData()
        listenToFirestore()
    }

    private fun listenToFirestore() {
        if (firestore == null) return
        try {
            // 1. Real-time reports listener
            firestore.collection("reports").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("PinProRepository", "Firestore reports listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reportsList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val catStr = doc.getString("category") ?: "FIGHTS"
                            val category = try { ReportCategory.valueOf(catStr.uppercase()) } catch (_: Exception) { ReportCategory.FIGHTS }
                            val statusStr = doc.getString("status") ?: "pending"
                            val status = ReportStatus.fromCode(statusStr)

                            val lat = doc.getDouble("lat") ?: 14.6762
                            val lng = doc.getDouble("lng") ?: 120.5375
                            val incidentLoc = LatLng(lat, lng)
                            val (calcX, calcY) = latLngToRatio(incidentLoc)

                            IncidentReport(
                                id = doc.id,
                                title = doc.getString("title") ?: doc.getString("label") ?: "${category.displayName} Report",
                                category = category,
                                animalType = AnimalType.OTHER_DANGEROUS,
                                label = doc.getString("label") ?: doc.getString("title") ?: "${category.displayName} Reported",
                                description = doc.getString("description") ?: "",
                                locationName = doc.getString("locationName") ?: doc.getString("address") ?: "Neighborhood Sector",
                                latLng = incidentLoc,
                                xRatio = (doc.getDouble("xRatio")?.toFloat() ?: calcX),
                                yRatio = (doc.getDouble("yRatio")?.toFloat() ?: calcY),
                                status = status,
                                residentId = doc.getString("residentId") ?: "",
                                reportedBy = doc.getString("reportedBy") ?: doc.getString("residentName") ?: "Resident",
                                barangayId = doc.getString("barangayId") ?: "sample_hall",
                                assignedOfficialId = doc.getString("assignedOfficialId"),
                                assignedOfficialName = doc.getString("assignedOfficialName"),
                                createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                                timestamp = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _animalPins.value = reportsList
                }
            }

            // 2. Real-time report_events listener
            firestore.collection("report_events").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val eventsList = snapshot.documents.mapNotNull { doc ->
                        try {
                            ReportEvent(
                                id = doc.id,
                                reportId = doc.getString("reportId") ?: "",
                                type = doc.getString("type") ?: "submitted",
                                actorId = doc.getString("actorId") ?: "",
                                actorName = doc.getString("actorName") ?: "User",
                                actorRole = doc.getString("actorRole") ?: "RESIDENT",
                                timestamp = doc.getTimestamp("timestamp")?.toDate()?.time ?: System.currentTimeMillis(),
                                note = doc.getString("note")
                            )
                        } catch (_: Exception) { null }
                    }.sortedByDescending { it.timestamp }
                    _reportEvents.value = eventsList
                }
            }

            // 3. Real-time official_locations listener
            firestore.collection("official_locations").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val locList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val lat = doc.getDouble("lat") ?: return@mapNotNull null
                            val lng = doc.getDouble("lng") ?: return@mapNotNull null
                            OfficialLocation(
                                officialId = doc.id,
                                name = doc.getString("name") ?: "Official",
                                lat = lat,
                                lng = lng,
                                heading = doc.getDouble("heading")?.toFloat() ?: 0f,
                                reportId = doc.getString("reportId"),
                                updatedAt = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
                            )
                        } catch (_: Exception) { null }
                    }
                    _officialLocations.value = locList
                }
            }

            // 4. Real-time notifications listener
            firestore.collection("notifications").addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val notifList = snapshot.documents.mapNotNull { doc ->
                        try {
                            AppNotification(
                                id = doc.id,
                                recipientId = doc.getString("recipientId") ?: "OFFICIAL",
                                reportId = doc.getString("reportId") ?: "",
                                type = doc.getString("type") ?: "new_report",
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                read = doc.getBoolean("read") ?: false,
                                createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                            )
                        } catch (_: Exception) { null }
                    }.sortedByDescending { it.createdAt }
                    _notifications.value = notifList
                }
            }
        } catch (e: Exception) {
            Log.w("PinProRepository", "Error setting up Firestore listeners", e)
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
        _isLocationConfirmed.value = false
        _confirmedLocation.value = null
        _confirmedBarangay.value = null
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

    fun findPhysicalBarangay(point: LatLng): Barangay? {
        return _barangays.value.firstOrNull { barangay ->
            isPointInPolygon(point, barangay.boundaryPolygon)
        }
    }

    private fun generateRoute(start: LatLng, end: LatLng): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val totalSteps = 16
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
     * Seeds barangays and user accounts (0 mock reports - clean Firestore storage).
     */
    fun seedDemoData() {
        // 1. Clean reports list (Zero mock reports)
        _animalPins.value = emptyList()

        // 2. Barangays in Balanga City, Bataan
        val bSampleHall = Barangay(
            id = "sample_hall",
            name = "Brgy. Poblacion, Balanga City",
            hallLocation = LatLng(14.6762, 120.5375),
            hallContact = "(047) 237-1234 / 0917-888-BLNG",
            boundaryPolygon = listOf(
                LatLng(14.6900, 120.5200),
                LatLng(14.6900, 120.5400),
                LatLng(14.6650, 120.5400),
                LatLng(14.6650, 120.5200)
            )
        )

        val bRiverside = Barangay(
            id = "riverside",
            name = "Brgy. San Jose, Balanga City",
            hallLocation = LatLng(14.6790, 120.5450),
            hallContact = "(047) 237-5678 / 0918-999-BLNG",
            boundaryPolygon = listOf(
                LatLng(14.6900, 120.5400),
                LatLng(14.6900, 120.5600),
                LatLng(14.6650, 120.5600),
                LatLng(14.6650, 120.5400)
            )
        )
        _barangays.value = listOf(bSampleHall, bRiverside)

        // 3. Demo Users
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
                uid = "tanod_berto",
                role = UserRole.TANOD,
                name = "Berto Aquino",
                mobile = "+63 921 555 1201",
                officialId = "TND-SH-04",
                status = UserStatus.ACTIVE,
                address = "Barangay Outpost 1",
                homeBarangayId = "sample_hall",
                barangayId = "sample_hall"
            )
        )
        _users.value = seededUsers
    }

    // Helper methods for Legacy Admin / Tanod / Resident screens
    fun assignTanod(alertId: String, tanodId: String) {
        acceptReport(alertId, "Officer Tanod")
    }

    fun updateAlertStatus(alertId: String, status: AlertStatus) {
        when (status) {
            AlertStatus.ASSIGNED, AlertStatus.ON_THE_WAY -> acceptReport(alertId)
            AlertStatus.ARRIVED -> markArrived(alertId)
            AlertStatus.RESOLVED -> resolveReport(alertId)
            AlertStatus.CANCELLED -> removeCyberPin(alertId)
            else -> togglePinStatus(alertId)
        }
    }

    fun simulateIncomingAlert(category: AlertCategory = AlertCategory.CRIME) {
        submitReport("Emergency Siren Alert", ReportCategory.FIGHTS, "Urgent assistance requested", ratioToLatLng(0.5f, 0.5f), "Poblacion Central")
    }

    fun setResidentStatus(residentId: String, status: UserStatus) {
        _users.value = _users.value.map { if (it.uid == residentId) it.copy(status = status) else it }
    }

    fun toggleTanodAvailability(tanodId: String) {
        _tanods.value = _tanods.value.map {
            if (it.uid == tanodId) {
                val next = if (it.availability == TanodAvailability.AVAILABLE) TanodAvailability.OFF_DUTY else TanodAvailability.AVAILABLE
                it.copy(availability = next)
            } else it
        }
    }

    fun addTanod(name: String, mobile: String, officialId: String) {
        val newTanod = Tanod(
            uid = "tnd_${System.currentTimeMillis()}",
            name = name,
            contact = mobile,
            badgeNumber = officialId,
            barangayId = _confirmedBarangay.value?.id ?: "sample_hall",
            availability = TanodAvailability.AVAILABLE
        )
        _tanods.value = _tanods.value + newTanod
    }

    fun cancelAlert(alertId: String) {
        removeCyberPin(alertId)
    }

    fun rateAlert(alertId: String, rating: Int, feedback: String? = null) {
        // No-op or log feedback
    }

    fun createAlert(category: AlertCategory, description: String, location: LatLng, locationName: String) {
        val repCat = when (category) {
            AlertCategory.MEDICAL -> ReportCategory.DANGEROUS_ANIMALS
            AlertCategory.FIRE -> ReportCategory.FOUL_SMELL
            AlertCategory.CRIME -> ReportCategory.FIGHTS
            AlertCategory.FLOOD -> ReportCategory.TRASH
            AlertCategory.OTHER -> ReportCategory.NOISE_COMPLAINT
        }
        submitReport("Community Alert", repCat, description, location, locationName)
    }

    fun createAlert(
        resident: User? = null,
        category: AlertCategory = AlertCategory.OTHER,
        note: String = "",
        photoUrl: String? = null,
        location: AlertLocation = AlertLocation(),
        shareLiveLocation: Boolean = true
    ) {
        createAlert(category, note, LatLng(location.lat, location.lng), location.address)
    }
}

