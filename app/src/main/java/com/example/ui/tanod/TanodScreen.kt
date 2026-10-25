package com.example.ui.tanod

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Alert
import com.example.data.model.AlertStatus
import com.example.data.model.Barangay
import com.example.data.model.LatLng
import com.example.data.model.Tanod
import com.example.data.model.TanodAvailability
import com.example.data.model.User
import com.example.data.repository.PinProRepository
import com.example.ui.components.PinProOutlinedButton
import com.example.ui.components.PinProPrimaryButton
import com.example.ui.components.StatusBadge
import com.example.ui.map.PinProMapView
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PinProBackground
import com.example.ui.theme.PinProBorder
import com.example.ui.theme.PinProNeonGreen
import com.example.ui.theme.PinProSurface
import com.example.ui.theme.PinProTextMuted
import com.example.ui.theme.PinProTextPrimary

@Composable
fun TanodScreen(
    tanodUser: User,
    repository: PinProRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barangays by repository.barangays.collectAsState()
    val allTanods by repository.tanods.collectAsState()
    val allAlerts by repository.alerts.collectAsState()

    val tanodRecord = allTanods.firstOrNull { it.uid == tanodUser.uid }
        ?: Tanod(uid = tanodUser.uid, name = tanodUser.name, badgeNumber = tanodUser.officialId ?: "TND-01")

    val myBarangay = barangays.firstOrNull { it.id == tanodRecord.barangayId }

    // Alerts assigned to this tanod
    val myAssignments = allAlerts.filter { it.assignedTanodId == tanodUser.uid && it.status != AlertStatus.CANCELLED }
    val activeAssignment = myAssignments.firstOrNull { it.status != AlertStatus.RESOLVED }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PinProBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PinProBackground)
                .border(1.dp, PinProBorder)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PinProNeonGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Badge",
                        tint = Color(0xFF0B0B0B),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = tanodRecord.name,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PinProTextPrimary
                    )
                    Text(
                        text = "Badge ${tanodRecord.badgeNumber} • ${myBarangay?.name ?: "Barangay Tanod"}",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (tanodRecord.availability == TanodAvailability.OFF_DUTY) "Off Duty" else "On Duty",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (tanodRecord.availability == TanodAvailability.OFF_DUTY) PinProTextMuted else PinProTextPrimary
                    )
                    Switch(
                        checked = tanodRecord.availability != TanodAvailability.OFF_DUTY,
                        onCheckedChange = { isOnDuty ->
                            repository.toggleTanodAvailability(tanodRecord.uid)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF0B0B0B),
                            checkedTrackColor = PinProNeonGreen
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = PinProTextMuted,
                    modifier = Modifier
                        .clickable(onClick = onLogout)
                        .padding(4.dp)
                )
            }
        }

        // Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeAssignment != null) {
                // Active Assignment Card with Map and Status buttons
                val alert = activeAssignment
                Text(
                    text = "Active Assignment: ${alert.id}",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PinProTextPrimary
                )

                // Mini Route Map
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    PinProMapView(
                        barangays = barangays,
                        activeBarangayId = alert.receivingBarangayId,
                        userLocation = LatLng(alert.location.lat, alert.location.lng),
                        assignedTanod = tanodRecord,
                        activeAlert = alert,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Resident Details & Emergency Note
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PinProSurface)
                        .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alert.category.displayName,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        )
                        StatusBadge(status = alert.status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Resident: ${alert.residentName}",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PinProTextPrimary
                    )
                    Text(
                        text = alert.location.address,
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Notes: ${alert.note.ifEmpty { "None" }}",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextPrimary
                    )

                    if (alert.etaMinutes != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Estimated Time of Arrival: ${alert.etaMinutes} mins",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Call Resident Button
                    PinProOutlinedButton(
                        text = "Call Resident (${alert.residentMobile})",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${alert.residentMobile}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                // Status Transition Action Buttons
                Text(
                    text = "Update Dispatch Status",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = PinProTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PinProPrimaryButton(
                        text = "On the way",
                        enabled = alert.status == AlertStatus.ASSIGNED,
                        onClick = { repository.updateAlertStatus(alert.id, AlertStatus.ON_THE_WAY) },
                        modifier = Modifier.weight(1f)
                    )
                    PinProPrimaryButton(
                        text = "Arrived",
                        enabled = alert.status == AlertStatus.ON_THE_WAY,
                        onClick = { repository.updateAlertStatus(alert.id, AlertStatus.ARRIVED) },
                        modifier = Modifier.weight(1f)
                    )
                    PinProPrimaryButton(
                        text = "Resolved",
                        enabled = alert.status == AlertStatus.ARRIVED,
                        onClick = { repository.updateAlertStatus(alert.id, AlertStatus.RESOLVED) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PinProSurface)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PinProTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active emergency alerts assigned.",
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = PinProTextMuted
                        )
                        Text(
                            text = "Stand by for barangay dispatcher assignments.",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextMuted
                        )
                    }
                }
            }

            // Past Completed Today
            Text(
                text = "Resolved Incidents Today (${tanodRecord.responseCountToday})",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PinProTextPrimary
            )

            val completed = myAssignments.filter { it.status == AlertStatus.RESOLVED }
            if (completed.isEmpty()) {
                Text(
                    text = "No completed assignments yet today.",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = PinProTextMuted
                )
            } else {
                completed.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PinProSurface)
                            .border(1.dp, PinProBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${item.id} • ${item.category.displayName}",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = PinProTextPrimary
                            )
                            Text(
                                text = "Resident: ${item.residentName}",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = PinProTextMuted
                            )
                        }
                        StatusBadge(status = AlertStatus.RESOLVED)
                    }
                }
            }
        }
    }
}
