package com.example.ui.cyberzone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRole
import com.example.data.model.CyberzoneTab
import com.example.data.repository.PinProRepository
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
 * Main Cyberzone Dashboard container orchestrating:
 * - Animated Bottom Navigation Bar (Home, Map Pin, Search, Profile)
 * - Real Google Maps Delivery Navigation & Live Incident Tracking
 * - Developer Role Switcher (Resident vs Official)
 * - Push / In-App Notification alerting resident when official marks an incident resolved
 */
@Composable
fun CyberzoneMainDashboard(
    username: String,
    repository: PinProRepository,
    onLogout: () -> Unit,
    onOpenLocationPicker: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(CyberzoneTab.MAP) } // Default to Map Pin dashboard
    var selectedReportId by remember { mutableStateOf<String?>(null) }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    val pins by repository.cyberPins.collectAsState()
    val officialLocations by repository.officialLocations.collectAsState()
    val reportEvents by repository.reportEvents.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val isPendingFilterActive by repository.isPendingOnlyFilterActive.collectAsState()
    val confirmedBarangay by repository.confirmedBarangay.collectAsState()
    val barangays by repository.barangays.collectAsState()
    val currentRole by repository.currentRole.collectAsState()
    val latestResolvedNotification by repository.latestResolvedNotification.collectAsState()

    val currentUid = currentUser?.uid.orEmpty()

    val unreadCount = remember(notifications, currentRole, currentUid) {
        if (currentRole == AppRole.OFFICIAL) {
            notifications.count { !it.read && (it.recipientId == "OFFICIAL" || it.recipientId == currentUid) }
        } else {
            notifications.count { !it.read && (it.recipientId == currentUid || it.recipientId == "RESIDENT" || it.recipientId.isBlank()) }
        }
    }

    // Real-time In-App Banner Toast for incoming notifications
    var activeToastNotif by remember { mutableStateOf<com.example.data.model.AppNotification?>(null) }
    var lastSeenNotifId by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(notifications, currentRole, currentUid) {
        val newest = notifications.firstOrNull { notif ->
            !notif.read && (
                (currentRole == AppRole.OFFICIAL && (notif.recipientId == "OFFICIAL" || notif.recipientId == currentUid)) ||
                (currentRole == AppRole.RESIDENT && (notif.recipientId == currentUid || notif.recipientId == "RESIDENT" || notif.recipientId.isBlank()))
            )
        }
        if (newest != null && newest.id != lastSeenNotifId) {
            lastSeenNotifId = newest.id
            activeToastNotif = newest
        }
    }

    androidx.compose.runtime.LaunchedEffect(activeToastNotif?.id) {
        if (activeToastNotif != null) {
            kotlinx.coroutines.delay(4500)
            activeToastNotif = null
        }
    }

    Scaffold(
        bottomBar = {
            CyberzoneBottomNav(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        },
        containerColor = CyberzoneBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = activeTab,
                animationSpec = tween(280),
                label = "tabTransition"
            ) { tab ->
                when (tab) {
                    CyberzoneTab.HOME -> {
                        CyberzoneHomeFeedView(
                            pins = pins,
                            onNavigateToMap = { activeTab = CyberzoneTab.MAP },
                            onSelectReport = { reportId ->
                                selectedReportId = reportId
                                activeTab = CyberzoneTab.MAP
                            },
                            onTogglePinStatus = { pinId ->
                                repository.togglePinStatus(pinId)
                            },
                            assignedBarangay = confirmedBarangay,
                            currentRole = currentRole
                        )
                    }
                    CyberzoneTab.MAP -> {
                        CyberzoneMapView(
                            pins = pins,
                            officialLocations = officialLocations,
                            selectedReportId = selectedReportId,
                            isPendingFilterActive = isPendingFilterActive,
                            assignedBarangay = confirmedBarangay,
                            barangays = barangays,
                            currentRole = currentRole,
                            currentUserId = currentUser?.uid ?: "",
                            unreadCount = unreadCount,
                            onOpenNotifications = { showNotificationsSheet = true },
                            onTogglePendingFilter = { repository.togglePendingOnlyFilter() },
                            onReportIncident = { category, label, description, latLng, locName ->
                                repository.addIncidentReport(category, label, description, latLng, locName)
                            },
                            onAcceptReport = { reportId ->
                                repository.acceptReport(reportId)
                            },
                            onSimulateMovement = { reportId ->
                                repository.simulateMovementStep(reportId)
                            },
                            onUpdateOfficialLocation = { officialId, name, lat, lng, heading, repId ->
                                repository.updateOfficialLocation(officialId, name, lat, lng, heading, repId, writeEvent = false)
                            },
                            onMarkArrived = { reportId ->
                                repository.markArrived(reportId)
                            },
                            onResolveReport = { reportId, note ->
                                repository.resolveReport(reportId, note = note)
                            },
                            onSwitchRole = { newRole ->
                                repository.setDeveloperRole(newRole)
                            }
                        )
                    }
                    CyberzoneTab.SEARCH -> {
                        CyberzoneSearchView(
                            pins = pins,
                            onSelectPin = { pin ->
                                selectedReportId = pin.id
                                activeTab = CyberzoneTab.MAP
                            }
                        )
                    }
                    CyberzoneTab.PROFILE -> {
                        CyberzoneProfileView(
                            username = username,
                            pinCount = pins.size,
                            reportEvents = reportEvents,
                            notifications = notifications,
                            assignedBarangay = confirmedBarangay,
                            currentRole = currentRole,
                            onSetRole = { newRole ->
                                repository.setDeveloperRole(newRole)
                            },
                            onOpenLocationPicker = onOpenLocationPicker,
                            onLogout = onLogout
                        )
                    }
                }
            }

            // Real-Time Floating In-App Banner Toast
            AnimatedVisibility(
                visible = activeToastNotif != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                activeToastNotif?.let { notif ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberzoneSurface)
                            .border(1.5.dp, CyberzoneElectricGreen, RoundedCornerShape(14.dp))
                            .clickable {
                                repository.markNotificationRead(notif.id)
                                if (notif.reportId.isNotBlank()) {
                                    selectedReportId = notif.reportId
                                    activeTab = CyberzoneTab.MAP
                                }
                                activeToastNotif = null
                            }
                            .padding(14.dp)
                            .testTag("in_app_notification_toast")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(CyberzoneElectricGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = notif.title.ifBlank { "PinPro Notification" },
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = CyberzoneElectricGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.message,
                                        fontFamily = InterFontFamily,
                                        fontSize = 12.sp,
                                        color = CyberzoneTextPrimary,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { activeToastNotif = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = CyberzoneTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Notifications List Sheet Dialog
            if (showNotificationsSheet) {
                val dateFormat = remember { java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault()) }
                val filteredNotifs = remember(notifications, currentRole, currentUid) {
                    notifications.filter { notif ->
                        if (currentRole == AppRole.OFFICIAL) {
                            notif.recipientId == "OFFICIAL" || notif.recipientId == currentUid
                        } else {
                            notif.recipientId == currentUid || notif.recipientId == "RESIDENT" || notif.recipientId.isBlank()
                        }
                    }
                }

                AlertDialog(
                    onDismissRequest = { showNotificationsSheet = false },
                    containerColor = CyberzoneSurface,
                    modifier = Modifier.testTag("notifications_list_dialog"),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = CyberzoneElectricGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Notifications",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = CyberzoneTextPrimary
                                )
                            }

                            if (unreadCount > 0) {
                                TextButton(
                                    onClick = { repository.markAllNotificationsAsRead() },
                                    modifier = Modifier.testTag("mark_all_read_button")
                                ) {
                                    Text(
                                        text = "Mark all as read",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = CyberzoneElectricGreen
                                    )
                                }
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                        ) {
                            if (filteredNotifs.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(CyberzoneSurfaceElevated, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No notifications yet.",
                                        fontFamily = InterFontFamily,
                                        fontSize = 13.sp,
                                        color = CyberzoneTextMuted
                                    )
                                }
                            } else {
                                androidx.compose.foundation.lazy.LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredNotifs.size) { index ->
                                        val notif = filteredNotifs[index]
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (!notif.read) CyberzoneElectricGreenSubtle else CyberzoneSurfaceElevated)
                                                .border(1.dp, if (!notif.read) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    repository.markNotificationRead(notif.id)
                                                    if (notif.reportId.isNotBlank()) {
                                                        selectedReportId = notif.reportId
                                                        activeTab = CyberzoneTab.MAP
                                                        showNotificationsSheet = false
                                                    }
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = notif.title.ifBlank { "Notification" },
                                                            fontFamily = InterFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.5.sp,
                                                            color = CyberzoneTextPrimary
                                                        )
                                                        if (!notif.read) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(7.dp)
                                                                    .clip(CircleShape)
                                                                    .background(CyberzoneElectricGreen)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = notif.message,
                                                        fontFamily = InterFontFamily,
                                                        fontSize = 12.sp,
                                                        color = CyberzoneTextSecondary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = dateFormat.format(java.util.Date(notif.createdAt)),
                                                        fontFamily = InterFontFamily,
                                                        fontSize = 10.sp,
                                                        color = CyberzoneTextMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showNotificationsSheet = false }) {
                            Text(
                                text = "Close",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = CyberzoneElectricGreen
                            )
                        }
                    }
                )
            }

            // In-App Push Notification Dialog when Official marks report as RESOLVED
            latestResolvedNotification?.let { resolvedIncident ->
                AlertDialog(
                    onDismissRequest = { repository.dismissResolvedNotification() },
                    containerColor = CyberzoneSurface,
                    modifier = Modifier.testTag("resolved_push_notification_dialog"),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberzoneElectricGreenSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CyberzoneElectricGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "REPORT RESOLVED",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = CyberzoneElectricGreen
                                )
                                Text(
                                    text = "Barangay Official Response",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = CyberzoneTextPrimary
                                )
                            }
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyberzoneSurfaceElevated)
                                    .border(1.dp, CyberzoneElectricGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        GoogleCategoryBadge(resolvedIncident.category, size = 26.dp, iconSize = 14.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = resolvedIncident.label,
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = CyberzoneTextPrimary
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = CyberzoneElectricGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = resolvedIncident.locationName,
                                            fontFamily = InterFontFamily,
                                            fontSize = 12.sp,
                                            color = CyberzoneTextSecondary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Your report has been successfully taken care of by ${resolvedIncident.assignedOfficialName ?: "the Barangay Tanod team"}. Thank you for keeping our community safe!",
                                fontFamily = InterFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = CyberzoneTextSecondary
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { repository.dismissResolvedNotification() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberzoneElectricGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("dismiss_resolved_notif_button")
                        ) {
                            Text(
                                text = "Acknowledge",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                )
            }
        }
    }
}
