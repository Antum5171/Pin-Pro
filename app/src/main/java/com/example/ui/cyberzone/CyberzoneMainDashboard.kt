package com.example.ui.cyberzone

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.CyberzoneTab
import com.example.data.repository.PinProRepository
import com.example.ui.theme.CyberzoneBackground

/**
 * Main Cyberzone Dashboard container orchestrating:
 * - Animated Bottom Navigation Bar (Home, Map Pin, Search, Profile)
 * - Fluid spring scale-bounce animations and neon highlights
 * - Seamless view transitions between tabs
 */
@Composable
fun CyberzoneMainDashboard(
    username: String,
    repository: PinProRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(CyberzoneTab.MAP) } // Default to Map Pin dashboard as requested
    val pins by repository.cyberPins.collectAsState()
    val isPendingFilterActive by repository.isPendingOnlyFilterActive.collectAsState()

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
                            onTogglePinStatus = { pinId ->
                                repository.togglePinStatus(pinId)
                            }
                        )
                    }
                    CyberzoneTab.MAP -> {
                        CyberzoneMapView(
                            pins = pins,
                            isPendingFilterActive = isPendingFilterActive,
                            onTogglePendingFilter = { repository.togglePendingOnlyFilter() },
                            onDropPin = { label, xRatio, yRatio ->
                                repository.addCyberPin(label, xRatio, yRatio)
                            },
                            onDeletePin = { id ->
                                repository.removeCyberPin(id)
                            },
                            onTogglePinStatus = { pinId ->
                                repository.togglePinStatus(pinId)
                            }
                        )
                    }
                    CyberzoneTab.SEARCH -> {
                        CyberzoneSearchView(
                            pins = pins,
                            onSelectPin = { pin ->
                                activeTab = CyberzoneTab.MAP
                            }
                        )
                    }
                    CyberzoneTab.PROFILE -> {
                        CyberzoneProfileView(
                            username = username,
                            pinCount = pins.size,
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}
