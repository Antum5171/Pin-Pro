package com.example.ui.cyberzone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimalReportPin
import com.example.data.model.AnimalType
import com.example.data.model.PinStatus
import com.example.ui.theme.CyberzoneBackground
import com.example.ui.theme.CyberzoneBorder
import com.example.ui.theme.CyberzoneElectricGreen
import com.example.ui.theme.CyberzoneElectricGreenBright
import com.example.ui.theme.CyberzoneElectricGreenSubtle
import com.example.ui.theme.CyberzoneError
import com.example.ui.theme.CyberzoneSurface
import com.example.ui.theme.CyberzoneSurfaceElevated
import com.example.ui.theme.CyberzoneTextMuted
import com.example.ui.theme.CyberzoneTextPrimary
import com.example.ui.theme.CyberzoneTextSecondary
import com.example.ui.theme.InterFontFamily
import kotlin.math.hypot

/**
 * Main Map Dashboard Screen for PinPro:
 * 1. Dark grid map viewport with minimalist building vector outlines.
 * 2. Real-time Dangerous Animal pins (Snake, Aggressive Dog) with animated glowing neon pulse.
 * 3. Floating filter pill near the bottom for filtering Pending vs All animal hazard pins.
 * 4. Tap anywhere on map to drop dynamic new Dangerous Animal report pins.
 */
@Composable
fun CyberzoneMapView(
    pins: List<AnimalReportPin>,
    isPendingFilterActive: Boolean,
    onTogglePendingFilter: () -> Unit,
    onDropPin: (label: String, xRatio: Float, yRatio: Float) -> Unit,
    onDeletePin: (String) -> Unit,
    onTogglePinStatus: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayPins = if (isPendingFilterActive) {
        pins.filter { it.status == PinStatus.PENDING }
    } else {
        pins
    }

    var selectedPin by remember { mutableStateOf<AnimalReportPin?>(displayPins.firstOrNull()) }
    var pendingDropCoordinates by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var selectedAnimalType by remember { mutableStateOf(AnimalType.SNAKE) }
    var reportNote by remember { mutableStateOf("") }
    var reportLocation by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }

    // Neon Radar & Pulse Animations
    val infiniteTransition = rememberInfiniteTransition(label = "hazardMap")
    val pulseRingRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseRingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
    ) {
        // 1. Dark Grid Viewport with Minimalist Building Vector Outlines
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(displayPins) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()

                        // Check if tap hit an existing pin
                        val hitPin = displayPins.firstOrNull { pin ->
                            val pinX = pin.xRatio * width
                            val pinY = pin.yRatio * height
                            hypot(tapOffset.x - pinX, tapOffset.y - pinY) < 48f
                        }

                        if (hitPin != null) {
                            selectedPin = hitPin
                        } else {
                            // Tap on map to report a dangerous animal (drop pin)
                            val relativeX = tapOffset.x / width
                            val relativeY = tapOffset.y / height
                            pendingDropCoordinates = Pair(relativeX, relativeY)
                            reportNote = ""
                            reportLocation = "Neighborhood Sector"
                            showReportDialog = true
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1.1 Base Grid Lines
            val gridSize = 42f
            val gridColor = CyberzoneElectricGreen.copy(alpha = 0.08f)
            val majorGridColor = CyberzoneElectricGreen.copy(alpha = 0.18f)

            var x = 0f
            var colIndex = 0
            while (x < width) {
                drawLine(
                    color = if (colIndex % 4 == 0) majorGridColor else gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = if (colIndex % 4 == 0) 1.5f else 1.0f
                )
                x += gridSize
                colIndex++
            }

            var y = 0f
            var rowIndex = 0
            while (y < height) {
                drawLine(
                    color = if (rowIndex % 4 == 0) majorGridColor else gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = if (rowIndex % 4 == 0) 1.5f else 1.0f
                )
                y += gridSize
                rowIndex++
            }

            // 1.2 Minimalist Isometric / Vector Building Outlines
            drawMinimalistWireframeBuildings(width, height)

            // 1.3 Radar Sweep Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CyberzoneElectricGreen.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.44f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.5f, height * 0.44f)
            )

            // 1.4 Render Dangerous Animal Pins
            displayPins.forEach { pin ->
                val pinScreenX = pin.xRatio * width
                val pinScreenY = pin.yRatio * height
                val isSelected = selectedPin?.id == pin.id

                drawAnimalHazardPin(
                    center = Offset(pinScreenX, pinScreenY),
                    pin = pin,
                    pulseRadius = pulseRingRadius,
                    pulseAlpha = pulseRingAlpha,
                    isSelected = isSelected
                )
            }
        }

        // 2. Top HUD Header
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberzoneSurface.copy(alpha = 0.88f))
                    .border(1.dp, CyberzoneBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberzoneElectricGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ANIMAL RADAR: ${displayPins.size} REPORTS",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = CyberzoneTextPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberzoneSurface.copy(alpha = 0.88f))
                    .border(1.dp, CyberzoneElectricGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Tap Map to Report Animal",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = CyberzoneElectricGreen
                )
            }
        }

        // 3. Floating Filter Pill Near the Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (selectedPin != null) 180.dp else 24.dp)
        ) {
            FloatingHazardFilterPill(
                isPendingOnly = isPendingFilterActive,
                onClick = onTogglePendingFilter
            )
        }

        // 4. Interactive Bottom Sheet for Selected Animal Pin
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedPin?.let { pin ->
                SelectedAnimalPinCard(
                    pin = pin,
                    onClose = { selectedPin = null },
                    onToggleStatus = {
                        onTogglePinStatus(pin.id)
                        selectedPin = pin.copy(
                            status = if (pin.status == PinStatus.PENDING) PinStatus.DONE else PinStatus.PENDING
                        )
                    },
                    onDelete = {
                        onDeletePin(pin.id)
                        selectedPin = null
                    }
                )
            }
        }

        // 5. Report Dangerous Animal Dialog (Snake, Aggressive Dog, etc.)
        if (showReportDialog && pendingDropCoordinates != null) {
            AlertDialog(
                onDismissRequest = {
                    showReportDialog = false
                    pendingDropCoordinates = null
                },
                containerColor = CyberzoneSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Report Dangerous Animal",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CyberzoneTextPrimary
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Select animal hazard type and provide sighting details.",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = CyberzoneTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "ANIMAL TYPE",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = CyberzoneElectricGreen
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Type selector chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AnimalType.values()) { type ->
                                val isSelected = selectedAnimalType == type
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) CyberzoneElectricGreen else CyberzoneSurfaceElevated)
                                        .border(1.dp, if (isSelected) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(10.dp))
                                        .clickable { selectedAnimalType = type }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${type.emoji} ${type.displayName}",
                                        fontFamily = InterFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF121212) else CyberzoneTextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        CyberInputField(
                            value = reportLocation,
                            onValueChange = { reportLocation = it },
                            label = "LOCATION / LANDMARK",
                            placeholder = "e.g. Near corner gate, garden alley"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        CyberInputField(
                            value = reportNote,
                            onValueChange = { reportNote = it },
                            label = "SIGHTING DESCRIPTION",
                            placeholder = "e.g. Aggressive, venomous, moving towards street"
                        )
                    }
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberzoneElectricGreen)
                            .clickable {
                                val coords = pendingDropCoordinates!!
                                val label = "${selectedAnimalType.displayName} Sighted"
                                onDropPin(label, coords.first, coords.second)
                                showReportDialog = false
                                pendingDropCoordinates = null
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Drop Report Pin",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF121212)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showReportDialog = false
                        pendingDropCoordinates = null
                    }) {
                        Text(
                            text = "Cancel",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = CyberzoneTextSecondary
                        )
                    }
                }
            )
        }
    }
}

/**
 * Draws minimalist building wireframe vector outlines.
 */
private fun DrawScope.drawMinimalistWireframeBuildings(width: Float, height: Float) {
    val wireframeColor = CyberzoneElectricGreen.copy(alpha = 0.22f)
    val wallColor = Color(0xFF242424)
    val strokeWidth = 1.6f

    // Building Blocks
    drawBuildingOutline(width * 0.12f, height * 0.18f, width * 0.22f, height * 0.12f, wireframeColor, wallColor, strokeWidth)
    drawBuildingOutline(width * 0.64f, height * 0.15f, width * 0.26f, height * 0.16f, wireframeColor, wallColor, strokeWidth)
    drawBuildingOutline(width * 0.15f, height * 0.58f, width * 0.28f, height * 0.18f, wireframeColor, wallColor, strokeWidth)
    drawBuildingOutline(width * 0.62f, height * 0.64f, width * 0.26f, height * 0.14f, wireframeColor, wallColor, strokeWidth)

    // Streets
    drawLine(
        color = CyberzoneElectricGreen.copy(alpha = 0.15f),
        start = Offset(0f, height * 0.36f),
        end = Offset(width, height * 0.36f),
        strokeWidth = 2.0f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
    )
    drawLine(
        color = CyberzoneElectricGreen.copy(alpha = 0.15f),
        start = Offset(width * 0.50f, 0f),
        end = Offset(width * 0.50f, height),
        strokeWidth = 2.0f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
    )
}

private fun DrawScope.drawBuildingOutline(
    x: Float, y: Float, w: Float, h: Float,
    wireframeColor: Color, wallColor: Color, strokeWidth: Float
) {
    drawRoundRect(wallColor, topLeft = Offset(x, y), size = Size(w, h), cornerRadius = CornerRadius(6f, 6f))
    drawRoundRect(wireframeColor, topLeft = Offset(x, y), size = Size(w, h), cornerRadius = CornerRadius(6f, 6f), style = Stroke(width = strokeWidth))
}

/**
 * Draws an interactive dangerous animal hazard pin on the map.
 * Shows glowing neon pulse if PENDING, subdued check styling if DONE.
 */
private fun DrawScope.drawAnimalHazardPin(
    center: Offset,
    pin: AnimalReportPin,
    pulseRadius: Float,
    pulseAlpha: Float,
    isSelected: Boolean
) {
    val isPending = pin.status == PinStatus.PENDING

    if (isPending) {
        // Glowing animated pulse ring for active/pending threats
        drawCircle(
            color = CyberzoneElectricGreen.copy(alpha = pulseAlpha * 0.35f),
            radius = pulseRadius * 1.4f,
            center = center
        )
        drawCircle(
            color = CyberzoneElectricGreen.copy(alpha = pulseAlpha * 0.8f),
            radius = pulseRadius,
            center = center,
            style = Stroke(width = 2.2f)
        )
    }

    // Pin Core Marker
    val pinColor = if (isPending) CyberzoneElectricGreen else Color(0xFF6B7280)
    drawCircle(
        color = pinColor,
        radius = if (isSelected) 15f else 12f,
        center = center
    )
    drawCircle(
        color = Color(0xFF121212),
        radius = 7f,
        center = center
    )

    // Floating label pill with animal emoji
    val truncated = "${pin.animalType.emoji} ${pin.label.take(14)}"
    val pillWidth = 140f
    val pillHeight = 28f
    val pillTopLeft = Offset(center.x - pillWidth / 2f, center.y - 42f)

    drawRoundRect(
        color = Color(0xFF121212),
        topLeft = pillTopLeft,
        size = Size(pillWidth, pillHeight),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = if (isPending) CyberzoneElectricGreen else CyberzoneBorder,
        topLeft = pillTopLeft,
        size = Size(pillWidth, pillHeight),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = if (isSelected) 2.0f else 1.2f)
    )

    // Connector tick
    drawLine(
        color = if (isPending) CyberzoneElectricGreen else CyberzoneBorder,
        start = Offset(center.x, pillTopLeft.y + pillHeight),
        end = center,
        strokeWidth = 1.5f
    )

    // Label Text
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = if (isPending) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#9E9E9E")
            textSize = 22f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawText(truncated, center.x, pillTopLeft.y + 20f, paint)
    }
}

/**
 * Floating filter pill near the bottom containing location icon and "Pending Threats" filter.
 */
@Composable
fun FloatingHazardFilterPill(
    isPendingOnly: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = CyberzoneElectricGreen,
                ambientColor = CyberzoneElectricGreen
            )
            .clip(RoundedCornerShape(32.dp))
            .background(if (isPendingOnly) CyberzoneElectricGreen else CyberzoneSurfaceElevated)
            .border(
                width = 2.dp,
                color = if (isPendingOnly) CyberzoneElectricGreenBright else CyberzoneElectricGreen.copy(alpha = 0.5f),
                shape = RoundedCornerShape(32.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .testTag("hazard_filter_pill"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location Pin",
                tint = if (isPendingOnly) Color(0xFF121212) else CyberzoneElectricGreen,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isPendingOnly) "Showing: Pending Only" else "Filter: All Hazards",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 0.8.sp,
                color = if (isPendingOnly) Color(0xFF121212) else CyberzoneTextPrimary
            )
        }
    }
}

/**
 * Detail card for a selected dangerous animal pin with Mark as Done toggle.
 */
@Composable
fun SelectedAnimalPinCard(
    pin: AnimalReportPin,
    onClose: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPending = pin.status == PinStatus.PENDING

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CyberzoneSurface)
            .border(1.5.dp, if (isPending) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = pin.animalType.emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = pin.label,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CyberzoneTextPrimary
                    )
                    Text(
                        text = pin.locationName.ifBlank { "Neighborhood Area" },
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = CyberzoneTextSecondary
                    )
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = CyberzoneTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = pin.description,
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            color = CyberzoneTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Toggle: Mark as Done / Re-open
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isPending) CyberzoneElectricGreen else Color(0xFF2E2E2E))
                    .clickable(onClick = onToggleStatus)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPending) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isPending) Color(0xFF121212) else CyberzoneTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPending) "Mark as Done" else "Mark as Pending",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isPending) Color(0xFF121212) else CyberzoneTextPrimary
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberzoneSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Pin",
                    tint = CyberzoneError,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
