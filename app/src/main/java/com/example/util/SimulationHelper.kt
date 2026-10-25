package com.example.util

import com.example.data.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt
import kotlin.math.ceil

data class InterpolatedRouteState(
    val currentLocation: LatLng,
    val heading: Float,
    val remainingRoute: List<LatLng>,
    val remainingDistanceMeters: Double,
    val remainingDistanceKm: Double,
    val etaMinutes: Int,
    val isArrived: Boolean
)

object SimulationHelper {

    /**
     * Calculates distance in meters between two LatLng coordinates using Equirectangular approximation.
     */
    fun calculateDistanceMeters(p1: LatLng, p2: LatLng): Double {
        val latAvg = Math.toRadians((p1.lat + p2.lat) / 2.0)
        val dLat = Math.toRadians(p2.lat - p1.lat) * 6371000.0
        val dLng = Math.toRadians(p2.lng - p1.lng) * 6371000.0 * cos(latAvg)
        return sqrt(dLat * dLat + dLng * dLng)
    }

    /**
     * Calculates bearing in degrees (0..360) from p1 to p2.
     */
    fun calculateBearing(p1: LatLng, p2: LatLng): Float {
        val lat1 = Math.toRadians(p1.lat)
        val lat2 = Math.toRadians(p2.lat)
        val dLng = Math.toRadians(p2.lng - p1.lng)

        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return ((bearingDeg + 360.0) % 360.0).toFloat()
    }

    /**
     * Shortest angular difference between two heading angles in degrees (-180..180).
     */
    fun shortestAngleDiff(current: Float, target: Float): Float {
        return ((target - current + 540f) % 360f) - 180f
    }

    /**
     * Calculates total distance along a polyline in meters.
     */
    fun calculatePolylineDistanceMeters(route: List<LatLng>): Double {
        if (route.size < 2) return 0.0
        var total = 0.0
        for (i in 0 until route.size - 1) {
            total += calculateDistanceMeters(route[i], route[i + 1])
        }
        return total
    }

    /**
     * Interpolates position, heading, remaining trimmed route polyline, and ETA
     * along the given polyline for the distance traveled in meters.
     */
    fun interpolateRoutePosition(
        fullRoute: List<LatLng>,
        traveledMeters: Double,
        speedMetersPerSec: Double = 9.72 // ~35 km/h base speed
    ): InterpolatedRouteState {
        if (fullRoute.isEmpty()) {
            return InterpolatedRouteState(
                currentLocation = LatLng(14.6762, 120.5375),
                heading = 0f,
                remainingRoute = emptyList(),
                remainingDistanceMeters = 0.0,
                remainingDistanceKm = 0.0,
                etaMinutes = 0,
                isArrived = true
            )
        }

        if (fullRoute.size == 1) {
            return InterpolatedRouteState(
                currentLocation = fullRoute.first(),
                heading = 0f,
                remainingRoute = fullRoute,
                remainingDistanceMeters = 0.0,
                remainingDistanceKm = 0.0,
                etaMinutes = 0,
                isArrived = true
            )
        }

        val segmentDistances = mutableListOf<Double>()
        var totalDistance = 0.0
        for (i in 0 until fullRoute.size - 1) {
            val dist = calculateDistanceMeters(fullRoute[i], fullRoute[i + 1])
            segmentDistances.add(dist)
            totalDistance += dist
        }

        if (traveledMeters >= totalDistance - 1.0) {
            val lastPt = fullRoute.last()
            val secondLastPt = fullRoute[fullRoute.size - 2]
            val finalHeading = calculateBearing(secondLastPt, lastPt)
            return InterpolatedRouteState(
                currentLocation = lastPt,
                heading = finalHeading,
                remainingRoute = listOf(lastPt),
                remainingDistanceMeters = 0.0,
                remainingDistanceKm = 0.0,
                etaMinutes = 0,
                isArrived = true
            )
        }

        var accumulated = 0.0
        for (i in 0 until segmentDistances.size) {
            val segLen = segmentDistances[i]
            if (traveledMeters <= accumulated + segLen) {
                val segTraveled = (traveledMeters - accumulated).coerceAtLeast(0.0)
                val fraction = if (segLen > 0.0001) (segTraveled / segLen).coerceIn(0.0, 1.0) else 1.0

                val pStart = fullRoute[i]
                val pEnd = fullRoute[i + 1]

                val interpolatedLat = pStart.lat + fraction * (pEnd.lat - pStart.lat)
                val interpolatedLng = pStart.lng + fraction * (pEnd.lng - pStart.lng)
                val currentPt = LatLng(interpolatedLat, interpolatedLng)

                val heading = calculateBearing(pStart, pEnd)

                val remainingPts = mutableListOf<LatLng>()
                remainingPts.add(currentPt)
                for (j in (i + 1) until fullRoute.size) {
                    remainingPts.add(fullRoute[j])
                }

                val remainingMeters = (totalDistance - traveledMeters).coerceAtLeast(0.0)
                val remainingKm = remainingMeters / 1000.0
                val speedKmH = (speedMetersPerSec * 3.6).coerceAtLeast(10.0)
                val etaMin = ceil(remainingKm / speedKmH * 60.0).toInt().coerceAtLeast(1)

                return InterpolatedRouteState(
                    currentLocation = currentPt,
                    heading = heading,
                    remainingRoute = remainingPts,
                    remainingDistanceMeters = remainingMeters,
                    remainingDistanceKm = remainingKm,
                    etaMinutes = etaMin,
                    isArrived = remainingMeters <= 5.0
                )
            }
            accumulated += segLen
        }

        val lastPt = fullRoute.last()
        return InterpolatedRouteState(
            currentLocation = lastPt,
            heading = 0f,
            remainingRoute = listOf(lastPt),
            remainingDistanceMeters = 0.0,
            remainingDistanceKm = 0.0,
            etaMinutes = 0,
            isArrived = true
        )
    }
}
