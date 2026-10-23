package com.example.data.model

import java.io.Serializable

data class LatLng(
    val lat: Double = 0.0,
    val lng: Double = 0.0
) : Serializable

enum class UserRole {
    RESIDENT,
    ADMIN,
    TANOD
}

enum class UserStatus {
    ACTIVE,
    SUSPENDED
}

data class User(
    val uid: String = "",
    val role: UserRole = UserRole.RESIDENT,
    val name: String = "",
    val mobile: String = "",
    val officialId: String? = null,
    val status: UserStatus = UserStatus.ACTIVE,
    val address: String = "",
    val homeBarangayId: String = "",
    val barangayId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Barangay(
    val id: String = "",
    val name: String = "",
    val hallLocation: LatLng = LatLng(),
    val hallContact: String = "",
    val boundaryPolygon: List<LatLng> = emptyList()
)

enum class TanodAvailability {
    AVAILABLE,
    BUSY,
    OFF_DUTY
}

data class Tanod(
    val uid: String = "",
    val barangayId: String = "",
    val name: String = "",
    val badgeNumber: String = "",
    val availability: TanodAvailability = TanodAvailability.AVAILABLE,
    val currentLocation: LatLng = LatLng(),
    val responseCountToday: Int = 0,
    val contact: String = "0917-555-0199"
)

enum class AlertCategory(val displayName: String) {
    MEDICAL("Medical"),
    FIRE("Fire"),
    CRIME("Crime / Peace and Order"),
    FLOOD("Flood / Disaster"),
    OTHER("Other Complaint")
}

enum class AlertStatus(val label: String) {
    RECEIVED("Received"),
    ASSIGNED("Tanod Assigned"),
    ON_THE_WAY("On the Way"),
    ARRIVED("Arrived"),
    RESOLVED("Resolved"),
    CANCELLED("Cancelled")
}

data class AlertLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = ""
)

data class Alert(
    val id: String = "",
    val residentId: String = "",
    val residentName: String = "",
    val residentMobile: String = "",
    val receivingBarangayId: String = "",
    val fromOutsideHome: Boolean = false,
    val category: AlertCategory = AlertCategory.MEDICAL,
    val note: String = "",
    val photoUrl: String? = null,
    val location: AlertLocation = AlertLocation(),
    val shareLiveLocation: Boolean = true,
    val status: AlertStatus = AlertStatus.RECEIVED,
    val assignedTanodId: String? = null,
    val etaMinutes: Int? = null,
    val route: List<LatLng> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val assignedAt: Long? = null,
    val arrivedAt: Long? = null,
    val resolvedAt: Long? = null,
    val rating: Int? = null
)

enum class DemoLocationOverride(val label: String, val latLng: LatLng, val defaultBarangayId: String?) {
    SAMPLE_HALL("Inside Barangay Sample Hall", LatLng(14.6535, 121.0500), "sample_hall"),
    RIVERSIDE("Inside Barangay Riverside", LatLng(14.6535, 121.0600), "riverside"),
    OUTSIDE_BOTH("Outside both barangays", LatLng(14.6400, 121.0400), null),
    REAL_GPS("Use real GPS (Simulated Manila)", LatLng(14.6530, 121.0510), "sample_hall")
}

/**
 * Standard Ray-casting algorithm for Point-In-Polygon detection.
 * Used to determine which Barangay territory a resident is physically within.
 */
fun isPointInPolygon(point: LatLng, polygon: List<LatLng>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val pi = polygon[i]
        val pj = polygon[j]
        if ((pi.lat > point.lat) != (pj.lat > point.lat) &&
            point.lng < (pj.lng - pi.lng) * (point.lat - pi.lat) / (pj.lat - pi.lat) + pi.lng
        ) {
            inside = !inside
        }
        j = i
    }
    return inside
}

enum class AnimalType(val displayName: String, val emoji: String) {
    SNAKE("Snake", "🐍"),
    AGGRESSIVE_DOG("Aggressive Dog", "🐕"),
    RABID_ANIMAL("Rabid Animal", "⚠️"),
    VENOMOUS_CREATURE("Venomous Creature", "🦂"),
    STRAY_PACK("Stray Pack", "🐾"),
    OTHER_DANGEROUS("Other Dangerous Animal", "⚠️")
}

enum class PinStatus(val label: String) {
    PENDING("Pending"),
    DONE("Done")
}

data class AnimalReportPin(
    val id: String = "",
    val animalType: AnimalType = AnimalType.SNAKE,
    val label: String = "",
    val description: String = "",
    val locationName: String = "",
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val status: PinStatus = PinStatus.PENDING,
    val reportedBy: String = "Resident",
    val timestamp: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null,
    val isPrimary: Boolean = false,
    val category: String = "Animal Alert"
)

typealias CyberPin = AnimalReportPin

enum class PinProTab {
    HOME,
    MAP,
    SEARCH,
    PROFILE
}

typealias CyberzoneTab = PinProTab


