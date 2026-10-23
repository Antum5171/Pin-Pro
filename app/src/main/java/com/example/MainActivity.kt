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
import com.example.ui.cyberzone.CyberzoneLoginScreen
import com.example.ui.cyberzone.CyberzoneMainDashboard
import com.example.ui.cyberzone.signOutUser
import com.example.ui.theme.CyberzoneBackground
import com.example.ui.theme.CyberzoneBorder
import com.example.ui.theme.CyberzoneElectricGreen
import com.example.ui.theme.CyberzoneSurface
import com.example.ui.theme.CyberzoneTextPrimary
import com.example.ui.theme.CyberzoneTextSecondary
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val firestoreDb = try {
            val dbId = getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (_: Exception) {
            null
        }

        val repository = PinProRepository(firestore = firestoreDb)

        setContent {
            MyApplicationTheme {
                PinProApp(repository = repository)
            }
        }
    }
}

@Composable
fun PinProApp(repository: PinProRepository) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUserDisplayName by remember {
        mutableStateOf<String?>(
            Firebase.auth.currentUser?.let { it.displayName ?: it.email ?: "Community Member" }
        )
    }

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
        topBar = {
            Column(modifier = Modifier.background(CyberzoneBackground)) {
                // Top PinPro Control Strip (Top-Right Barangay Switcher Removed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberzoneBackground)
                        .border(1.dp, CyberzoneBorder)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                            text = "PINPRO • ANIMAL HAZARD RADAR",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = CyberzoneElectricGreen
                        )
                    }

                    if (currentUserDisplayName != null) {
                        // Quick Sign Out Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberzoneSurface)
                                .border(1.dp, CyberzoneBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    signOutUser(context, credentialManager, coroutineScope) {
                                        currentUserDisplayName = null
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Sign Out",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = CyberzoneTextSecondary
                            )
                        }
                    }
                }
            }
        },
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
                    // Main Dangerous Animal Hazard Dashboard & Map View
                    CyberzoneMainDashboard(
                        username = currentUserDisplayName ?: "Resident",
                        repository = repository,
                        onLogout = {
                            signOutUser(context, credentialManager, coroutineScope) {
                                currentUserDisplayName = null
                            }
                        }
                    )
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
