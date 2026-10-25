package com.example.ui.cyberzone

import android.annotation.SuppressLint
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.example.util.SimulationHelper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRole
import com.example.data.model.Barangay
import com.example.data.model.IncidentReport
import com.example.data.model.LatLng as ModelLatLng
import com.example.data.model.OfficialLocation
import com.example.data.model.ReportCategory
import com.example.data.model.ReportStatus
import com.example.ui.theme.CyberzoneBackground
import com.example.ui.theme.CyberzoneBorder
import com.example.ui.theme.CyberzoneElectricGreen
import com.example.ui.theme.CyberzoneElectricGreenSubtle
import com.example.ui.theme.CyberzoneSurface
import com.example.ui.theme.CyberzoneSurfaceElevated
import com.example.ui.theme.CyberzoneTextMuted
import com.example.ui.theme.CyberzoneTextPrimary
import com.example.ui.theme.CyberzoneTextSecondary
import com.example.ui.theme.InterFontFamily

/**
 * Incident Reporting & Live Tracking Map View
 * - TOP BAR: Displays ONLY big bold "PinPro" logo (28px #10B981 brand green accent).
 * - ZERO MOCK DATA: Empty state banner ("No reports yet") on map when no active reports exist.
 * - DRIVER VIEW for responding Officials with route line, ETA, distance, and "Simulate movement" step button.
 * - Live official location tracking with real-time Firestore listeners.
 */
@Composable
fun CyberzoneMapView(
    pins: List<IncidentReport>,
    officialLocations: List<OfficialLocation> = emptyList(),
    selectedReportId: String? = null,
    isPendingFilterActive: Boolean = false,
    assignedBarangay: Barangay? = null,
    barangays: List<Barangay> = emptyList(),
    currentRole: AppRole = AppRole.RESIDENT,
    currentUserId: String = "",
    unreadCount: Int = 0,
    onOpenNotifications: () -> Unit = {},
    onTogglePendingFilter: () -> Unit = {},
    onReportIncident: (category: ReportCategory, title: String, description: String, latLng: ModelLatLng, locationName: String) -> Unit = { _, _, _, _, _ -> },
    onAcceptReport: (reportId: String) -> Unit = {},
    onSimulateMovement: (reportId: String) -> Unit = {},
    onUpdateOfficialLocation: (officialId: String, name: String, lat: Double, lng: Double, heading: Float, reportId: String) -> Unit = { _, _, _, _, _, _ -> },
    onMarkArrived: (reportId: String) -> Unit = {},
    onResolveReport: (reportId: String, note: String?) -> Unit = { _, _ -> },
    onSwitchRole: (AppRole) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var googleMapMode by remember { mutableStateOf("dark") }

    // Role-based visibility filtering:
    // Residents only see their own reports (and assigned official's location).
    // Officials see all reports and all active official markers.
    val visiblePins = remember(pins, currentRole, currentUserId) {
        if (currentRole == AppRole.RESIDENT) {
            pins.filter { currentUserId.isEmpty() || it.residentId == currentUserId }
        } else {
            pins
        }
    }

    val displayPins = remember(visiblePins, isPendingFilterActive) {
        val filtered = if (isPendingFilterActive) {
            visiblePins.filter { it.status == ReportStatus.PENDING }
        } else {
            visiblePins
        }
        filtered.filter { it.status != ReportStatus.RESOLVED }
    }

    val defaultCenter = remember(assignedBarangay) {
        assignedBarangay?.hallLocation ?: ModelLatLng(14.6762, 120.5375)
    }

    var cameraCenter by remember { mutableStateOf(defaultCenter) }
    var resolveConfirmationToast by remember { mutableStateOf<String?>(null) }

    // Active tracking incident (ON_THE_WAY, ACCEPTED, or ARRIVED only; resolved removed)
    val activeDriverIncident = displayPins.firstOrNull { it.status == ReportStatus.ON_THE_WAY || it.status == ReportStatus.ACCEPTED || it.status == ReportStatus.ARRIVED }
    var selectedPin by remember { mutableStateOf<IncidentReport?>(null) }
    var isSheetExpanded by remember { mutableStateOf(true) }
    var isSheetVisible by remember { mutableStateOf(true) }
    var isMapStyleMenuExpanded by remember { mutableStateOf(false) }

    // Smooth Simulation Loop State
    var isSimulating by remember { mutableStateOf(false) }
    var isSimPaused by remember { mutableStateOf(false) }
    var simSpeedMultiplier by remember { mutableFloatStateOf(1.0f) } // 1.0f, 2.0f, 4.0f
    var traveledMetersState by remember { mutableDoubleStateOf(0.0) }
    var simVehicleLatLng by remember { mutableStateOf<ModelLatLng?>(null) }
    var simHeading by remember { mutableFloatStateOf(0f) }
    var simRemainingRoute by remember { mutableStateOf<List<ModelLatLng>>(emptyList()) }
    var simRemainingDistKm by remember { mutableDoubleStateOf(0.8) }
    var simEtaMinutes by remember { mutableIntStateOf(3) }

    // Reset simulation state if active report changes or gets resolved
    LaunchedEffect(activeDriverIncident?.id) {
        if (activeDriverIncident == null || activeDriverIncident.status == ReportStatus.RESOLVED) {
            isSimulating = false
            isSimPaused = false
            traveledMetersState = 0.0
            simVehicleLatLng = null
            simRemainingRoute = emptyList()
        }
    }

    // 60 FPS Simulation Animation Loop
    LaunchedEffect(isSimulating, isSimPaused, simSpeedMultiplier, activeDriverIncident?.id) {
        if (!isSimulating || isSimPaused || activeDriverIncident == null) return@LaunchedEffect

        val reportId = activeDriverIncident.id
        val fullRoute = if (activeDriverIncident.routeToIncident.size >= 2) {
            activeDriverIncident.routeToIncident
        } else {
            val startLoc = activeDriverIncident.officialLocation ?: assignedBarangay?.hallLocation ?: ModelLatLng(14.6762, 120.5375)
            listOf(startLoc, activeDriverIncident.latLng)
        }

        val totalDist = SimulationHelper.calculatePolylineDistanceMeters(fullRoute)
        if (totalDist <= 0.0) return@LaunchedEffect

        var lastFrameTime = System.currentTimeMillis()
        var lastFirestoreSyncTime = 0L

        while (isSimulating && !isSimPaused) {
            val now = System.currentTimeMillis()
            val dt = ((now - lastFrameTime) / 1000.0).coerceIn(0.005, 0.05)
            lastFrameTime = now

            // Base speed ~35 km/h = 9.72 m/s
            val baseSpeedMps = 9.72 * simSpeedMultiplier
            val progressMeters = traveledMetersState

            // Ease-in at start, Ease-out at destination
            val accelFactor = (progressMeters / 20.0).coerceIn(0.2, 1.0)
            val decelFactor = ((totalDist - progressMeters) / 30.0).coerceIn(0.15, 1.0)
            val currentSpeedMps = baseSpeedMps * Math.min(accelFactor, decelFactor)

            val newTraveled = progressMeters + (currentSpeedMps * dt)
            traveledMetersState = newTraveled

            val interpState = SimulationHelper.interpolateRoutePosition(
                fullRoute = fullRoute,
                traveledMeters = newTraveled,
                speedMetersPerSec = currentSpeedMps
            )

            // Smooth heading angle rotation
            val angleDiff = SimulationHelper.shortestAngleDiff(simHeading, interpState.heading)
            simHeading = (simHeading + angleDiff * (dt * 8.0).toFloat()).let { (it + 360f) % 360f }

            simVehicleLatLng = interpState.currentLocation
            simRemainingRoute = interpState.remainingRoute
            simRemainingDistKm = interpState.remainingDistanceKm
            simEtaMinutes = interpState.etaMinutes

            // Driver view camera follows vehicle smoothly
            cameraCenter = interpState.currentLocation

            // Throttle Firestore updates every 1.5 seconds (or on arrival)
            if (now - lastFirestoreSyncTime >= 1500L || interpState.isArrived) {
                lastFirestoreSyncTime = now
                val officialName = "Officer Berto Aquino"
                val officialId = currentUserId.ifBlank { "official_berto" }

                onUpdateOfficialLocation(
                    officialId,
                    officialName,
                    interpState.currentLocation.lat,
                    interpState.currentLocation.lng,
                    simHeading,
                    reportId
                )
            }

            if (interpState.isArrived) {
                isSimulating = false
                isSimPaused = false
                onMarkArrived(reportId)
                break
            }

            kotlinx.coroutines.delay(16) // ~60 FPS
        }
    }

    // Smooth Gliding Marker Interpolation for Residents
    val residentTargetLoc = activeDriverIncident?.officialLocation
    val animatedResidentLat by animateFloatAsState(
        targetValue = (residentTargetLoc?.lat ?: 14.6762).toFloat(),
        animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
        label = "residentLatAnim"
    )
    val animatedResidentLng by animateFloatAsState(
        targetValue = (residentTargetLoc?.lng ?: 120.5375).toFloat(),
        animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
        label = "residentLngAnim"
    )
    val residentSmoothLocation = remember(animatedResidentLat, animatedResidentLng) {
        ModelLatLng(animatedResidentLat.toDouble(), animatedResidentLng.toDouble())
    }

    // Automatically dismiss selectedPin if it gets resolved
    LaunchedEffect(pins) {
        if (selectedPin?.status == ReportStatus.RESOLVED) {
            selectedPin = null
        }
    }

    LaunchedEffect(selectedReportId, displayPins) {
        if (selectedReportId != null) {
            val found = displayPins.firstOrNull { it.id == selectedReportId }
            if (found != null) {
                selectedPin = found
                cameraCenter = found.latLng
                isSheetExpanded = true
                isSheetVisible = true
            }
        }
    }

    // Resolve Note Dialog state
    var showResolveDialog by remember { mutableStateOf(false) }
    var resolveNoteText by remember { mutableStateOf("") }
    var targetResolveReportId by remember { mutableStateOf<String?>(null) }

    // Reporting dialog state
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(ReportCategory.FIGHTS) }
    var reportTitle by remember { mutableStateOf("") }
    var reportDescription by remember { mutableStateOf("") }
    var reportLocationName by remember { mutableStateOf("") }
    var reportTargetCoords by remember { mutableStateOf<ModelLatLng?>(null) }

    // Active report object passed to BalangaGoogleMapCanvas
    val activeReportToShow = remember(
        selectedPin,
        activeDriverIncident,
        isSimulating,
        simVehicleLatLng,
        simHeading,
        simRemainingRoute,
        simEtaMinutes,
        residentSmoothLocation,
        currentRole
    ) {
        val base = selectedPin ?: activeDriverIncident
        base?.let {
            if (currentRole == AppRole.OFFICIAL && (isSimulating || traveledMetersState > 0.0)) {
                it.copy(
                    officialLocation = simVehicleLatLng ?: it.officialLocation,
                    officialHeading = simHeading,
                    routeToIncident = simRemainingRoute.ifEmpty { it.routeToIncident },
                    officialEtaMinutes = simEtaMinutes
                )
            } else if (currentRole == AppRole.RESIDENT && residentTargetLoc != null) {
                it.copy(
                    officialLocation = residentSmoothLocation,
                    routeToIncident = it.routeToIncident
                )
            } else {
                it
            }
        }
    }
    val simulatedActiveReport = activeReportToShow

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
    ) {
        val isWideScreen = maxWidth >= 600.dp
        val maxSheetHeight = maxHeight * 0.60f

        // 1. Interactive Native Google Map Canvas
        BalangaGoogleMapCanvas(
            centerLatLng = cameraCenter,
            mapStyle = googleMapMode,
            barangays = if (barangays.isNotEmpty()) barangays else listOfNotNull(assignedBarangay),
            detectedBarangay = assignedBarangay,
            pins = displayPins,
            activeReport = activeReportToShow,
            onMapClick = { lat, lng ->
                reportTargetCoords = ModelLatLng(lat, lng)
                reportLocationName = "${assignedBarangay?.name ?: "Balanga City"} Sector"
                selectedCategory = ReportCategory.FIGHTS
                reportTitle = ""
                reportDescription = ""
                showReportDialog = true
            },
            onPinClick = { pinId ->
                val found = displayPins.firstOrNull { it.id == pinId }
                if (found != null) {
                    selectedPin = found
                    cameraCenter = found.latLng
                    isSheetExpanded = true
                    isSheetVisible = true
                }
            },
            bottomControlsPaddingDp = if (isSheetVisible && activeReportToShow != null) 230 else 110,
            modifier = Modifier.fillMaxSize()
        )

        // 2. TOP BAR OVERLAY: Compact single-row pinned to top edge
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .background(CyberzoneSurface.copy(alpha = 0.90f))
                .border(1.dp, CyberzoneBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PinPro Logo and Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "PinPro Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PinPro",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = CyberzoneElectricGreen,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.testTag("pinpro_brand_title")
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification Bell Button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CyberzoneSurfaceElevated)
                            .border(1.dp, if (unreadCount > 0) CyberzoneElectricGreen else CyberzoneBorder, CircleShape)
                            .clickable(onClick = onOpenNotifications)
                            .padding(6.dp)
                            .testTag("notifications_bell_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = if (unreadCount > 0) CyberzoneElectricGreen else CyberzoneTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(CyberzoneElectricGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadCount > 9) "9+" else "$unreadCount",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 7.5.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Pending Filter Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isPendingFilterActive) CyberzoneElectricGreen else CyberzoneSurfaceElevated)
                            .border(1.dp, if (isPendingFilterActive) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(16.dp))
                            .clickable(onClick = onTogglePendingFilter)
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("filter_pending_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = if (isPendingFilterActive) Color.Black else CyberzoneElectricGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPendingFilterActive) "Pending Only" else "All Reports",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isPendingFilterActive) Color.Black else CyberzoneTextPrimary
                            )
                        }
                    }

                    // Map Style Mode Switcher Dropdown
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CyberzoneSurfaceElevated)
                                .border(1.dp, CyberzoneBorder, CircleShape)
                                .clickable { isMapStyleMenuExpanded = true }
                                .padding(6.dp)
                                .testTag("map_style_dropdown_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Map Style",
                                tint = CyberzoneElectricGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMapStyleMenuExpanded,
                            onDismissRequest = { isMapStyleMenuExpanded = false },
                            modifier = Modifier.background(CyberzoneSurfaceElevated)
                        ) {
                            listOf(
                                "streets" to "Streets",
                                "satellite" to "Satellite",
                                "dark" to "Dark"
                            ).forEach { (mode, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = label,
                                                fontFamily = InterFontFamily,
                                                fontWeight = if (googleMapMode == mode) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp,
                                                color = if (googleMapMode == mode) CyberzoneElectricGreen else CyberzoneTextPrimary
                                            )
                                            if (googleMapMode == mode) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = CyberzoneElectricGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        googleMapMode = mode
                                        isMapStyleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Toast Banner for Resolution Confirmation
        AnimatedVisibility(
            visible = resolveConfirmationToast != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            resolveConfirmationToast?.let { toastMsg ->
                LaunchedEffect(toastMsg) {
                    kotlinx.coroutines.delay(3500)
                    resolveConfirmationToast = null
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberzoneElectricGreen)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { resolveConfirmationToast = null }
                        .testTag("resolve_confirmation_toast"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = toastMsg,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. EMPTY STATE OVERLAY BARS: When no active report exists in Firestore
        if (displayPins.isEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberzoneSurface.copy(alpha = 0.94f))
                    .border(1.dp, CyberzoneBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
                    .testTag("empty_map_state_banner"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberzoneElectricGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No reports yet",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CyberzoneTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentRole == AppRole.RESIDENT)
                            "Tap 'Report incident' below to submit a community report."
                        else
                            "No active incident reports in your jurisdiction.",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = CyberzoneTextMuted
                    )
                }
            }
        }

        // 4. FLOATING TRACKING PILL CHIP (When bottom sheet is dismissed during active tracking)
        AnimatedVisibility(
            visible = !isSheetVisible && activeReportToShow != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            activeReportToShow?.let { incident ->
                Box(
                    modifier = Modifier
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(CyberzoneSurface)
                        .border(1.dp, CyberzoneElectricGreen, CircleShape)
                        .clickable { isSheetVisible = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("floating_tracking_pill"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = CyberzoneElectricGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (incident.status == ReportStatus.ON_THE_WAY || incident.status == ReportStatus.ACCEPTED)
                                "Tracking • ETA ${incident.officialEtaMinutes ?: 3} min"
                            else
                                "Active Incident • ${incident.label}",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyberzoneTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = "Reopen Tracking Sheet",
                            tint = CyberzoneElectricGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 5. FLOATING ACTION BUTTONS (Recenter & Report Incident)
        Column(
            modifier = Modifier
                .align(if (isWideScreen) Alignment.BottomStart else Alignment.BottomEnd)
                .padding(
                    start = if (isWideScreen) 16.dp else 0.dp,
                    end = if (isWideScreen) 0.dp else 16.dp,
                    bottom = if (isWideScreen) 16.dp else if (isSheetVisible && activeReportToShow != null) (if (isSheetExpanded) 250.dp else 140.dp) else 80.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recenter Camera Button
            IconButton(
                onClick = { cameraCenter = defaultCenter },
                modifier = Modifier
                    .size(46.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CyberzoneSurface)
                    .border(1.dp, CyberzoneBorder, CircleShape)
                    .testTag("recenter_gps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = "Recenter",
                    tint = CyberzoneElectricGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Primary "Report incident" FAB
            Box(
                modifier = Modifier
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CyberzoneElectricGreen)
                    .clickable {
                        reportTargetCoords = defaultCenter
                        reportLocationName = "${assignedBarangay?.name ?: "Balanga City"}"
                        selectedCategory = ReportCategory.FIGHTS
                        reportTitle = ""
                        reportDescription = ""
                        showReportDialog = true
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("report_incident_fab")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Report Incident",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Report incident",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // 6. BOTTOM TRACKING CARD / SIDE CARD (When an Incident Report is Selected or Active and Sheet is Visible)
        AnimatedVisibility(
            visible = isSheetVisible && activeReportToShow != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = if (isWideScreen) {
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp)
                    .width(360.dp)
            } else {
                Modifier.align(Alignment.BottomCenter)
            }
        ) {
            activeReportToShow?.let { incident ->
                val isOnTheWay = incident.status == ReportStatus.ON_THE_WAY || incident.status == ReportStatus.ACCEPTED
                val isArrived = incident.status == ReportStatus.ARRIVED
                val isResolved = incident.status == ReportStatus.RESOLVED

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = if (isSheetExpanded) maxSheetHeight else 130.dp)
                        .shadow(16.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(CyberzoneSurface)
                        .border(1.dp, if (isOnTheWay) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("incident_detail_tracking_sheet")
                ) {
                    // Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(CyberzoneBorder)
                            .clickable { isSheetExpanded = !isSheetExpanded }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title & Category Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            GoogleCategoryBadge(incident.category, size = 32.dp, iconSize = 18.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    GoogleStatusBadge(incident.status)
                                    if (isOnTheWay) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CyberzoneElectricGreen)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${incident.officialEtaMinutes ?: 3} MINS ETA",
                                                fontFamily = InterFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = incident.label,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = CyberzoneTextPrimary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isSheetExpanded = !isSheetExpanded },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSheetExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                    contentDescription = "Toggle",
                                    tint = CyberzoneTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { isSheetVisible = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = CyberzoneTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (isSheetExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Location & Reported By
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = CyberzoneElectricGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${incident.locationName} • Reported by ${incident.reportedBy}",
                                fontFamily = InterFontFamily,
                                fontSize = 11.5.sp,
                                color = CyberzoneTextSecondary
                            )
                        }

                        if (incident.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = incident.description,
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = CyberzoneTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Real-time Incident Lifecycle Progress Bar
                        val currentStepIndex = when (incident.status) {
                            ReportStatus.PENDING -> 1
                            ReportStatus.ACCEPTED -> 2
                            ReportStatus.ON_THE_WAY -> 3
                            ReportStatus.ARRIVED -> 4
                            ReportStatus.RESOLVED -> 5
                        }

                        val timelineSteps = listOf(
                            "Reported",
                            "Accepted",
                            "On the way",
                            "Arrived",
                            "Resolved"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberzoneSurfaceElevated)
                                .border(1.dp, CyberzoneBorder, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            timelineSteps.forEachIndexed { index, stepTitle ->
                                val stepNumber = index + 1
                                val isDone = stepNumber < currentStepIndex
                                val isCurrent = stepNumber == currentStepIndex

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCurrent -> CyberzoneElectricGreen
                                                    isDone -> CyberzoneElectricGreenSubtle
                                                    else -> CyberzoneSurface
                                                }
                                            )
                                            .border(
                                                1.dp,
                                                if (isCurrent || isDone) CyberzoneElectricGreen else CyberzoneBorder,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = CyberzoneElectricGreen,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "$stepNumber",
                                                fontFamily = InterFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp,
                                                color = if (isCurrent) Color.Black else CyberzoneTextMuted
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stepTitle,
                                        fontFamily = InterFontFamily,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 9.sp,
                                        color = if (isCurrent) CyberzoneElectricGreen else if (isDone) CyberzoneTextPrimary else CyberzoneTextMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Official Action Buttons (Accept / Resolve)
                    if (currentRole == AppRole.OFFICIAL) {
                        if (incident.status == ReportStatus.PENDING) {
                            Button(
                                onClick = { onAcceptReport(incident.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("official_accept_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberzoneElectricGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Accept Incident Response",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                            }
                        } else if (!isResolved) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Simulate / Pause / Resume Toggle Button
                                Button(
                                    onClick = {
                                        if (!isSimulating) {
                                            isSimulating = true
                                            isSimPaused = false
                                        } else {
                                            isSimPaused = !isSimPaused
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(42.dp)
                                        .testTag("simulate_movement_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSimulating && !isSimPaused) CyberzoneSurfaceElevated else CyberzoneElectricGreen
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (!isSimulating || isSimPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null,
                                        tint = if (isSimulating && !isSimPaused) CyberzoneElectricGreen else Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = when {
                                            !isSimulating -> "Simulate 🚗"
                                            isSimPaused -> "Resume ▶️"
                                            else -> "Pause ⏸️"
                                        },
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSimulating && !isSimPaused) CyberzoneElectricGreen else Color.Black
                                    )
                                }

                                // Speed Multiplier Toggle (1x, 2x, 4x)
                                Box(
                                    modifier = Modifier
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CyberzoneSurfaceElevated)
                                        .border(1.dp, CyberzoneElectricGreen, RoundedCornerShape(10.dp))
                                        .clickable {
                                            simSpeedMultiplier = when (simSpeedMultiplier) {
                                                1.0f -> 2.0f
                                                2.0f -> 4.0f
                                                else -> 1.0f
                                            }
                                        }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${simSpeedMultiplier.toInt()}x",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = CyberzoneElectricGreen
                                    )
                                }

                                // Mark Resolved Button
                                Button(
                                    onClick = {
                                        targetResolveReportId = incident.id
                                        showResolveDialog = true
                                    },
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(42.dp)
                                        .testTag("official_resolve_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberzoneElectricGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Mark Resolved",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    } else {
                        // Resident Status Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberzoneSurfaceElevated)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (incident.status) {
                                        ReportStatus.PENDING -> Icons.Default.Schedule
                                        ReportStatus.ACCEPTED, ReportStatus.ON_THE_WAY -> Icons.Default.DirectionsCar
                                        ReportStatus.ARRIVED -> Icons.Default.LocationOn
                                        ReportStatus.RESOLVED -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = if (isOnTheWay || isArrived) CyberzoneElectricGreen else CyberzoneTextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (incident.status) {
                                        ReportStatus.PENDING -> "Report Pending Official Response"
                                        ReportStatus.ACCEPTED, ReportStatus.ON_THE_WAY -> "Official dispatched • Live tracking enabled"
                                        ReportStatus.ARRIVED -> "Official has arrived at the scene"
                                        ReportStatus.RESOLVED -> "Incident officially resolved"
                                    },
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (isOnTheWay || isArrived) CyberzoneElectricGreen else CyberzoneTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. REPORT INCIDENT DIALOG (5 Required Categories)
        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                containerColor = CyberzoneSurface,
                title = {
                    Text(
                        text = "Report Incident",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CyberzoneTextPrimary
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SELECT CATEGORY",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = CyberzoneElectricGreen
                        )

                        // 5 Category Cards
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ReportCategory.values().forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) CyberzoneElectricGreenSubtle else CyberzoneSurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) CyberzoneElectricGreen else CyberzoneBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedCategory = cat
                                            if (reportTitle.isBlank()) {
                                                reportTitle = "${cat.displayName} reported"
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("category_option_${cat.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            GoogleCategoryBadge(cat, size = 32.dp, iconSize = 18.dp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = cat.displayName,
                                                    fontFamily = InterFontFamily,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) CyberzoneElectricGreen else CyberzoneTextPrimary
                                                )
                                                Text(
                                                    text = cat.description,
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 11.sp,
                                                    color = CyberzoneTextMuted
                                                )
                                            }
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = CyberzoneElectricGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = reportTitle,
                            onValueChange = { reportTitle = it },
                            label = { Text("Title / Summary", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberzoneElectricGreen,
                                unfocusedBorderColor = CyberzoneBorder,
                                focusedTextColor = CyberzoneTextPrimary,
                                unfocusedTextColor = CyberzoneTextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_title_input")
                        )

                        OutlinedTextField(
                            value = reportDescription,
                            onValueChange = { reportDescription = it },
                            label = { Text("Details & Description", fontSize = 12.sp) },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberzoneElectricGreen,
                                unfocusedBorderColor = CyberzoneBorder,
                                focusedTextColor = CyberzoneTextPrimary,
                                unfocusedTextColor = CyberzoneTextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_desc_input")
                        )

                        OutlinedTextField(
                            value = reportLocationName,
                            onValueChange = { reportLocationName = it },
                            label = { Text("Street Address / Landmark", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberzoneElectricGreen,
                                unfocusedBorderColor = CyberzoneBorder,
                                focusedTextColor = CyberzoneTextPrimary,
                                unfocusedTextColor = CyberzoneTextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("report_location_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val coords = reportTargetCoords ?: defaultCenter
                            val title = reportTitle.ifBlank { "${selectedCategory.displayName} Reported" }
                            val desc = reportDescription.ifBlank { selectedCategory.description }
                            val loc = reportLocationName.ifBlank { "${assignedBarangay?.name ?: "Balanga City"}" }

                            onReportIncident(
                                selectedCategory,
                                title,
                                desc,
                                coords,
                                loc
                            )
                            showReportDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberzoneElectricGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("submit_report_button")
                    ) {
                        Text(
                            text = "Submit Report",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text(
                            text = "Cancel",
                            fontFamily = InterFontFamily,
                            color = CyberzoneTextMuted
                        )
                    }
                }
            )
        }

        // 8. RESOLVE NOTE DIALOG FOR OFFICIALS
        if (showResolveDialog && targetResolveReportId != null) {
            AlertDialog(
                onDismissRequest = {
                    showResolveDialog = false
                    targetResolveReportId = null
                },
                containerColor = CyberzoneSurface,
                title = {
                    Text(
                        text = "Resolve Incident",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CyberzoneTextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Provide an optional resolution note or action taken summary:",
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            color = CyberzoneTextSecondary
                        )
                        OutlinedTextField(
                            value = resolveNoteText,
                            onValueChange = { resolveNoteText = it },
                            label = { Text("Resolution Note", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Dispersed crowd and secured area.", fontSize = 12.sp) },
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberzoneElectricGreen,
                                unfocusedBorderColor = CyberzoneBorder,
                                focusedTextColor = CyberzoneTextPrimary,
                                unfocusedTextColor = CyberzoneTextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("resolve_note_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val repId = targetResolveReportId
                            if (repId != null) {
                                onResolveReport(repId, resolveNoteText.ifBlank { null })
                            }
                            selectedPin = null
                            resolveConfirmationToast = "Report resolved"
                            showResolveDialog = false
                            targetResolveReportId = null
                            resolveNoteText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberzoneElectricGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("confirm_resolve_button")
                    ) {
                        Text(
                            text = "Confirm Resolution",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showResolveDialog = false
                            targetResolveReportId = null
                            resolveNoteText = ""
                        }
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = InterFontFamily,
                            color = CyberzoneTextMuted
                        )
                    }
                }
            )
        }
    }
}
