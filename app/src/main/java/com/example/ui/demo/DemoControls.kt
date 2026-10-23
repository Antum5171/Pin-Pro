package com.example.ui.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DemoLocationOverride
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.PinProRepository
import com.example.ui.components.PinProOutlinedButton
import com.example.ui.components.PinProPrimaryButton
import com.example.ui.components.PinProTextField
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PinProBackground
import com.example.ui.theme.PinProBorder
import com.example.ui.theme.PinProError
import com.example.ui.theme.PinProNeonGreen
import com.example.ui.theme.PinProSurface
import com.example.ui.theme.PinProTextMuted
import com.example.ui.theme.PinProTextPrimary

/**
 * Floating demo tools bar providing:
 * 1. "Demo: set my location" dropdown
 * 2. "Demo accounts" panel with 1-click login
 * 3. "Seed demo data" and "Reset demo" buttons
 */
@Composable
fun DemoControlsBar(
    repository: PinProRepository,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val locationOverride by repository.locationOverride.collectAsState()
    val allUsers by repository.users.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var showLocationMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(Color(0xFF0B0B0B))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Toggle bar header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Demo Controls",
                    tint = PinProNeonGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Demo Panel: ${currentUser?.name ?: "Guest"} (${currentUser?.role?.name ?: "None"})",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Location selector dropdown button
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF262626))
                            .clickable { showLocationMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PinProNeonGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = locationOverride.label.take(18) + "...",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showLocationMenu,
                        onDismissRequest = { showLocationMenu = false }
                    ) {
                        DemoLocationOverride.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = mode.label,
                                        fontFamily = InterFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (locationOverride == mode) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    repository.setLocationOverride(mode)
                                    showLocationMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Expanded Panel: 1-Click Login accounts + Seed / Reset buttons
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "One-Click Demo Accounts Switcher:",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Accounts grouped by role
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Residents
                    Text(
                        text = "RESIDENTS (Alert & Tracking):",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinProNeonGreen
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allUsers.filter { it.role == UserRole.RESIDENT }.forEach { user ->
                            DemoAccountChip(
                                user = user,
                                isSelected = currentUser?.uid == user.uid,
                                onSelect = {
                                    repository.loginAs(user)
                                    isExpanded = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Admins
                    Text(
                        text = "BARANGAY ADMINS / DISPATCHERS:",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinProNeonGreen
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allUsers.filter { it.role == UserRole.ADMIN }.forEach { user ->
                            DemoAccountChip(
                                user = user,
                                isSelected = currentUser?.uid == user.uid,
                                onSelect = {
                                    repository.loginAs(user)
                                    isExpanded = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tanods
                    Text(
                        text = "TANOD RESPONDERS:",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinProNeonGreen
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allUsers.filter { it.role == UserRole.TANOD }.take(3).forEach { user ->
                            DemoAccountChip(
                                user = user,
                                isSelected = currentUser?.uid == user.uid,
                                onSelect = {
                                    repository.loginAs(user)
                                    isExpanded = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seed & Reset demo buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF262626))
                            .clickable { repository.seedDemoData() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Seed Demo Data",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF450A0A))
                            .clickable { repository.seedDemoData() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reset Demo",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFCA5A5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DemoAccountChip(
    user: User,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PinProNeonGreen else Color(0xFF262626))
            .clickable(onClick = onSelect)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = user.name.split(" ").firstOrNull() ?: user.name,
                fontFamily = InterFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp,
                color = if (isSelected) Color(0xFF0B0B0B) else Color.White
            )
            Text(
                text = if (user.barangayId == "riverside" || user.homeBarangayId == "riverside") "Riverside" else "Sample Hall",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = if (isSelected) Color(0xFF0B0B0B) else Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun OfficialLoginDialog(
    onDismiss: () -> Unit,
    repository: PinProRepository,
    onLoginSuccess: (User) -> Unit
) {
    val allUsers by repository.users.collectAsState()
    var officialId by remember { mutableStateOf("KAP-01-SH") }
    var password by remember { mutableStateOf("admin123") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Barangay Official Login",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PinProTextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Sign in with your Barangay Official ID or Tanod Badge ID.",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = PinProTextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                PinProTextField(
                    value = officialId,
                    onValueChange = { officialId = it },
                    label = "Official ID / Badge",
                    placeholder = "e.g. KAP-01-SH or TND-SH-04"
                )
                Spacer(modifier = Modifier.height(10.dp))
                PinProTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Enter official password"
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProError
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Quick Demo Officials:",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PinProTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PinProSurface)
                            .border(1.dp, PinProBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                officialId = "KAP-01-SH"
                            }
                            .padding(6.dp)
                    ) {
                        Text("Kapitan Ramon", fontFamily = InterFontFamily, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PinProSurface)
                            .border(1.dp, PinProBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                officialId = "TND-SH-04"
                            }
                            .padding(6.dp)
                    ) {
                        Text("Tanod Berto", fontFamily = InterFontFamily, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            PinProPrimaryButton(
                text = "Sign In as Official",
                onClick = {
                    val official = allUsers.firstOrNull {
                        (it.role == UserRole.ADMIN || it.role == UserRole.TANOD) &&
                                (it.officialId?.equals(officialId.trim(), ignoreCase = true) == true ||
                                        it.uid.equals(officialId.trim(), ignoreCase = true))
                    }
                    if (official != null) {
                        onLoginSuccess(official)
                    } else {
                        val fallback = allUsers.firstOrNull { it.role == UserRole.ADMIN }
                        if (fallback != null) {
                            onLoginSuccess(fallback)
                        } else {
                            error = "Official ID not recognized in database."
                        }
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = InterFontFamily, fontSize = 16.sp, color = PinProTextPrimary)
            }
        }
    )
}
