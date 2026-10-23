package com.example.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Alert
import com.example.data.model.AlertCategory
import com.example.data.model.AlertStatus
import com.example.data.model.Barangay
import com.example.data.model.LatLng
import com.example.data.model.Tanod
import com.example.data.model.TanodAvailability
import com.example.data.model.User
import com.example.data.model.UserStatus
import com.example.data.repository.PinProRepository
import com.example.ui.components.OutsideHomeBadge
import com.example.ui.components.PinProOutlinedButton
import com.example.ui.components.PinProPrimaryButton
import com.example.ui.components.PinProTextField
import com.example.ui.components.StatusBadge
import com.example.ui.map.PinProMapView
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PinProBackground
import com.example.ui.theme.PinProBorder
import com.example.ui.theme.PinProError
import com.example.ui.theme.PinProNeonGreen
import com.example.ui.theme.PinProSurface
import com.example.ui.theme.PinProTextMuted
import com.example.ui.theme.PinProTextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminTab(val title: String) {
    LIVE_ALERTS("Live Alerts"),
    RESIDENTS("Residents"),
    TANODS("Tanods Roster"),
    ALERT_HISTORY("History")
}

@Composable
fun AdminDashboardScreen(
    admin: User,
    repository: PinProRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barangays by repository.barangays.collectAsState()
    val allAlerts by repository.alerts.collectAsState()
    val allTanods by repository.tanods.collectAsState()
    val allUsers by repository.users.collectAsState()

    val myBarangayId = admin.barangayId ?: "sample_hall"
    val myBarangay = barangays.firstOrNull { it.id == myBarangayId }

    // Admin only sees alerts where receivingBarangayId == myBarangayId
    val barangayAlerts = allAlerts.filter { it.receivingBarangayId == myBarangayId }
    val activeAlerts = barangayAlerts.filter { it.status != AlertStatus.RESOLVED && it.status != AlertStatus.CANCELLED }

    val barangayTanods = allTanods.filter { it.barangayId == myBarangayId }
    val barangayResidents = allUsers.filter { it.homeBarangayId == myBarangayId && it.role == com.example.data.model.UserRole.RESIDENT }

    var selectedTab by remember { mutableStateOf(AdminTab.LIVE_ALERTS) }
    var selectedAlertId by remember { mutableStateOf<String?>(activeAlerts.firstOrNull()?.id) }

    val selectedAlert = barangayAlerts.firstOrNull { it.id == selectedAlertId }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PinProBackground)
    ) {
        // 1. Dashboard Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PinProBackground)
                .border(1.dp, PinProBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PinProNeonGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "PinPro",
                        tint = Color(0xFF0B0B0B),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PinPro",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Green Online dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PinProNeonGreen)
                        )
                    }
                    Text(
                        text = "${myBarangay?.name ?: "Barangay Hall"} Dispatch",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Active count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (activeAlerts.isNotEmpty()) PinProNeonGreen else PinProSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${activeAlerts.size} Active",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0B0B0B)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PinProTextMuted,
                    modifier = Modifier
                        .clickable(onClick = onLogout)
                        .padding(4.dp)
                )
            }
        }

        // 2. Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = PinProBackground,
            contentColor = PinProTextPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = PinProNeonGreen,
                    height = 3.dp
                )
            }
        ) {
            AdminTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            fontFamily = InterFontFamily,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selectedTab == tab) PinProTextPrimary else PinProTextMuted
                        )
                    }
                )
            }
        }

        // 3. Main Tab Contents
        when (selectedTab) {
            AdminTab.LIVE_ALERTS -> {
                AdminLiveAlertsView(
                    alerts = activeAlerts,
                    selectedAlert = selectedAlert,
                    barangays = barangays,
                    myBarangayId = myBarangayId,
                    tanods = barangayTanods,
                    onSelectAlert = { selectedAlertId = it.id },
                    onAssignTanod = { alertId, tanodId -> repository.assignTanod(alertId, tanodId) },
                    onUpdateStatus = { alertId, status -> repository.updateAlertStatus(alertId, status) },
                    onSimulateAlert = { repository.simulateIncomingAlert(myBarangayId) }
                )
            }
            AdminTab.RESIDENTS -> {
                AdminResidentsView(
                    residents = barangayResidents,
                    onToggleSuspend = { residentId, newStatus ->
                        repository.setResidentStatus(residentId, newStatus)
                    }
                )
            }
            AdminTab.TANODS -> {
                AdminTanodsView(
                    tanods = barangayTanods,
                    onToggleAvailability = { tanodId, avail ->
                        repository.toggleTanodAvailability(tanodId, avail)
                    },
                    onAddTanod = { name, badge ->
                        repository.addTanod(myBarangayId, name, badge)
                    }
                )
            }
            AdminTab.ALERT_HISTORY -> {
                AdminHistoryView(alerts = barangayAlerts)
            }
        }
    }
}

@Composable
fun AdminLiveAlertsView(
    alerts: List<Alert>,
    selectedAlert: Alert?,
    barangays: List<Barangay>,
    myBarangayId: String,
    tanods: List<Tanod>,
    onSelectAlert: (Alert) -> Unit,
    onAssignTanod: (String, String) -> Unit,
    onUpdateStatus: (String, AlertStatus) -> Unit,
    onSimulateAlert: () -> Unit
) {
    val context = LocalContext.current
    var showAssignDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Emergency Stream",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PinProTextPrimary
            )
            PinProPrimaryButton(
                text = "+ Simulate Alert",
                onClick = onSimulateAlert,
                modifier = Modifier
                    .width(160.dp)
                    .testTag("admin_simulate_alert_button")
            )
        }

        // Map Overview showing barangay territory, pins, tanods
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            PinProMapView(
                barangays = barangays,
                activeBarangayId = myBarangayId,
                userLocation = selectedAlert?.let { LatLng(it.location.lat, it.location.lng) },
                activeAlert = selectedAlert,
                allAlerts = alerts,
                allTanods = tanods,
                assignedTanod = tanods.firstOrNull { it.uid == selectedAlert?.assignedTanodId },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Incoming Alert Cards (with slide-in green highlight)
        Text(
            text = "Active Incidents (${alerts.size})",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = PinProTextPrimary
        )

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PinProSurface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active emergency alerts in your barangay.",
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    color = PinProTextMuted
                )
            }
        } else {
            alerts.forEach { alert ->
                val isSelected = alert.id == selectedAlert?.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFFF0FDF4) else PinProSurface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) PinProNeonGreen else PinProBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelectAlert(alert) }
                ) {
                    // Left neon green highlight bar for active/selected
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(90.dp)
                            .background(if (isSelected) PinProNeonGreen else Color.Transparent)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${alert.id} • ${alert.category.displayName}",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PinProTextPrimary
                            )
                            StatusBadge(status = alert.status)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Resident: ${alert.residentName} • ${alert.residentMobile}",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextPrimary
                        )

                        Text(
                            text = alert.location.address,
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextMuted
                        )

                        if (alert.fromOutsideHome) {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutsideHomeBadge()
                        }
                    }
                }
            }
        }

        // Selected Alert Detail & Dispatch Actions Panel
        if (selectedAlert != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Dispatch & Control: ${selectedAlert.id}",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PinProTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Note: ${selectedAlert.note.ifEmpty { "No additional notes provided." }}",
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    color = PinProTextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Assign Tanod Dropdown
                val assignedTanod = tanods.firstOrNull { it.uid == selectedAlert.assignedTanodId }
                Text(
                    text = "Assigned Responder: ${assignedTanod?.name ?: "None assigned"}",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = PinProTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    PinProOutlinedButton(
                        text = if (assignedTanod != null) "Reassign Tanod" else "Accept & Assign Tanod",
                        onClick = { showAssignDropdown = true }
                    )

                    DropdownMenu(
                        expanded = showAssignDropdown,
                        onDismissRequest = { showAssignDropdown = false }
                    ) {
                        val availableTanods = tanods.filter { it.availability != TanodAvailability.OFF_DUTY }
                        if (availableTanods.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No tanods on duty", fontFamily = InterFontFamily, fontSize = 12.sp) },
                                onClick = { showAssignDropdown = false }
                            )
                        } else {
                            availableTanods.forEach { tanod ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${tanod.name} (${tanod.badgeNumber}) - ${tanod.availability.name}",
                                            fontFamily = InterFontFamily,
                                            fontSize = 16.sp
                                        )
                                    },
                                    onClick = {
                                        onAssignTanod(selectedAlert.id, tanod.uid)
                                        showAssignDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Update Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PinProPrimaryButton(
                        text = "On the way",
                        enabled = selectedAlert.status == AlertStatus.ASSIGNED,
                        onClick = { onUpdateStatus(selectedAlert.id, AlertStatus.ON_THE_WAY) },
                        modifier = Modifier.weight(1f)
                    )
                    PinProPrimaryButton(
                        text = "Arrived",
                        enabled = selectedAlert.status == AlertStatus.ON_THE_WAY,
                        onClick = { onUpdateStatus(selectedAlert.id, AlertStatus.ARRIVED) },
                        modifier = Modifier.weight(1f)
                    )
                    PinProPrimaryButton(
                        text = "Resolved",
                        enabled = selectedAlert.status == AlertStatus.ARRIVED,
                        onClick = { onUpdateStatus(selectedAlert.id, AlertStatus.RESOLVED) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PinProOutlinedButton(
                    text = "Call Resident (${selectedAlert.residentMobile})",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${selectedAlert.residentMobile}")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun AdminResidentsView(
    residents: List<User>,
    onToggleSuspend: (String, UserStatus) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var residentToConfirm by remember { mutableStateOf<User?>(null) }

    val filtered = residents.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.mobile.contains(searchQuery) ||
                it.address.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Registered Residents (${residents.size})",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = PinProTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        PinProTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Search resident by name or address",
            placeholder = "e.g. Santos or Acacia",
            trailingContent = { Icon(Icons.Default.Search, contentDescription = null, tint = PinProTextMuted) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { res ->
                val isSuspended = res.status == UserStatus.SUSPENDED
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PinProSurface)
                        .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = res.name,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = PinProTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSuspended) Color(0xFFFEE2E2) else Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isSuspended) "Suspended" else "Active",
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSuspended) PinProError else Color(0xFF166534)
                                )
                            }
                        }
                        Text(
                            text = res.address,
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextMuted
                        )
                        Text(
                            text = res.mobile,
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextPrimary
                        )
                    }

                    PinProOutlinedButton(
                        text = if (isSuspended) "Reinstate" else "Suspend",
                        onClick = { residentToConfirm = res },
                        modifier = Modifier.width(110.dp)
                    )
                }
            }
        }
    }

    if (residentToConfirm != null) {
        val target = residentToConfirm!!
        val isSuspended = target.status == UserStatus.SUSPENDED
        AlertDialog(
            onDismissRequest = { residentToConfirm = null },
            title = {
                Text(
                    text = if (isSuspended) "Reinstate Account?" else "Suspend Resident?",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PinProTextPrimary
                )
            },
            text = {
                Text(
                    text = if (isSuspended)
                        "Allow ${target.name} to send emergency alerts to this barangay again?"
                    else
                        "Suspended residents cannot submit new emergency reports. They will be directed to 911 or the barangay hall.",
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    color = PinProTextPrimary
                )
            },
            confirmButton = {
                PinProPrimaryButton(
                    text = if (isSuspended) "Confirm Reinstate" else "Confirm Suspend",
                    onClick = {
                        onToggleSuspend(target.uid, if (isSuspended) UserStatus.ACTIVE else UserStatus.SUSPENDED)
                        residentToConfirm = null
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { residentToConfirm = null }) {
                    Text("Cancel", fontFamily = InterFontFamily, fontSize = 16.sp, color = PinProTextPrimary)
                }
            }
        )
    }
}

@Composable
fun AdminTanodsView(
    tanods: List<Tanod>,
    onToggleAvailability: (String, TanodAvailability) -> Unit,
    onAddTanod: (String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newBadge by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tanods Roster",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PinProTextPrimary
            )
            PinProPrimaryButton(
                text = "+ Add Tanod",
                onClick = { showAddDialog = true },
                modifier = Modifier.width(130.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tanods) { tanod ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PinProSurface)
                        .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${tanod.name} • Badge ${tanod.badgeNumber}",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        )
                        Text(
                            text = "Today's Responses: ${tanod.responseCountToday} | Contact: ${tanod.contact}",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextMuted
                        )
                    }

                    // Availability Toggle Chip
                    val (statusColor, textColor) = when (tanod.availability) {
                        TanodAvailability.AVAILABLE -> PinProNeonGreen to Color(0xFF0B0B0B)
                        TanodAvailability.BUSY -> Color(0xFFFED7AA) to Color(0xFF9A3412)
                        TanodAvailability.OFF_DUTY -> Color(0xFFE5E7EB) to Color(0xFF4B5563)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor)
                            .clickable {
                                val next = when (tanod.availability) {
                                    TanodAvailability.AVAILABLE -> TanodAvailability.OFF_DUTY
                                    TanodAvailability.OFF_DUTY -> TanodAvailability.AVAILABLE
                                    TanodAvailability.BUSY -> TanodAvailability.AVAILABLE
                                }
                                onToggleAvailability(tanod.uid, next)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tanod.availability.name.replace("_", " "),
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Tanod Officer",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PinProTextPrimary
                )
            },
            text = {
                Column {
                    PinProTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = "Full Name",
                        placeholder = "e.g. Cardo Dalisay"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    PinProTextField(
                        value = newBadge,
                        onValueChange = { newBadge = it },
                        label = "Badge Number",
                        placeholder = "e.g. TND-21"
                    )
                }
            },
            confirmButton = {
                PinProPrimaryButton(
                    text = "Save",
                    onClick = {
                        if (newName.isNotBlank() && newBadge.isNotBlank()) {
                            onAddTanod(newName, newBadge)
                            newName = ""
                            newBadge = ""
                            showAddDialog = false
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", fontFamily = InterFontFamily, fontSize = 16.sp, color = PinProTextPrimary)
                }
            }
        )
    }
}

@Composable
fun AdminHistoryView(alerts: List<Alert>) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Barangay Incident Log (${alerts.size})",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = PinProTextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(alerts) { alert ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PinProSurface)
                        .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${alert.id} • ${alert.category.displayName}",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        )
                        StatusBadge(status = alert.status)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Resident: ${alert.residentName} (${alert.residentMobile})",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextPrimary
                    )
                    Text(
                        text = "${alert.location.address} • ${dateFormat.format(Date(alert.createdAt))}",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextMuted
                    )
                }
            }
        }
    }
}
