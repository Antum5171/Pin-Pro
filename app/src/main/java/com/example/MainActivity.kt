package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.repository.PinProRepository
import androidx.compose.runtime.collectAsState
import com.example.ui.cyberzone.GoogleMapsLocationPickerScreen
import com.example.ui.cyberzone.CyberzoneLoginScreen
import com.example.ui.cyberzone.CyberzoneMainDashboard
import com.example.ui.cyberzone.signOutUser
import com.example.ui.theme.CyberzoneBackground
import com.example.ui.theme.CyberzoneBorder
import com.example.ui.theme.CyberzoneElectricGreen
import com.example.ui.theme.CyberzoneElectricGreenSubtle
import com.example.ui.theme.CyberzoneSurface
import com.example.ui.theme.CyberzoneTextPrimary
import com.example.ui.theme.CyberzoneTextSecondary
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.example.util.configureAppCheck
import com.google.firebase.firestore.FirebaseFirestore

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Step 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        createNotificationChannel()

        val firestoreDb = try {
            val dbId = getString(R.string.firestore_database_id)
            if (dbId.isBlank() || dbId == "(default)") {
                FirebaseFirestore.getInstance()
            } else {
                FirebaseFirestore.getInstance(dbId)
            }
        } catch (_: Exception) {
            try {
                FirebaseFirestore.getInstance()
            } catch (_: Exception) {
                null
            }
        }

        val repository = PinProRepository(firestore = firestoreDb)

        setContent {
            MyApplicationTheme {
                PinProApp(repository = repository)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "PinPro Reports Channel"
            val descriptionText = "Notifications for barangay incident reports and official updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("pinpro_reports_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun PinProApp(repository: PinProRepository) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var currentUserDisplayName by remember {
        mutableStateOf<String?>(
            Firebase.auth.currentUser?.let { it.displayName ?: it.email ?: "Community Member" }
        )
    }

    val isLocationConfirmed by repository.isLocationConfirmed.collectAsState()
    val confirmedBarangay by repository.confirmedBarangay.collectAsState()
    val currentRole by repository.currentRole.collectAsState()

    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                currentUserDisplayName = user.displayName ?: user.email ?: "Community Member"
            }
        }
        Firebase.auth.addAuthStateListener(authListener)
        onDispose {
            Firebase.auth.removeAuthStateListener(authListener)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = CyberzoneBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentUserDisplayName != null,
                transitionSpec = {
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut()
                    )
                },
                label = "authTransition"
            ) { isLoggedIn ->
                if (isLoggedIn) {
                    if (!isLocationConfirmed) {
                        // Required Google Maps-style GPS location onboarding picker upon logging in
                        GoogleMapsLocationPickerScreen(
                            repository = repository,
                            onLocationConfirmed = { _, _ ->
                                // Location and Barangay assigned; transition to main dashboard
                            }
                        )
                    } else {
                        // Main Dangerous Animal Hazard Dashboard & Map View
                        CyberzoneMainDashboard(
                            username = currentUserDisplayName ?: "Resident",
                            repository = repository,
                            onOpenLocationPicker = {
                                repository.requestLocationReSelection()
                            },
                            onLogout = {
                                signOutUser(context, credentialManager, coroutineScope) {
                                    currentUserDisplayName = null
                                    repository.logout()
                                }
                            }
                        )
                    }
                } else {
                    // Sleek Login Screen with Google Sign-In & Community Auth
                    CyberzoneLoginScreen(
                        onLoginSuccess = { displayName ->
                            currentUserDisplayName = displayName
                        }
                    )
                }
            }
        }
    }
}
