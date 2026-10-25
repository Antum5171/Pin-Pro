package com.example.ui.cyberzone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimalType
import com.example.data.model.AppRole
import com.example.data.model.ReportCategory
import com.example.data.model.ReportStatus
import com.example.ui.theme.CyberzoneBorder
import com.example.ui.theme.CyberzoneElectricGreen
import com.example.ui.theme.CyberzoneSurfaceElevated
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.StatusDone
import com.example.ui.theme.StatusOnTheWay
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusUrgent

/**
 * Google Material Symbols UI Pack mapping and clean reusable components
 * Refactored for Barangay Report Tracking with exact status token colors.
 */
object GoogleIconPack {

    fun getCategoryIcon(category: ReportCategory): ImageVector = when (category) {
        ReportCategory.FIGHTS -> Icons.Filled.SportsMma
        ReportCategory.DANGEROUS_ANIMALS -> Icons.Filled.Pets
        ReportCategory.NOISE_COMPLAINT -> Icons.Filled.Campaign
        ReportCategory.FOUL_SMELL -> Icons.Filled.Air
        ReportCategory.TRASH -> Icons.Filled.Delete
    }

    fun getCategoryColor(category: ReportCategory): Color = when (category) {
        ReportCategory.FIGHTS -> StatusUrgent // Red #EF4444
        ReportCategory.DANGEROUS_ANIMALS -> StatusPending // Amber #F59E0B
        ReportCategory.NOISE_COMPLAINT -> StatusOnTheWay // Blue #3B82F6
        ReportCategory.FOUL_SMELL -> Color(0xFF8B5CF6) // Purple
        ReportCategory.TRASH -> StatusDone // Emerald Green #10B981
    }

    fun getStatusIcon(status: ReportStatus): ImageVector = when (status) {
        ReportStatus.PENDING -> Icons.Filled.Schedule
        ReportStatus.ACCEPTED -> Icons.Filled.Check
        ReportStatus.ON_THE_WAY -> Icons.Filled.NearMe
        ReportStatus.ARRIVED -> Icons.Filled.Place
        ReportStatus.RESOLVED -> Icons.Filled.CheckCircle
    }

    fun getStatusColor(status: ReportStatus): Color = when (status) {
        ReportStatus.PENDING -> StatusPending      // Amber #F59E0B
        ReportStatus.ACCEPTED -> StatusOnTheWay     // Blue #3B82F6
        ReportStatus.ON_THE_WAY -> StatusOnTheWay  // Blue #3B82F6
        ReportStatus.ARRIVED -> StatusOnTheWay     // Blue #3B82F6
        ReportStatus.RESOLVED -> StatusDone        // Muted Green #10B981
    }

    fun getAnimalTypeIcon(animalType: AnimalType): ImageVector = when (animalType) {
        AnimalType.SNAKE -> Icons.Filled.Warning
        AnimalType.AGGRESSIVE_DOG -> Icons.Filled.Pets
        AnimalType.RABID_ANIMAL -> Icons.Filled.Warning
        AnimalType.VENOMOUS_CREATURE -> Icons.Filled.Warning
        AnimalType.STRAY_PACK -> Icons.Filled.Pets
        AnimalType.OTHER_DANGEROUS -> Icons.Filled.Warning
    }

    fun getRoleIcon(role: AppRole): ImageVector = when (role) {
        AppRole.OFFICIAL -> Icons.Filled.Shield
        AppRole.RESIDENT -> Icons.Filled.Person
    }

    val PATROL_CAR = Icons.Filled.DirectionsCar
    val LOCATION_PIN = Icons.Filled.Place
    val GPS_TARGET = Icons.Filled.GpsFixed
    val CIVIC_BUILDING = Icons.Filled.AccountBalance
    val CHURCH = Icons.Filled.Church
    val CITY_HALL = Icons.Filled.LocationCity
    val MARKET = Icons.Filled.Storefront
    val UNIVERSITY = Icons.Filled.School
    val HOSPITAL = Icons.Filled.LocalHospital
    val WATER_WETLAND = Icons.Filled.WaterDrop
    val PARK = Icons.Filled.Park
}

@Composable
fun GoogleCategoryBadge(
    category: ReportCategory,
    size: Dp = 32.dp,
    iconSize: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    val icon = GoogleIconPack.getCategoryIcon(category)
    val color = GoogleIconPack.getCategoryColor(category)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category.displayName,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Status pill (PENDING / ON THE WAY / DONE):
 * Auto-width, single-line, uppercase, small, with a colored dot. Never wraps!
 */
@Composable
fun GoogleStatusBadge(
    status: ReportStatus,
    modifier: Modifier = Modifier
) {
    val color = GoogleIconPack.getStatusColor(status)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status.label.uppercase(),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = color,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun GoogleRoleBadge(
    role: AppRole,
    modifier: Modifier = Modifier
) {
    val icon = GoogleIconPack.getRoleIcon(role)
    val label = if (role == AppRole.OFFICIAL) "OFFICIAL" else "RESIDENT"

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = CyberzoneElectricGreen,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = CyberzoneElectricGreen,
            maxLines = 1,
            softWrap = false
        )
    }
}
