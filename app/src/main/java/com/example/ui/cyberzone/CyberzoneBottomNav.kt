package com.example.ui.cyberzone

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CyberzoneTab
import com.example.ui.theme.CyberzoneBackground
import com.example.ui.theme.CyberzoneBorder
import com.example.ui.theme.CyberzoneElectricGreen
import com.example.ui.theme.CyberzoneElectricGreenGlow
import com.example.ui.theme.CyberzoneSurface
import com.example.ui.theme.CyberzoneTextMuted
import com.example.ui.theme.CyberzoneTextPrimary
import com.example.ui.theme.InterFontFamily

/**
 * Animated Bottom Navigation Bar inspired by Instagram with Cyberzone styling:
 * - Four distinct tab icons: Home, Map Pin, Search, and Profile.
 * - Fluid spring scale-bounce animations and neon glow highlights.
 * - Edge-to-edge navigation bar padding safe area compliance.
 */
@Composable
fun CyberzoneBottomNav(
    activeTab: CyberzoneTab,
    onTabSelected: (CyberzoneTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberzoneBackground)
            .border(1.dp, CyberzoneBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CyberNavItem(
                tab = CyberzoneTab.HOME,
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = activeTab == CyberzoneTab.HOME,
                onClick = { onTabSelected(CyberzoneTab.HOME) }
            )

            CyberNavItem(
                tab = CyberzoneTab.MAP,
                label = "Map Pin",
                selectedIcon = Icons.Filled.LocationOn,
                unselectedIcon = Icons.Outlined.LocationOn,
                isSelected = activeTab == CyberzoneTab.MAP,
                onClick = { onTabSelected(CyberzoneTab.MAP) }
            )

            CyberNavItem(
                tab = CyberzoneTab.SEARCH,
                label = "Search",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                isSelected = activeTab == CyberzoneTab.SEARCH,
                onClick = { onTabSelected(CyberzoneTab.SEARCH) }
            )

            CyberNavItem(
                tab = CyberzoneTab.PROFILE,
                label = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                isSelected = activeTab == CyberzoneTab.PROFILE,
                onClick = { onTabSelected(CyberzoneTab.PROFILE) }
            )
        }
    }
}

@Composable
fun CyberNavItem(
    tab: CyberzoneTab,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Fluid spring scale-bounce animation
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("nav_tab_${tab.name.lowercase()}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp)
        ) {
            // Neon Glow Highlight behind active icon
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyberzoneElectricGreen.copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = if (isSelected) CyberzoneElectricGreen else CyberzoneTextMuted,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scaleAnim)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Glowing Neon Indicator Dot / Label
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(CyberzoneElectricGreen)
            )
        } else {
            Box(modifier = Modifier.size(4.dp))
        }
    }
}
