package com.example.ui.resident

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Alert
import com.example.data.model.AlertCategory
import com.example.data.model.AlertStatus
import com.example.data.model.Barangay
import com.example.data.model.Tanod
import com.example.data.model.User
import com.example.data.model.UserStatus
import com.example.data.repository.PinProRepository
import com.example.ui.components.BlockedSuspendedDialog
import com.example.ui.components.CategoryChip
import com.example.ui.components.Emergency911Button
import com.example.ui.components.OutsideHomeBadge
import com.example.ui.components.PinProOutlinedButton
import com.example.ui.components.PinProPrimaryButton
import com.example.ui.components.PinProTextField
import com.example.ui.components.StarRatingBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusStepper
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

enum class ResidentTab {
    HOME,
    HISTORY,
    PROFILE
}

enum class ResidentFlowState {
    MAIN,
    REPORT_DETAILS,
    TRACKING,
    RESOLVED
}

@Composable
fun ResidentApp(
    repository: PinProRepository,
    onSwitchToOfficialLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val barangays by repository.barangays.collectAsState()
    val allAlerts by repository.alerts.collectAsState()
    val allTanods by repository.tanods.collectAsState()

    var showAuthFlow by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    if (currentUser == null) {
        if (showAuthFlow) {
            if (isRegistering) {
                ResidentRegisterFlow(
                    barangays = barangays,
                    onRegisterComplete = { name, mobile, address, barangayId ->
                        repository.registerResident(name, mobile, address, barangayId)
                        showAuthFlow = false
                    },
                    onBackToWelcome = { showAuthFlow = false }
                )
            } else {
                ResidentLoginForm(
                    onLoginSuccess = { user ->
                        repository.loginAs(user)
                        showAuthFlow = false
                    },
                    repository = repository,
                    onGoToRegister = { isRegistering = true },
                    onBackToWelcome = { showAuthFlow = false }
                )
            }
        } else {
            ResidentWelcomeScreen(
                onLoginClick = {
                    isRegistering = false
                    showAuthFlow = true
                },
                onCreateAccountClick = {
                    isRegistering = true
                    showAuthFlow = true
                },
                onOfficialLoginClick = onSwitchToOfficialLogin
            )
        }
        return
    }

    val user = currentUser!!

    // Active alert for this resident
    val activeAlert = allAlerts.firstOrNull {
        it.residentId == user.uid && it.status in listOf(
            AlertStatus.RECEIVED,
            AlertStatus.ASSIGNED,
            AlertStatus.ON_THE_WAY,
            AlertStatus.ARRIVED
        )
    }

    val assignedTanod = allTanods.firstOrNull { it.uid == activeAlert?.assignedTanodId }

    var selectedTab by remember { mutableStateOf(ResidentTab.HOME) }
    var flowState by remember { mutableStateOf(if (activeAlert != null) ResidentFlowState.TRACKING else ResidentFlowState.MAIN) }

    // If an alert was resolved while tracking
    var recentlyResolvedAlert by remember { mutableStateOf<Alert?>(null) }
    var showSuspendedDialog by remember { mutableStateOf(false) }

    // Selected category for new alert
    var selectedCategory by remember { mutableStateOf(AlertCategory.MEDICAL) }

    // Location check dialog state
    var showLocationPrompt by remember { mutableStateOf(false) }
    var detectedPhysicalBarangay by remember { mutableStateOf<Barangay?>(null) }
    var isOutsideCovered by remember { mutableStateOf(false) }

    val homeBarangay = barangays.firstOrNull { it.id == user.homeBarangayId }

    Scaffold(
        bottomBar = {
            if (flowState == ResidentFlowState.MAIN) {
                NavigationBar(
                    containerColor = PinProBackground,
                    contentColor = PinProTextPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(1.dp, PinProBorder)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == ResidentTab.HOME,
                        onClick = { selectedTab = ResidentTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = {
                            Text(
                                "Home",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == ResidentTab.HOME) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PinProTextPrimary,
                            selectedTextColor = PinProTextPrimary,
                            indicatorColor = PinProNeonGreen
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == ResidentTab.HISTORY,
                        onClick = { selectedTab = ResidentTab.HISTORY },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = {
                            Text(
                                "History",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == ResidentTab.HISTORY) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PinProTextPrimary,
                            selectedTextColor = PinProTextPrimary,
                            indicatorColor = PinProNeonGreen
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == ResidentTab.PROFILE,
                        onClick = { selectedTab = ResidentTab.PROFILE },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = {
                            Text(
                                "Profile",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == ResidentTab.PROFILE) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PinProTextPrimary,
                            selectedTextColor = PinProTextPrimary,
                            indicatorColor = PinProNeonGreen
                        )
                    )
                }
            }
        },
        containerColor = PinProBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // Tracking flow
                activeAlert != null -> {
                    ResidentTrackingScreen(
                        alert = activeAlert,
                        tanod = assignedTanod,
                        barangays = barangays,
                        onCancelAlert = {
                            repository.cancelAlert(activeAlert.id)
                            flowState = ResidentFlowState.MAIN
                        }
                    )
                }
                // Resolved screen
                recentlyResolvedAlert != null -> {
                    ResidentResolvedScreen(
                        alert = recentlyResolvedAlert!!,
                        onDone = {
                            recentlyResolvedAlert = null
                            flowState = ResidentFlowState.MAIN
                        },
                        onRate = { rating ->
                            repository.rateAlert(recentlyResolvedAlert!!.id, rating)
                        }
                    )
                }
                // Report details flow
                flowState == ResidentFlowState.REPORT_DETAILS -> {
                    ResidentReportDetailsScreen(
                        category = selectedCategory,
                        user = user,
                        barangays = barangays,
                        onBack = { flowState = ResidentFlowState.MAIN },
                        onSubmit = { note, shareLiveLocation ->
                            val alert = repository.createAlert(
                                resident = user,
                                category = selectedCategory,
                                note = note,
                                shareLiveLocation = shareLiveLocation
                            )
                            flowState = ResidentFlowState.TRACKING
                        }
                    )
                }
                // Standard Tab views
                else -> {
                    when (selectedTab) {
                        ResidentTab.HOME -> {
                            ResidentHomeScreen(
                                user = user,
                                homeBarangay = homeBarangay,
                                selectedCategory = selectedCategory,
                                onSelectCategory = { selectedCategory = it },
                                onSendAlert = {
                                    if (user.status == UserStatus.SUSPENDED) {
                                        showSuspendedDialog = true
                                        return@ResidentHomeScreen
                                    }

                                    // Check point-in-polygon
                                    val currentLoc = repository.getCurrentDeviceLocation()
                                    val physicalB = repository.findPhysicalBarangay(currentLoc)

                                    if (physicalB == null) {
                                        // Outside both
                                        detectedPhysicalBarangay = null
                                        isOutsideCovered = true
                                        showLocationPrompt = true
                                    } else if (physicalB.id != user.homeBarangayId) {
                                        // Inside another barangay
                                        detectedPhysicalBarangay = physicalB
                                        isOutsideCovered = false
                                        showLocationPrompt = true
                                    } else {
                                        // Normal flow in home barangay
                                        flowState = ResidentFlowState.REPORT_DETAILS
                                    }
                                }
                            )
                        }
                        ResidentTab.HISTORY -> {
                            ResidentHistoryScreen(
                                residentId = user.uid,
                                alerts = allAlerts.filter { it.residentId == user.uid },
                                barangays = barangays
                            )
                        }
                        ResidentTab.PROFILE -> {
                            ResidentProfileScreen(
                                user = user,
                                barangayName = homeBarangay?.name ?: "Unknown",
                                onUpdateProfile = { name, address ->
                                    repository.updateUserProfile(name, address)
                                },
                                onLogout = { repository.logout() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Location Check Prompt Dialog
    if (showLocationPrompt) {
        AlertDialog(
            onDismissRequest = { showLocationPrompt = false },
            title = {
                Text(
                    text = if (isOutsideCovered) "Outside Covered Area" else "Different Barangay Detected",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PinProTextPrimary
                )
            },
            text = {
                Column {
                    if (isOutsideCovered) {
                        Text(
                            text = "You appear to be outside any barangay we cover.",
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Emergency911Button()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "You can still route this alert to your registered home barangay (${homeBarangay?.name}).",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextMuted
                        )
                    } else {
                        Text(
                            text = "You are currently physically in ${detectedPhysicalBarangay?.name}. Your alert will be sent to ${detectedPhysicalBarangay?.name}, not your home barangay.",
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        )
                    }
                }
            },
            confirmButton = {
                PinProPrimaryButton(
                    text = if (isOutsideCovered) "Send anyway to home barangay" else "Continue",
                    onClick = {
                        showLocationPrompt = false
                        flowState = ResidentFlowState.REPORT_DETAILS
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showLocationPrompt = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = InterFontFamily,
                        fontSize = 16.sp,
                        color = PinProTextPrimary
                    )
                }
            }
        )
    }

    // Suspended Account Dialog
    if (showSuspendedDialog) {
        BlockedSuspendedDialog(
            barangayName = homeBarangay?.name ?: "Barangay Hall",
            barangayContact = homeBarangay?.hallContact ?: "(02) 8876-1234",
            onDismiss = { showSuspendedDialog = false }
        )
    }
}

@Composable
fun ResidentWelcomeScreen(
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onOfficialLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Emergency911Button(modifier = Modifier.align(Alignment.End))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 32.dp)
        ) {
            // Stylized PinPro Shield + Pin Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(PinProNeonGreen)
                    .border(2.dp, Color(0xFF0B0B0B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "PinPro Logo",
                    tint = Color(0xFF0B0B0B),
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PinPro",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PinProTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Barangay Emergency & Complaint Response",
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                color = PinProTextMuted
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PinProPrimaryButton(
                text = "Send Alert (Log In)",
                onClick = onLoginClick,
                modifier = Modifier.testTag("welcome_login_button")
            )

            Spacer(modifier = Modifier.height(12.dp))

            PinProOutlinedButton(
                text = "Create Resident Account",
                onClick = onCreateAccountClick,
                modifier = Modifier.testTag("welcome_register_button")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Barangay Official Login",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PinProTextPrimary,
                modifier = Modifier
                    .clickable(onClick = onOfficialLoginClick)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ResidentLoginForm(
    onLoginSuccess: (User) -> Unit,
    repository: PinProRepository,
    onGoToRegister: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    var mobile by remember { mutableStateOf("+63 917 123 4567") }
    var password by remember { mutableStateOf("demo123") }
    var error by remember { mutableStateOf<String?>(null) }
    var useOtpMode by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }

    val allUsers by repository.users.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                IconButton(onClick = onBackToWelcome) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resident Login",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PinProTextPrimary
                )
            }

            PinProTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = "Mobile Number (PH)",
                placeholder = "+63 9XX XXX XXXX",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!useOtpMode) {
                PinProTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Enter your password",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PinProSurface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Demo Step: Any 6 digits accepted. Hint: 123456",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                PinProTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it },
                    label = "Enter 6-digit Demo OTP",
                    placeholder = "123456",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error!!,
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = PinProError
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (useOtpMode) "Use Password instead" else "Login with Demo OTP",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = PinProTextPrimary,
                modifier = Modifier
                    .clickable { useOtpMode = !useOtpMode }
                    .padding(vertical = 4.dp)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            PinProPrimaryButton(
                text = "Log In",
                onClick = {
                    val normalized = mobile.replace(" ", "").replace("-", "")
                    val resident = allUsers.firstOrNull {
                        it.role == com.example.data.model.UserRole.RESIDENT &&
                                it.mobile.replace(" ", "").replace("-", "") == normalized
                    }
                    if (resident != null) {
                        onLoginSuccess(resident)
                    } else {
                        // For demo convenience, log in as Maria Santos if not exact
                        val fallback = allUsers.firstOrNull { it.role == com.example.data.model.UserRole.RESIDENT }
                        if (fallback != null) {
                            onLoginSuccess(fallback)
                        } else {
                            error = "Account not found for this mobile number."
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onGoToRegister,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Don't have an account? Register",
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    color = PinProTextPrimary
                )
            }
        }
    }
}

@Composable
fun ResidentRegisterFlow(
    barangays: List<Barangay>,
    onRegisterComplete: (String, String, String, String) -> Unit,
    onBackToWelcome: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // Form inputs
    var mobile by remember { mutableStateOf("+63 917 555 7788") }
    var otpCode by remember { mutableStateOf("123456") }
    var name by remember { mutableStateOf("Juan Dela Cruz") }
    var birthDate by remember { mutableStateOf("1995-08-15") }
    var password by remember { mutableStateOf("securePass123") }
    var address by remember { mutableStateOf("24 Mabini St., Purok 2") }
    var selectedBarangayId by remember { mutableStateOf(barangays.firstOrNull()?.id ?: "sample_hall") }
    var showBarangayDropdown by remember { mutableStateOf(false) }
    var agreedToPrivacy by remember { mutableStateOf(false) }
    var idUploaded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                IconButton(onClick = { if (step > 1) step-- else onBackToWelcome() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Register (Step $step of 4)",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PinProTextPrimary
                )
            }

            // Step Progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i <= step) PinProNeonGreen else PinProBorder)
                    )
                }
            }

            when (step) {
                1 -> {
                    Text(
                        text = "Enter Mobile Number",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PinProTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PinProTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = "Mobile Number (PH format)",
                        placeholder = "+63 9XX XXX XXXX"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PinProSurface)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "DEMO STEP: SMS OTP is simulated. Any 6 digits accepted. Demo code: 123456",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    PinProTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = "6-Digit Demo OTP",
                        placeholder = "123456"
                    )
                }
                2 -> {
                    Text(
                        text = "Personal Information",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PinProTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PinProTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PinProTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = "Birthdate (YYYY-MM-DD)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PinProTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password (min 6 chars)",
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                3 -> {
                    Text(
                        text = "Address & Barangay",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PinProTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PinProTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "House No., Street or Purok"
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val selectedB = barangays.firstOrNull { it.id == selectedBarangayId }
                    Text(
                        text = "Detected Barangay",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                        Text(
                            text = selectedB?.name ?: "Barangay Sample Hall",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        )
                        Box {
                            Text(
                                text = "Not correct? Change",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PinProTextPrimary,
                                modifier = Modifier
                                    .clickable { showBarangayDropdown = true }
                                    .padding(4.dp)
                            )
                            DropdownMenu(
                                expanded = showBarangayDropdown,
                                onDismissRequest = { showBarangayDropdown = false }
                            ) {
                                barangays.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b.name, fontFamily = InterFontFamily, fontSize = 16.sp) },
                                        onClick = {
                                            selectedBarangayId = b.id
                                            showBarangayDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                4 -> {
                    Text(
                        text = "ID Verification (Demo step)",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PinProTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload any valid ID for residency confirmation. (Simulated demo step)",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (idUploaded) Color(0xFFDCFCE7) else PinProSurface)
                            .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
                            .clickable { idUploaded = !idUploaded },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (idUploaded) Icons.Default.CheckCircle else Icons.Default.Shield,
                                contentDescription = "ID Preview",
                                tint = if (idUploaded) Color(0xFF166534) else PinProTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (idUploaded) "Mock Government ID Attached" else "Tap to attach Mock ID / Camera preview",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (idUploaded) Color(0xFF166534) else PinProTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // RA 10173 Data Privacy consent
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreedToPrivacy = !agreedToPrivacy }
                    ) {
                        Checkbox(
                            checked = agreedToPrivacy,
                            onCheckedChange = { agreedToPrivacy = it },
                            colors = CheckboxDefaults.colors(checkedColor = PinProNeonGreen, checkmarkColor = PinProTextPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I agree to the Data Privacy Act (RA 10173) and consent to share my physical GPS location with the barangay during emergency alerts.",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextPrimary
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            PinProPrimaryButton(
                text = if (step < 4) "Next" else "Agree & Register",
                enabled = step < 4 || agreedToPrivacy,
                onClick = {
                    if (step < 4) {
                        step++
                    } else {
                        onRegisterComplete(name, mobile, address, selectedBarangayId)
                    }
                }
            )

            if (step == 4) {
                Spacer(modifier = Modifier.height(8.dp))
                PinProOutlinedButton(
                    text = "Skip ID for now",
                    onClick = {
                        idUploaded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResidentHomeScreen(
    user: User,
    homeBarangay: Barangay?,
    selectedCategory: AlertCategory,
    onSelectCategory: (AlertCategory) -> Unit,
    onSendAlert: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Location line with green dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PinProSurface)
                    .border(1.dp, PinProBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PinProNeonGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Location: ${user.address}, ${homeBarangay?.name ?: "Barangay"}",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = PinProTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Need help?",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PinProTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap to send an instant emergency alert to the nearest barangay responder.",
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                color = PinProTextMuted,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Big circular neon green "SEND ALERT" button with subtle glow
            Box(
                modifier = Modifier
                    .size(200.dp + haloPulse.dp)
                    .shadow(elevation = 16.dp, shape = CircleShape, spotColor = PinProNeonGreen.copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(PinProNeonGreen)
                    .border(3.dp, Color(0xFF0B0B0B), CircleShape)
                    .clickable(onClick = onSendAlert)
                    .testTag("send_alert_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert Icon",
                        tint = Color(0xFF0B0B0B),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SEND ALERT",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0B0B0B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Select Incident Category",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PinProTextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        category = AlertCategory.MEDICAL,
                        isSelected = selectedCategory == AlertCategory.MEDICAL,
                        onSelect = { onSelectCategory(AlertCategory.MEDICAL) },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryChip(
                        category = AlertCategory.FIRE,
                        isSelected = selectedCategory == AlertCategory.FIRE,
                        onSelect = { onSelectCategory(AlertCategory.FIRE) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        category = AlertCategory.CRIME,
                        isSelected = selectedCategory == AlertCategory.CRIME,
                        onSelect = { onSelectCategory(AlertCategory.CRIME) },
                        modifier = Modifier.weight(1f)
                    )
                    CategoryChip(
                        category = AlertCategory.FLOOD,
                        isSelected = selectedCategory == AlertCategory.FLOOD,
                        onSelect = { onSelectCategory(AlertCategory.FLOOD) },
                        modifier = Modifier.weight(1f)
                    )
                }
                CategoryChip(
                    category = AlertCategory.OTHER,
                    isSelected = selectedCategory == AlertCategory.OTHER,
                    onSelect = { onSelectCategory(AlertCategory.OTHER) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Emergency911Button()
    }
}

@Composable
fun ResidentReportDetailsScreen(
    category: AlertCategory,
    user: User,
    barangays: List<Barangay>,
    onBack: () -> Unit,
    onSubmit: (note: String, shareLiveLocation: Boolean) -> Unit
) {
    var note by remember { mutableStateOf("") }
    var shareLiveLocation by remember { mutableStateOf(true) }
    var photoAttached by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Details",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PinProTextPrimary
                )
            }

            // Category summary badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PinProNeonGreen)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF0B0B0B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Category: ${category.displayName}",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF0B0B0B)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Note input
            PinProTextField(
                value = note,
                onValueChange = { note = it },
                label = "Additional Notes / Landmarks (Optional)",
                placeholder = "e.g. Near blue gate, second floor apartment"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo upload box
            Text(
                text = "Incident Photo (Optional)",
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = PinProTextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (photoAttached) Color(0xFFDCFCE7) else PinProSurface)
                    .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
                    .clickable { photoAttached = !photoAttached },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (photoAttached) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                        contentDescription = "Attach photo",
                        tint = if (photoAttached) Color(0xFF166534) else PinProTextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (photoAttached) "Photo Attached (Simulated)" else "Tap to take or attach photo",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = if (photoAttached) Color(0xFF166534) else PinProTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live location sharing switch
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
                        text = "Share Live Location",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PinProTextPrimary
                    )
                    Text(
                        text = "Allows responding tanod to see your real-time position",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProTextMuted
                    )
                }
                Switch(
                    checked = shareLiveLocation,
                    onCheckedChange = { shareLiveLocation = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0B0B0B),
                        checkedTrackColor = PinProNeonGreen
                    )
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            PinProPrimaryButton(
                text = "Send to Barangay",
                onClick = { onSubmit(note, shareLiveLocation) }
            )
        }
    }
}

@Composable
fun ResidentTrackingScreen(
    alert: Alert,
    tanod: Tanod?,
    barangays: List<Barangay>,
    onCancelAlert: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
    ) {
        // Map occupies top portion (Grab / Lalamove style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
        ) {
            PinProMapView(
                barangays = barangays,
                activeBarangayId = alert.receivingBarangayId,
                userLocation = com.example.data.model.LatLng(alert.location.lat, alert.location.lng),
                assignedTanod = tanod,
                activeAlert = alert,
                modifier = Modifier.fillMaxSize()
            )

            // ETA Chip floating on top of map
            if (alert.status == AlertStatus.ON_THE_WAY && alert.etaMinutes != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0B0B0B))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "ETA: ${alert.etaMinutes} mins",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PinProNeonGreen
                    )
                }
            }
        }

        // Bottom Sheet: Status, Stepper, Tanod Card, Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(PinProBackground)
                .border(1.dp, PinProBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header with Ref and Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = alert.id,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = PinProTextPrimary
                        )
                        Text(
                            text = "Category: ${alert.category.displayName}",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextMuted
                        )
                    }
                    StatusBadge(status = alert.status)
                }

                if (alert.fromOutsideHome) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutsideHomeBadge()
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Stepper
                StatusStepper(currentStatus = alert.status)

                Spacer(modifier = Modifier.height(12.dp))

                // Waiting state before assignment
                if (alert.status == AlertStatus.RECEIVED) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PinProSurface)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF0B0B0B),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Waiting for the barangay to dispatch...",
                                fontFamily = InterFontFamily,
                                fontSize = 16.sp,
                                color = PinProTextPrimary
                            )
                        }
                    }
                } else if (tanod != null) {
                    // Tanod Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PinProNeonGreen)
                                    .border(1.dp, Color(0xFF0B0B0B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Badge",
                                    tint = Color(0xFF0B0B0B),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tanod.name,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = PinProTextPrimary
                                )
                                Text(
                                    text = "Barangay Tanod • Badge ${tanod.badgeNumber}",
                                    fontFamily = InterFontFamily,
                                    fontSize = 12.sp,
                                    color = PinProTextMuted
                                )
                            }
                        }

                        // Call Tanod Button
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${tanod.contact}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, PinProBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Tanod",
                                tint = PinProTextPrimary
                            )
                        }
                    }
                }
            }

            // Cancel Alert button
            if (alert.status in listOf(AlertStatus.RECEIVED, AlertStatus.ASSIGNED)) {
                TextButton(
                    onClick = onCancelAlert,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Cancel Alert",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = PinProError
                    )
                }
            }
        }
    }
}

@Composable
fun ResidentResolvedScreen(
    alert: Alert,
    onDone: () -> Unit,
    onRate: (Int) -> Unit
) {
    var rating by remember { mutableIntStateOf(alert.rating ?: 5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PinProNeonGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Resolved",
                    tint = Color(0xFF0B0B0B),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Help has arrived",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PinProTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your emergency report #${alert.id} has been marked as resolved by the responding team.",
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                color = PinProTextMuted
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Rate Tanod Response",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = PinProTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            StarRatingBar(
                rating = rating,
                onRatingChanged = {
                    rating = it
                    onRate(it)
                }
            )
        }

        PinProPrimaryButton(
            text = "Done",
            onClick = onDone
        )
    }
}

@Composable
fun ResidentHistoryScreen(
    residentId: String,
    alerts: List<Alert>,
    barangays: List<Barangay>
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
            .padding(24.dp)
    ) {
        Text(
            text = "Alert History",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = PinProTextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No past alerts recorded.",
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    color = PinProTextMuted
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(alerts) { alert ->
                    val bName = barangays.firstOrNull { it.id == alert.receivingBarangayId }?.name ?: "Barangay"
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
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = PinProTextPrimary
                            )
                            StatusBadge(status = alert.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = dateFormat.format(Date(alert.createdAt)),
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Received by: $bName",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = PinProTextPrimary
                        )

                        if (alert.rating != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for (i in 1..alert.rating) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFEAB308),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResidentProfileScreen(
    user: User,
    barangayName: String,
    onUpdateProfile: (String, String) -> Unit,
    onLogout: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user.name) }
    var editAddress by remember { mutableStateOf(user.address) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PinProBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Resident Profile",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PinProTextPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (!isEditing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PinProSurface)
                        .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("Full Name", fontFamily = InterFontFamily, fontSize = 12.sp, color = PinProTextMuted)
                        Text(user.name, fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PinProTextPrimary)
                    }
                    Column {
                        Text("Mobile Number", fontFamily = InterFontFamily, fontSize = 12.sp, color = PinProTextMuted)
                        Text(user.mobile, fontFamily = InterFontFamily, fontSize = 16.sp, color = PinProTextPrimary)
                    }
                    Column {
                        Text("Address", fontFamily = InterFontFamily, fontSize = 12.sp, color = PinProTextMuted)
                        Text(user.address, fontFamily = InterFontFamily, fontSize = 16.sp, color = PinProTextPrimary)
                    }
                    Column {
                        Text("Home Barangay", fontFamily = InterFontFamily, fontSize = 12.sp, color = PinProTextMuted)
                        Text(barangayName, fontFamily = InterFontFamily, fontSize = 16.sp, color = PinProTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                PinProOutlinedButton(
                    text = "Edit Profile Info",
                    onClick = {
                        editName = user.name
                        editAddress = user.address
                        isEditing = true
                    }
                )
            } else {
                PinProTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = "Full Name"
                )
                Spacer(modifier = Modifier.height(12.dp))
                PinProTextField(
                    value = editAddress,
                    onValueChange = { editAddress = it },
                    label = "Address"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PinProOutlinedButton(
                        text = "Cancel",
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f)
                    )
                    PinProPrimaryButton(
                        text = "Save",
                        onClick = {
                            onUpdateProfile(editName, editAddress)
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            PinProOutlinedButton(
                text = "Sign Out",
                onClick = onLogout
            )
        }
    }
}
