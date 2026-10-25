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

/**
 * Temporary developer option role allowing role switching on the same device.
 * Easy to remove before production release.
 */
enum class AppRole(val title: String) {
    RESIDENT("Resident"),
    OFFICIAL("Barangay Official")
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

/**
 * The 5 core incident categories required for PinPro:
 * 1. Fights (🥊)
 * 2. Dangerous animals (snake, dog, etc.) (🐍 / 🐕)
 * 3. Noise complaint (🔊)
 * 4. Foul smell in the area (💨)
 * 5. Trash (🗑️)
 */
enum class ReportCategory(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    FIGHTS("Fights", "🥊", "Street fights, brawls, and physical altercations"),
    DANGEROUS_ANIMALS("Dangerous Animals", "🐍", "Venomous snakes, aggressive dogs, or rabid animals"),
    NOISE_COMPLAINT("Noise Complaint", "🔊", "Loud music, karaoke, or late-night disturbance"),
    FOUL_SMELL("Foul Smell in the Area", "💨", "Pungent odor, burning rubber, toxic fumes, or waste stench"),
    TRASH("Trash", "🗑️", "Illegal dumpsite, garbage pile, or uncollected waste")
}

enum class AnimalType(val displayName: String, val emoji: String) {
    SNAKE("Snake", "🐍"),
    AGGRESSIVE_DOG("Aggressive Dog", "🐕"),
    RABID_ANIMAL("Rabid Animal", "⚠️"),
    VENOMOUS_CREATURE("Venomous Creature", "🦂"),
    STRAY_PACK("Stray Pack", "🐾"),
    OTHER_DANGEROUS("Other Dangerous Animal", "⚠️")
}

/**
 * Official response status flow:
 * PENDING -> ON_THE_WAY -> RESOLVED
 */
enum class ReportStatus(val label: String, val code: String) {
    PENDING("Pending", "pending"),
    ACCEPTED("Accepted", "accepted"),
    ON_THE_WAY("On the way", "on_the_way"),
    ARRIVED("Arrived", "arrived"),
    RESOLVED("Resolved", "resolved");

    companion object {
        val DONE = RESOLVED
        fun fromCode(code: String): ReportStatus {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
                ?: PENDING
        }
    }
}

typealias PinStatus = ReportStatus

data class IncidentReport(
    val id: String = "",
    val title: String = "",
    val category: ReportCategory = ReportCategory.FIGHTS,
    val animalType: AnimalType = AnimalType.OTHER_DANGEROUS,
    val label: String = "",
    val description: String = "",
    val locationName: String = "",
    val latLng: LatLng = LatLng(14.6762, 120.5375),
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val status: ReportStatus = ReportStatus.PENDING,
    val residentId: String = "",
    val reportedBy: String = "Resident",
    val barangayId: String = "sample_hall",
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val arrivedAt: Long? = null,
    val resolvedAt: Long? = null,
    val assignedOfficialId: String? = null,
    val assignedOfficialName: String? = null,
    val officialLocation: LatLng? = null,
    val officialHeading: Float = 0f,
    val officialEtaMinutes: Int? = null,
    val routeToIncident: List<LatLng> = emptyList(),
    val isPrimary: Boolean = false
)

typealias AnimalReportPin = IncidentReport
typealias CyberPin = IncidentReport

data class ReportEvent(
    val id: String = "",
    val reportId: String = "",
    val type: String = "submitted", // submitted | accepted | on_the_way | location_update | arrived | resolved
    val actorId: String = "",
    val actorName: String = "",
    val actorRole: String = "RESIDENT",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

data class OfficialLocation(
    val officialId: String = "",
    val name: String = "",
    val lat: Double = 14.6762,
    val lng: Double = 120.5375,
    val heading: Float = 0f,
    val reportId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class AppNotification(
    val id: String = "",
    val recipientId: String = "OFFICIAL", // OFFICIAL or specific userId
    val reportId: String = "",
    val type: String = "new_report", // new_report | accepted | on_the_way | arrived | resolved
    val title: String = "",
    val message: String = "",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Backward-compatible Alert models for Admin / Tanod legacy modules
enum class AlertCategory(val displayName: String, val emoji: String) {
    MEDICAL("Medical Emergency", "🚑"),
    FIRE("Fire Incident", "🔥"),
    CRIME("Peace & Order / Crime", "🚨"),
    FLOOD("Flood / Calamity", "🌊"),
    OTHER("General Assistance", "⚠️")
}

enum class AlertStatus(val label: String) {
    RECEIVED("Received"),
    ASSIGNED("Assigned"),
    ON_THE_WAY("On the way"),
    ARRIVED("Arrived"),
    RESOLVED("Resolved"),
    CANCELLED("Cancelled")
}

data class AlertLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val address: String = ""
) : Serializable

data class Alert(
    val id: String = "",
    val residentId: String = "",
    val residentName: String = "",
    val residentMobile: String = "",
    val receivingBarangayId: String = "",
    val fromOutsideHome: Boolean = false,
    val category: AlertCategory = AlertCategory.OTHER,
    val note: String = "",
    val photoUrl: String? = null,
    val location: AlertLocation = AlertLocation(),
    val shareLiveLocation: Boolean = true,
    val status: AlertStatus = AlertStatus.RECEIVED,
    val assignedTanodId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val assignedAt: Long? = null,
    val arrivedAt: Long? = null,
    val resolvedAt: Long? = null,
    val etaMinutes: Int? = null,
    val route: List<LatLng> = emptyList(),
    val rating: Int? = null
)

enum class TanodAvailability(val label: String) {
    AVAILABLE("Available"),
    BUSY("Responding"),
    OFF_DUTY("Off Duty")
}

data class Tanod(
    val uid: String = "",
    val barangayId: String = "",
    val name: String = "",
    val badgeNumber: String = "",
    val availability: TanodAvailability = TanodAvailability.AVAILABLE,
    val currentLocation: LatLng = LatLng(),
    val responseCountToday: Int = 0,
    val contact: String = ""
)

enum class DemoLocationOverride(val label: String, val latLng: LatLng) {
    SAMPLE_HALL("Inside Barangay Sample Hall", LatLng(14.6538, 121.0505)),
    RIVERSIDE("Inside Barangay Riverside", LatLng(14.6538, 121.0610)),
    BORDER_DISPUTED("Border Disputed Sector", LatLng(14.6538, 121.0550))
}

/**
 * Standard Ray-casting algorithm for Point-In-Polygon territory detection.
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

enum class PinProTab {
    HOME,
    MAP,
    SEARCH,
    PROFILE
}

typealias CyberzoneTab = PinProTab
