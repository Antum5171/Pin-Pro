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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimalReportPin
import com.example.data.model.AnimalType
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PinPro Home View:
 * 1. Live Radar scanning area at the top
 * 2. Area Notifications feed displaying whether dangerous animal pins are PENDING or DONE
 * 3. Interactive toggle to mark pins as Done or Pending
 */
@Composable
fun CyberzoneHomeFeedView(
    pins: List<AnimalReportPin>,
    onNavigateToMap: () -> Unit,
    onTogglePinStatus: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var filterStatus by remember { mutableStateOf<PinStatus?>(null) } // null = all

    val pendingCount = pins.count { it.status == PinStatus.PENDING }
    val doneCount = pins.count { it.status == PinStatus.DONE }

    val filteredPins = pins.filter { filterStatus == null || it.status == filterStatus }

    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarPulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PinPro",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = CyberzoneTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberzoneElectricGreen)
                )
            }

            IconButton(onClick = onNavigateToMap) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Map View",
                    tint = CyberzoneElectricGreen
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Live Radar Top Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberzoneSurface)
                        .border(1.dp, CyberzoneElectricGreen.copy(alpha = radarPulse), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CyberzoneElectricGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LIVE ANIMAL HAZARD RADAR",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.2.sp,
                                    color = CyberzoneElectricGreen
                                )
                            }

                            Text(
                                text = "SCANNING ACTIVE",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = CyberzoneTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Dangerous Animal Sighting Alerts",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CyberzoneTextPrimary
                        )

                        Text(
                            text = "Monitor confirmed sightings of snakes, aggressive dogs, and hazards in your immediate neighborhood.",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = CyberzoneTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF261818))
                                    .border(1.dp, Color(0xFF6B2B2B), RoundedCornerShape(10.dp))
                                    .clickable {
                                        filterStatus = if (filterStatus == PinStatus.PENDING) null else PinStatus.PENDING
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "$pendingCount PENDING",
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = CyberzoneError
                                        )
                                        Text(
                                            text = "Active Threats",
                                            fontFamily = InterFontFamily,
                                            fontSize = 12.sp,
                                            color = CyberzoneTextMuted
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = CyberzoneError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF142416))
                                    .border(1.dp, Color(0xFF26592B), RoundedCornerShape(10.dp))
                                    .clickable {
                                        filterStatus = if (filterStatus == PinStatus.DONE) null else PinStatus.DONE
                                    }
                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "$doneCount DONE",
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = CyberzoneElectricGreen
                                        )
                                        Text(
                                            text = "Secured / Handled",
                                            fontFamily = InterFontFamily,
                                            fontSize = 12.sp,
                                            color = CyberzoneTextMuted
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = CyberzoneElectricGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Notifications Section Header
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
                            text = "Area Hazard Notifications",
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

            // 3. Animal Pin Notifications Feed
            items(filteredPins) { pin ->
                AnimalNotificationCard(
                    pin = pin,
                    onNavigateToMap = onNavigateToMap,
                    onToggleStatus = { onTogglePinStatus(pin.id) }
                )
            }
        }
    }
}

/**
 * Notification Card showing whether a reported dangerous animal is Pending or Done.
 */
@Composable
fun AnimalNotificationCard(
    pin: AnimalReportPin,
    onNavigateToMap: () -> Unit,
    onToggleStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPending = pin.status == PinStatus.PENDING
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberzoneSurface)
            .border(
                width = 1.dp,
                color = if (isPending) CyberzoneElectricGreen.copy(alpha = 0.6f) else CyberzoneBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pin.animalType.emoji,
                    fontSize = 20.sp
                )
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
                        text = pin.locationName.ifBlank { "Spotted near neighborhood perimeter" },
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = CyberzoneTextSecondary
                    )
                }
            }

            // Pending vs Done Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPending) CyberzoneElectricGreen else Color(0xFF282828))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isPending) "PENDING" else "DONE",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isPending) Color(0xFF121212) else CyberzoneTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = pin.description,
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            color = CyberzoneTextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reported by ${pin.reportedBy} • ${dateFormat.format(Date(pin.timestamp))}",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = CyberzoneTextMuted
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Interactive Status Switcher: Mark as Done / Pending
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberzoneSurfaceElevated)
                        .border(1.dp, CyberzoneBorder, RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleStatus)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isPending) "Mark as Done" else "Re-open (Pending)",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isPending) CyberzoneElectricGreen else CyberzoneTextSecondary
                    )
                }

                // Locate on Map Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberzoneElectricGreenSubtle)
                        .clickable(onClick = onNavigateToMap)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Locate",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberzoneElectricGreen
                    )
                }
            }
        }
    }
}

/**
 * Dangerous Animal Sighting Search & Category Filter View.
 */
@Composable
fun CyberzoneSearchView(
    pins: List<AnimalReportPin>,
    onSelectPin: (AnimalReportPin) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<AnimalType?>(null) }

    val filteredPins = pins.filter { pin ->
        (selectedType == null || pin.animalType == selectedType) &&
                (searchQuery.isBlank() ||
                        pin.label.contains(searchQuery, ignoreCase = true) ||
                        pin.description.contains(searchQuery, ignoreCase = true) ||
                        pin.locationName.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Search Animal Reports",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = CyberzoneTextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        CyberInputField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "SEARCH SIGHTINGS",
            placeholder = "Search snake, aggressive dog, street...",
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CyberzoneElectricGreen)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Animal Type Filters (Snake, Aggressive Dog, etc.)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedType == null) CyberzoneElectricGreen else CyberzoneSurface)
                        .border(1.dp, if (selectedType == null) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedType = null }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "All Hazards",
                        fontFamily = InterFontFamily,
                        fontWeight = if (selectedType == null) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (selectedType == null) Color(0xFF121212) else CyberzoneTextPrimary
                    )
                }
            }

            items(AnimalType.values()) { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyberzoneElectricGreen else CyberzoneSurface)
                        .border(1.dp, if (isSelected) CyberzoneElectricGreen else CyberzoneBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedType = type }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${type.emoji} ${type.displayName}",
                        fontFamily = InterFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isSelected) Color(0xFF121212) else CyberzoneTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results List
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
                        .clickable { onSelectPin(pin) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = pin.animalType.emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pin.label,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CyberzoneTextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (pin.status == PinStatus.PENDING) CyberzoneElectricGreen else CyberzoneSurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = pin.status.name,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (pin.status == PinStatus.PENDING) Color(0xFF121212) else CyberzoneTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pin.description,
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = CyberzoneTextSecondary
                    )
                }
            }
        }
    }
}

/**
 * PinPro Operative / Resident Profile View.
 */
@Composable
fun CyberzoneProfileView(
    username: String,
    pinCount: Int,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "User Profile",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = CyberzoneTextPrimary,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Avatar with Electric Green Ring
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            CyberzoneElectricGreen.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.5.dp, CyberzoneElectricGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CyberzoneSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.take(2).uppercase(),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = CyberzoneElectricGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = username,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = CyberzoneTextPrimary
        )

        Text(
            text = "PinPro Community Reporter",
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            color = CyberzoneTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

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
            CyberProfileStat(number = "$pinCount", label = "Animal Pins")
            CyberProfileStat(number = "100%", label = "Verified")
            CyberProfileStat(number = "Active", label = "Radar Status")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Emergency Protocol Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberzoneSurface)
                .border(1.dp, CyberzoneBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "DANGEROUS ANIMAL SAFETY PROTOCOL",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = CyberzoneElectricGreen
            )

            Text(
                text = "• Snakes: Do not agitate or corner. Keep 5-meter distance.\n• Aggressive Dogs: Avoid direct eye contact. Back away slowly.\n• For urgent animal capture, contact local barangay animal control or dial 911.",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                color = CyberzoneTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Logout Action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF261616))
                .border(1.dp, Color(0xFF552222), RoundedCornerShape(14.dp))
                .clickable(onClick = onLogout),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SIGN OUT",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = CyberzoneError
            )
        }
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
