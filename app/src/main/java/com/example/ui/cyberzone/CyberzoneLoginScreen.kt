package com.example.ui.cyberzone

import android.app.Activity
import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Sleek PinPro Login Screen:
 * - Minimalist Instagram-inspired layout
 * - Dark grey (#121212) & electric green (#00FF66) aesthetic
 * - Official Google Sign-In with Jetpack Credential Manager
 * - Seamless transition to main dashboard upon authentication
 */
@Composable
fun CyberzoneLoginScreen(
    onLoginSuccess: (userDisplayName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val focusManager = LocalFocusManager.current

    var emailOrUsername by remember { mutableStateOf("resident@pinpro.ph") }
    var password by remember { mutableStateOf("pinpro2026") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var isStandardLoading by remember { mutableStateOf(false) }

    // Pulsing logo halo
    val infiniteTransition = rememberInfiniteTransition(label = "halo")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberzoneBackground)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header with Neon Touch
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    CyberzoneElectricGreen.copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CyberzoneSurface)
                            .border(1.5.dp, CyberzoneElectricGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = CyberzoneElectricGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PinPro",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = CyberzoneTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CyberzoneElectricGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ANIMAL HAZARD RADAR",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.5.sp,
                        color = CyberzoneElectricGreen
                    )
                }

                Text(
                    text = "Community dangerous animal alerts & neighborhood tracking",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = CyberzoneTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Authentication Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Google Sign-In Button (Primary Auth Provider)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = CyberzoneElectricGreen,
                            ambientColor = CyberzoneElectricGreen
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberzoneSurface)
                        .border(1.5.dp, CyberzoneElectricGreen, RoundedCornerShape(16.dp))
                        .clickable(enabled = !isGoogleLoading) {
                            errorMessage = null
                            isGoogleLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = { user ->
                                    isGoogleLoading = false
                                    onLoginSuccess(user.displayName ?: user.email ?: "Community Member")
                                },
                                onAuthError = { err ->
                                    isGoogleLoading = false
                                    errorMessage = err
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isGoogleLoading = false
                                }
                            )
                        }
                        .testTag("google_signin_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            color = CyberzoneElectricGreen,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Icon",
                                tint = CyberzoneElectricGreen,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp,
                                color = CyberzoneTextPrimary
                            )
                        }
                    }
                }

                // Divider Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(CyberzoneBorder)
                    )
                    Text(
                        text = "OR COMMUNITY LOGIN",
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = CyberzoneTextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(CyberzoneBorder)
                    )
                }

                // Email / Username Input
                CyberInputField(
                    value = emailOrUsername,
                    onValueChange = {
                        emailOrUsername = it
                        errorMessage = null
                    },
                    label = "EMAIL OR USERNAME",
                    placeholder = "resident@pinpro.ph",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = CyberzoneTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                // Password Input
                CyberInputField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = "PASSWORD",
                    placeholder = "••••••••",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CyberzoneTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = CyberzoneTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (emailOrUsername.isNotBlank()) {
                                onLoginSuccess(emailOrUsername.substringBefore("@"))
                            }
                        }
                    )
                )

                // Error Message
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = CyberzoneError,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Quick Demo Autofill Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Demo profiles",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = CyberzoneTextMuted
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DemoAutoFillChip(label = "Resident") {
                            emailOrUsername = "resident@pinpro.ph"
                            password = "pinpro2026"
                        }
                        DemoAutoFillChip(label = "Animal Control") {
                            emailOrUsername = "responder@pinpro.ph"
                            password = "pinpro2026"
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Community Login Action Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberzoneElectricGreen)
                        .clickable(
                            enabled = emailOrUsername.isNotBlank(),
                            onClick = {
                                focusManager.clearFocus()
                                onLoginSuccess(emailOrUsername.substringBefore("@"))
                            }
                        )
                        .testTag("community_login_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOG IN TO PINPRO",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF121212)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PinPro Alert Radar • Neighborhood Protection",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = CyberzoneTextMuted
                )
            }
        }
    }
}

/**
 * Initiates Google Sign-In via Jetpack Credential Manager using default_web_client_id.
 */
fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: (FirebaseUser) -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context as Activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                authResult.user?.let { onAuthSuccess(it) } ?: onAuthError("Firebase authentication failed")
            } else {
                onAuthError("Unexpected credential type returned")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign-in failed")
        }
    }
}

/**
 * Signs out of Firebase Auth and clears Credential Manager state.
 */
fun signOutUser(
    context: Context,
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onComplete: () -> Unit
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onComplete()
        }
    }
}

@Composable
fun CyberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.8.sp,
            color = if (isFocused) CyberzoneElectricGreen else CyberzoneTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CyberzoneSurface)
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) CyberzoneElectricGreen else CyberzoneBorder,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = CyberzoneTextMuted
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        visualTransformation = visualTransformation,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        cursorBrush = SolidColor(CyberzoneElectricGreen),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = CyberzoneTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused }
                    )
                }

                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        }
    }
}

@Composable
fun DemoAutoFillChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CyberzoneSurfaceElevated)
            .border(1.dp, CyberzoneBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = CyberzoneElectricGreen
        )
    }
}
