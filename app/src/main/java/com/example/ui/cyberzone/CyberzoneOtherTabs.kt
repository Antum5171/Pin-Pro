package com.example.ui.cyberzone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.data.model.AppNotification
import com.example.data.model.AppRole
import com.example.data.model.ReportCategory
import com.example.data.model.ReportEvent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimalReportPin
import com.example.data.model.AnimalType
import com.example.data.model.Barangay
import com.example.data.model.PinStatus
import com.example.ui.theme.CyberzoneBackground
import com.example.ui.theme.CyberzoneBorder
import com.example.ui.theme.CyberzoneElectricGreen
import com.example.ui.theme.CyberzoneElectricGreenSubtle
import com.example.ui.theme.CyberzoneError
import com.example.ui.theme.CyberzoneSurface
import com.example.ui.theme.CyberzoneSurfaceElevated
import com.example.ui.theme.CyberzoneTextMuted
import com.example.ui.theme.CyberzoneTextPrimary
import com.example.ui.theme.CyberzoneTextSecondary
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusOnTheWay
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusUrgent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PinPro Home View:
 * Top bar only shows PinPro brand logo (28px, brand green accent).
 * 0 mock reports - counters calculated from real database records (0 when empty).
 */
@Composable
fun CyberzoneHomeFeedView(
    pins: List<AnimalReportPin>,
    onNavigateToMap: () -> Unit,
    onSelectReport: (String) -> Unit = {},
    onTogglePinStatus: (String) -> Unit = {},
    assignedBarangay: Barangay? = null,
    currentRole: AppRole = AppRole.RESIDENT,
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf<PinStatus?>(null) }

    val pendingCount = pins.count { it.status == PinStatus.PENDING || it.status == PinStatus.ACCEPTED || it.status == PinStatus.ON_THE_WAY || it.status == PinStatus.ARRIVED }
    val doneCount = pins.count { it.status == PinStatus.RESOLVED }

    val filteredPins = pins.filter { filterStatus == null || it.status == filterStatus }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
    ) {
        // TOP BAR: PinPro Logo and Title (noticeably bigger and bolder, 28px, brand green accent)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "PinPro Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "PinPro",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = CyberzoneElectricGreen
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Home Summary Card & Report Shortcut
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Barangay Report Tracking System",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CyberzoneTextPrimary
                    )
                    Text(
                        text = "Report community concerns and track official response in real time",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = CyberzoneTextSecondary
                    )

                    // Two Stat Tiles (Pending / Resolved)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pending Stat Tile
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CyberzoneSurface)
                                .border(
                                    1.dp,
                                    if (filterStatus == PinStatus.PENDING) StatusPending else CyberzoneBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    filterStatus = if (filterStatus == PinStatus.PENDING) null else PinStatus.PENDING
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PENDING",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp,
                                        color = StatusPending
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(StatusPending)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$pendingCount",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = CyberzoneTextPrimary
                                )
                                Text(
                                    text = "Active concerns",
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    color = CyberzoneTextSecondary
                                )
                            }
                        }

                        // Done Stat Tile
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CyberzoneSurface)
                                .border(
                                    1.dp,
                                    if (filterStatus == PinStatus.RESOLVED) StatusDone else CyberzoneBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    filterStatus = if (filterStatus == PinStatus.RESOLVED) null else PinStatus.RESOLVED
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "DONE",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp,
                                        color = StatusDone
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(StatusDone)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "$doneCount",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = CyberzoneTextPrimary
                                )
                                Text(
                                    text = "Resolved cases",
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    color = CyberzoneTextSecondary
                                )
                            }
                        }
                    }

                    // "Report incident" shortcut button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberzoneSurfaceElevated)
                            .border(1.dp, CyberzoneElectricGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onNavigateToMap)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = CyberzoneElectricGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Report incident",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = CyberzoneElectricGreen
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = CyberzoneElectricGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Area Reports",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CyberzoneTextPrimary
                        )
                    }

                    if (filterStatus != null) {
                        Text(
                            text = "Show All",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberzoneElectricGreen,
                            modifier = Modifier
                                .clickable { filterStatus = null }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Real-Time Reports List or Empty State ("No reports yet")
            if (filteredPins.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberzoneSurface)
                            .border(1.dp, CyberzoneBorder, RoundedCornerShape(16.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(CyberzoneSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = CyberzoneTextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No reports yet",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CyberzoneTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "When residents submit community concerns, they will appear here in real time.",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = CyberzoneTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredPins) { pin ->
                    AnimalNotificationCard(
                        pin = pin,
                        onNavigateToMap = {
                            onSelectReport(pin.id)
                            onNavigateToMap()
                        },
                        onToggleStatus = { onTogglePinStatus(pin.id) },
                        onSelectCard = {
                            onSelectReport(pin.id)
                            onNavigateToMap()
                        },
                        currentRole = currentRole
                    )
                }
            }
        }
    }
}

/**
 * Read-Only Details Dialog for Resolved Reports (No map navigation, no Accept/Locate buttons).
 */
@Composable
fun ReadOnlyResolvedReportDialog(
    pin: AnimalReportPin,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberzoneSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GoogleCategoryBadge(pin.category, size = 32.dp, iconSize = 18.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        GoogleStatusBadge(status = PinStatus.RESOLVED)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pin.title.ifBlank { pin.label },
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CyberzoneTextPrimary
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Location & Reporter Details
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberzoneSurfaceElevated)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CyberzoneElectricGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pin.locationName.ifBlank { "Barangay Sector" },
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = CyberzoneTextPrimary
                            )
                        }
                        Text(
                            text = "Reported by ${pin.reportedBy} • ${dateFormat.format(Date(pin.timestamp))}",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            color = CyberzoneTextMuted
                        )
                    }
                }

                if (pin.description.isNotBlank()) {
                    Column {
                        Text(
                            text = "DESCRIPTION",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.5.sp,
                            color = CyberzoneElectricGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pin.description,
                            fontFamily = InterFontFamily,
                            fontSize = 12.5.sp,
                            color = CyberzoneTextSecondary
                        )
                    }
                }

                // Incident Status Timeline
                Column {
                    Text(
                        text = "INCIDENT TIMELINE",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.5.sp,
                        color = CyberzoneElectricGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val steps = listOf("Reported", "Accepted", "Dispatched", "Arrived", "Resolved")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberzoneSurfaceElevated)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { idx, title ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(CyberzoneElectricGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = title,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp,
                                    color = CyberzoneElectricGreen
                                )
                            }
                        }
                    }
                }

                // Responding Official & Resolution Note Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberzoneElectricGreenSubtle)
                        .border(1.dp, CyberzoneElectricGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CyberzoneElectricGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INCIDENT RESOLVED",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = CyberzoneElectricGreen
                            )
                        }
                        Text(
                            text = "Responding Official: ${if (pin.assignedOfficialName.isNullOrBlank()) "Barangay Tanod Response Unit" else pin.assignedOfficialName}",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = CyberzoneTextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
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

/**
 * Incident Report Card with auto-width, single-line status pill and dedicated action row.
 */
@Composable
fun AnimalNotificationCard(
    pin: AnimalReportPin,
    onNavigateToMap: () -> Unit,
    onToggleStatus: () -> Unit,
    onSelectCard: () -> Unit = {},
    currentRole: AppRole = AppRole.RESIDENT,
    modifier: Modifier = Modifier
) {
    val isResolved = pin.status == PinStatus.RESOLVED
    val isPending = pin.status == PinStatus.PENDING
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    var showResolvedDialog by remember { mutableStateOf(false) }

    if (showResolvedDialog) {
        ReadOnlyResolvedReportDialog(
            pin = pin,
            onDismiss = { showResolvedDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberzoneSurface)
            .border(1.dp, CyberzoneBorder, RoundedCornerShape(14.dp))
            .clickable {
                if (isResolved) {
                    showResolvedDialog = true
                } else {
                    onSelectCard()
                }
            }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoogleCategoryBadge(pin.category, size = 36.dp, iconSize = 18.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = pin.title.ifBlank { pin.label },
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CyberzoneTextPrimary
                    )
                    Text(
                        text = pin.locationName.ifBlank { "Barangay Sector" },
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = CyberzoneTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            GoogleStatusBadge(status = pin.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = pin.description,
            fontFamily = InterFontFamily,
            fontSize = 13.sp,
            color = CyberzoneTextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Reported by ${pin.reportedBy} • ${dateFormat.format(Date(pin.timestamp))}",
            fontFamily = InterFontFamily,
            fontSize = 11.sp,
            color = CyberzoneTextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isResolved) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberzoneSurfaceElevated)
                    .border(1.dp, CyberzoneBorder, RoundedCornerShape(10.dp))
                    .clickable { showResolvedDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusDone,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "View Resolved Record",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusDone
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberzoneSurfaceElevated)
                        .border(1.dp, CyberzoneBorder, RoundedCornerShape(10.dp))
                        .clickable(onClick = onNavigateToMap),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Locate",
                            tint = CyberzoneTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Locate",
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberzoneTextPrimary
                        )
                    }
                }

                if (currentRole == AppRole.OFFICIAL) {
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPending) CyberzoneElectricGreen else CyberzoneSurfaceElevated)
                            .border(
                                1.dp,
                                if (isPending) CyberzoneElectricGreen else CyberzoneBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(onClick = onToggleStatus),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isPending) Icons.Default.CheckCircle else Icons.Default.Refresh,
                                contentDescription = null,
                                tint = if (isPending) Color.Black else CyberzoneElectricGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPending) "Accept Response" else "Update Status",
                                fontFamily = InterFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPending) Color.Black else CyberzoneElectricGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Barangay Incident Search View with Status Filter chips & empty state ("No reports yet").
 */
@Composable
fun CyberzoneSearchView(
    pins: List<AnimalReportPin>,
    onSelectPin: (AnimalReportPin) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ReportCategory?>(null) }
    var statusFilter by remember { mutableStateOf("All") } // "All", "Pending", "Active", "Resolved"
    var selectedResolvedPin by remember { mutableStateOf<AnimalReportPin?>(null) }

    if (selectedResolvedPin != null) {
        ReadOnlyResolvedReportDialog(
            pin = selectedResolvedPin!!,
            onDismiss = { selectedResolvedPin = null }
        )
    }

    val filteredPins = pins.filter { pin ->
        val matchesCategory = selectedCategory == null || pin.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                pin.title.contains(searchQuery, ignoreCase = true) ||
                pin.label.contains(searchQuery, ignoreCase = true) ||
                pin.description.contains(searchQuery, ignoreCase = true) ||
                pin.locationName.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (statusFilter) {
            "Pending" -> pin.status == PinStatus.PENDING
            "Active" -> pin.status == PinStatus.ACCEPTED || pin.status == PinStatus.ON_THE_WAY || pin.status == PinStatus.ARRIVED
            "Resolved" -> pin.status == PinStatus.RESOLVED
            else -> true
        }
        matchesCategory && matchesSearch && matchesStatus
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // TOP BAR: PinPro Logo and Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "PinPro Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "PinPro",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = CyberzoneElectricGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberzoneSurface)
                .border(1.dp, CyberzoneBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CyberzoneElectricGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search reports by keyword, street...",
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            color = CyberzoneTextMuted
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            color = CyberzoneTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )
                }
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = CyberzoneTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips (All / Pending / Active / Resolved)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("All", "Pending", "Active", "Resolved").forEach { statusOpt ->
                val isSelected = statusFilter == statusOpt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) CyberzoneElectricGreen else CyberzoneSurface)
                        .border(1.dp, if (isSelected) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(16.dp))
                        .clickable { statusFilter = statusOpt }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusOpt,
                        fontFamily = InterFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = if (isSelected) Color.Black else CyberzoneTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isAllSelected = selectedCategory == null
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(if (isAllSelected) CyberzoneElectricGreen else CyberzoneSurface)
                        .border(1.dp, if (isAllSelected) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(17.dp))
                        .clickable { selectedCategory = null }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All Categories",
                        fontFamily = InterFontFamily,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = if (isAllSelected) Color.Black else CyberzoneTextPrimary
                    )
                }
            }

            items(ReportCategory.entries.toTypedArray()) { cat ->
                val isSelected = selectedCategory == cat
                val catIcon = GoogleIconPack.getCategoryIcon(cat)
                val catColor = GoogleIconPack.getCategoryColor(cat)

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(if (isSelected) CyberzoneElectricGreen else CyberzoneSurface)
                        .border(1.dp, if (isSelected) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(17.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else catColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = cat.displayName,
                            fontFamily = InterFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = if (isSelected) Color.Black else CyberzoneTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Empty state vs Results
        if (filteredPins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(CyberzoneSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CyberzoneTextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No reports yet",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = CyberzoneTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching with a different term or adjust your filters.",
                        fontFamily = InterFontFamily,
                        fontSize = 13.sp,
                        color = CyberzoneTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPins) { pin ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberzoneSurface)
                            .border(1.dp, CyberzoneBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                if (pin.status == PinStatus.RESOLVED) {
                                    selectedResolvedPin = pin
                                } else {
                                    onSelectPin(pin)
                                }
                            }
                            .padding(14.dp)
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
                                GoogleCategoryBadge(pin.category, size = 30.dp, iconSize = 16.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = pin.title.ifBlank { pin.label },
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = CyberzoneTextPrimary
                                    )
                                    Text(
                                        text = pin.locationName.ifBlank { "Barangay Sector" },
                                        fontFamily = InterFontFamily,
                                        fontSize = 11.sp,
                                        color = CyberzoneTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            GoogleStatusBadge(status = pin.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = pin.description,
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = CyberzoneTextSecondary,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Profile View with Role Switcher & Debug / Test Log Screen.
 */
@Composable
fun CyberzoneProfileView(
    username: String,
    pinCount: Int,
    reportEvents: List<ReportEvent> = emptyList(),
    notifications: List<AppNotification> = emptyList(),
    assignedBarangay: Barangay? = null,
    currentRole: AppRole = AppRole.RESIDENT,
    onSetRole: (AppRole) -> Unit = {},
    onOpenLocationPicker: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDebugLogDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP BAR: PinPro Logo & Profile Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "PinPro Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "PinPro",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = CyberzoneElectricGreen
                )
            }

            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CyberzoneSurface)
                    .border(1.dp, CyberzoneBorder, CircleShape)
                    .testTag("profile_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = CyberzoneElectricGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Role Segmented Control (Resident / Official) for Testing
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberzoneSurface)
                .border(1.dp, CyberzoneBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "ACTIVE TESTING ROLE",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = CyberzoneElectricGreen
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isResident = currentRole == AppRole.RESIDENT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isResident) CyberzoneElectricGreen else CyberzoneSurfaceElevated)
                        .border(1.dp, if (isResident) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(10.dp))
                        .clickable { onSetRole(AppRole.RESIDENT) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Resident 👤",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isResident) Color.Black else CyberzoneTextPrimary
                    )
                }

                val isOfficial = currentRole == AppRole.OFFICIAL
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isOfficial) CyberzoneElectricGreen else CyberzoneSurfaceElevated)
                        .border(1.dp, if (isOfficial) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(10.dp))
                        .clickable { onSetRole(AppRole.OFFICIAL) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Official 🛡️",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isOfficial) Color.Black else CyberzoneTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Debug / Test Audit Log Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberzoneSurfaceElevated)
                .border(1.dp, CyberzoneElectricGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable { showDebugLogDialog = true }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = CyberzoneElectricGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📜 Debug / Test Log Screen",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CyberzoneTextPrimary
                        )
                        Text(
                            text = "Events (${reportEvents.size}) • Notifications (${notifications.size})",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            color = CyberzoneTextSecondary
                        )
                    }
                }
                Text(
                    text = "View",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CyberzoneElectricGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberzoneSurface)
                .border(1.dp, CyberzoneBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CyberProfileStat(number = "$pinCount", label = "Total Reports")
            CyberProfileStat(number = "${reportEvents.size}", label = "Audit Events")
            CyberProfileStat(number = "${notifications.size}", label = "Sent Notifs")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Debug / Test Log Screen Dialog
    if (showDebugLogDialog) {
        val dateFormat = remember { SimpleDateFormat("MMM dd, hh:mm:ss a", Locale.getDefault()) }
        var logTab by remember { mutableStateOf("events") } // "events" or "notifs"

        AlertDialog(
            onDismissRequest = { showDebugLogDialog = false },
            containerColor = CyberzoneSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = CyberzoneElectricGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Debug / Test Audit Log",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CyberzoneTextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    // Log Type Switcher (Report Events vs Notifications)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberzoneSurfaceElevated)
                            .border(1.dp, CyberzoneBorder, RoundedCornerShape(10.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (logTab == "events") CyberzoneElectricGreen else Color.Transparent)
                                .clickable { logTab = "events" }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Events (${reportEvents.size})",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (logTab == "events") Color.Black else CyberzoneTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (logTab == "notifs") CyberzoneElectricGreen else Color.Transparent)
                                .clickable { logTab = "notifs" }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sent Notifs (${notifications.size})",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (logTab == "notifs") Color.Black else CyberzoneTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (logTab == "events") {
                        if (reportEvents.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CyberzoneSurfaceElevated, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No report events recorded yet.",
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    color = CyberzoneTextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(reportEvents) { evt ->
                                    val badgeColor = when (evt.type) {
                                        "submitted" -> StatusDone
                                        "accepted" -> StatusOnTheWay
                                        "location_update" -> Color(0xFF8B5CF6)
                                        "arrived" -> StatusPending
                                        "resolved" -> StatusDone
                                        else -> CyberzoneTextSecondary
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CyberzoneSurfaceElevated)
                                            .border(1.dp, CyberzoneBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(badgeColor.copy(alpha = 0.2f))
                                                    .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = evt.type.uppercase(),
                                                    fontFamily = InterFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = badgeColor
                                                )
                                            }

                                            Text(
                                                text = dateFormat.format(Date(evt.timestamp)),
                                                fontFamily = InterFontFamily,
                                                fontSize = 10.sp,
                                                color = CyberzoneTextMuted
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "${evt.actorName} (${evt.actorRole})",
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = CyberzoneTextPrimary
                                        )

                                        evt.note?.let { n ->
                                            Text(
                                                text = n,
                                                fontFamily = InterFontFamily,
                                                fontSize = 11.sp,
                                                color = CyberzoneTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CyberzoneSurfaceElevated, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No sent notifications yet.",
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    color = CyberzoneTextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(notifications) { notif ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CyberzoneSurfaceElevated)
                                            .border(1.dp, CyberzoneBorder, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(CyberzoneElectricGreen.copy(alpha = 0.2f))
                                                    .border(1.dp, CyberzoneElectricGreen, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "TO: ${notif.recipientId}",
                                                    fontFamily = InterFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = CyberzoneElectricGreen
                                                )
                                            }

                                            Text(
                                                text = dateFormat.format(Date(notif.createdAt)),
                                                fontFamily = InterFontFamily,
                                                fontSize = 10.sp,
                                                color = CyberzoneTextMuted
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = notif.title.ifBlank { "Notification (${notif.type})" },
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = CyberzoneTextPrimary
                                        )

                                        Text(
                                            text = notif.message,
                                            fontFamily = InterFontFamily,
                                            fontSize = 11.sp,
                                            color = CyberzoneTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDebugLogDialog = false }) {
                    Text(text = "Close", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, color = CyberzoneElectricGreen)
                }
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = CyberzoneSurface,
            title = {
                Text(text = "Settings", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, color = CyberzoneTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Account: $username", fontFamily = InterFontFamily, fontSize = 13.sp, color = CyberzoneTextSecondary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF261616))
                            .clickable {
                                showSettingsDialog = false
                                onLogout()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "SIGN OUT", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyberzoneError)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(text = "Done", fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, color = CyberzoneElectricGreen)
                }
            }
        )
    }
}

@Composable
fun CyberProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = CyberzoneElectricGreen
        )
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            color = CyberzoneTextMuted
        )
    }
}
