package com.example.ui.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Alert
import com.example.data.model.Barangay
import com.example.data.model.LatLng
import com.example.data.model.Tanod
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PinProBorder
import com.example.ui.theme.PinProNeonGreen
import com.example.ui.theme.PinProNeonGreenSubtle
import com.example.ui.theme.PinProSurface
import com.example.ui.theme.PinProTextMuted
import com.example.ui.theme.PinProTextPrimary

/**
 * High-performance vector map canvas customized for PinPro Philippines.
 * Renders barangay boundaries, street grid, emergency halls, live tanods,
 * user pin, and Grab/Lalamove neon green route polyline with dark border.
 */
@Composable
fun PinProMapView(
    barangays: List<Barangay>,
    activeBarangayId: String?,
    userLocation: LatLng?,
    assignedTanod: Tanod? = null,
    activeAlert: Alert? = null,
    allTanods: List<Tanod> = emptyList(),
    allAlerts: List<Alert> = emptyList(),
    onAlertSelected: ((Alert) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Coordinate bounding box for Pasig/Manila area
    // lat: 14.6460 to 14.6610 (height: 0.015)
    // lng: 121.0420 to 121.0680 (width: 0.026)
    val minLat = 14.6460
    val maxLat = 14.6610
    val minLng = 121.0420
    val maxLng = 121.0680

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.8f, 3.5f)
                    panOffset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            fun latLngToScreen(latLng: LatLng): Offset {
                val xNorm = ((latLng.lng - minLng) / (maxLng - minLng)).toFloat()
                // Invert Y because latitude increases upwards
                val yNorm = (1f - ((latLng.lat - minLat) / (maxLat - minLat))).toFloat()

                val basePoint = Offset(xNorm * width, yNorm * height)
                val center = Offset(width / 2f, height / 2f)
                return (basePoint - center) * zoomScale + center + panOffset
            }

            // 1. Draw subtle street grid & water line
            val riverY = height * 0.72f
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(0f, riverY),
                end = Offset(width, riverY - 30f),
                strokeWidth = 14f * zoomScale,
                cap = StrokeCap.Round
            )

            // Street lines
            val streetColor = Color(0xFFE5E7EB)
            for (step in 1..7) {
                val y = height * (step / 8f)
                drawLine(
                    color = streetColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 2.5f * zoomScale
                )
            }
            for (step in 1..9) {
                val x = width * (step / 10f)
                drawLine(
                    color = streetColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 2.5f * zoomScale
                )
            }

            // 2. Draw Barangay Boundary Polygons
            barangays.forEach { barangay ->
                val isActive = barangay.id == activeBarangayId
                if (barangay.boundaryPolygon.size >= 3) {
                    val path = Path()
                    val first = latLngToScreen(barangay.boundaryPolygon.first())
                    path.moveTo(first.x, first.y)
                    for (i in 1 until barangay.boundaryPolygon.size) {
                        val pt = latLngToScreen(barangay.boundaryPolygon[i])
                        path.lineTo(pt.x, pt.y)
                    }
                    path.close()

                    // Subtle background tint for active barangay
                    if (isActive) {
                        drawPath(
                            path = path,
                            color = PinProNeonGreenSubtle
                        )
                    }

                    // Dashed boundary stroke
                    drawPath(
                        path = path,
                        color = if (isActive) Color(0xFF0B0B0B) else Color(0xFF9CA3AF),
                        style = Stroke(
                            width = if (isActive) 2.5f * zoomScale else 1.5f * zoomScale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )

                    // Barangay Hall Pin
                    val hallScreen = latLngToScreen(barangay.hallLocation)
                    // Hall footprint badge
                    drawCircle(
                        color = Color(0xFF0B0B0B),
                        radius = 7f * zoomScale,
                        center = hallScreen
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f * zoomScale,
                        center = hallScreen
                    )
                }
            }

            // 3. Draw Grab/Lalamove Route Line (Neon Green with thin dark outline)
            val route = activeAlert?.route ?: emptyList()
            if (route.size >= 2) {
                val routePath = Path()
                val startScreen = latLngToScreen(route.first())
                routePath.moveTo(startScreen.x, startScreen.y)
                for (i in 1 until route.size) {
                    val p = latLngToScreen(route[i])
                    routePath.lineTo(p.x, p.y)
                }

                // Dark outline for contrast on light background
                drawPath(
                    path = routePath,
                    color = Color(0xFF0B0B0B),
                    style = Stroke(
                        width = 8f * zoomScale,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Neon Green fill (3-4px line)
                drawPath(
                    path = routePath,
                    color = PinProNeonGreen,
                    style = Stroke(
                        width = 5.5f * zoomScale,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 4. Draw Alerts Pins (for Admin map overview)
            allAlerts.forEach { alert ->
                val isSelected = alert.id == activeAlert?.id
                val alertScreen = latLngToScreen(LatLng(alert.location.lat, alert.location.lng))

                if (isSelected) {
                    drawCircle(
                        color = PinProNeonGreen,
                        radius = 10f * zoomScale,
                        center = alertScreen
                    )
                    drawCircle(
                        color = Color(0xFF0B0B0B),
                        radius = 4f * zoomScale,
                        center = alertScreen
                    )
                } else {
                    drawCircle(
                        color = Color(0xFF6B7280),
                        radius = 6f * zoomScale,
                        center = alertScreen
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5f * zoomScale,
                        center = alertScreen
                    )
                }
            }

            // 5. Draw All Tanods (for Admin/Dispatcher map)
            allTanods.forEach { tanod ->
                if (tanod.uid != assignedTanod?.uid) {
                    val tanodScreen = latLngToScreen(tanod.currentLocation)
                    drawCircle(
                        color = Color(0xFF1F2937),
                        radius = 7f * zoomScale,
                        center = tanodScreen
                    )
                    drawCircle(
                        color = if (tanod.availability == com.example.data.model.TanodAvailability.AVAILABLE) PinProNeonGreen else Color(0xFF9CA3AF),
                        radius = 4f * zoomScale,
                        center = tanodScreen
                    )
                }
            }

            // 6. Draw Assigned Tanod (with Live Radar Pulse)
            assignedTanod?.let { tanod ->
                val tanodScreen = latLngToScreen(tanod.currentLocation)

                // Outer radar pulse
                drawCircle(
                    color = PinProNeonGreen.copy(alpha = pulseAlpha),
                    radius = pulseRadius * zoomScale,
                    center = tanodScreen
                )

                // Tanod Badge base
                drawCircle(
                    color = Color(0xFF0B0B0B),
                    radius = 12f * zoomScale,
                    center = tanodScreen
                )
                // Neon Green inner accent
                drawCircle(
                    color = PinProNeonGreen,
                    radius = 8f * zoomScale,
                    center = tanodScreen
                )
                // Dark center dot
                drawCircle(
                    color = Color(0xFF0B0B0B),
                    radius = 3.5f * zoomScale,
                    center = tanodScreen
                )
            }

            // 7. Draw User / Incident Location Pin (Neon Green with glow)
            userLocation?.let { userLoc ->
                val userScreen = latLngToScreen(userLoc)

                // Soft glow halo
                drawCircle(
                    color = PinProNeonGreen.copy(alpha = 0.35f),
                    radius = 18f * zoomScale,
                    center = userScreen
                )
                // Outer dark ring
                drawCircle(
                    color = Color(0xFF0B0B0B),
                    radius = 10f * zoomScale,
                    center = userScreen
                )
                // Neon green inner fill
                drawCircle(
                    color = PinProNeonGreen,
                    radius = 7.5f * zoomScale,
                    center = userScreen
                )
                // White center core
                drawCircle(
                    color = Color.White,
                    radius = 3f * zoomScale,
                    center = userScreen
                )
            }
        }

        // Map Watermark / Legend in top corner (12sp small)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(8.dp))
                .border(1.dp, PinProBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Map: Pasig-Marikina Boundary • Pinch to Zoom",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = PinProTextMuted
            )
        }
    }
}
