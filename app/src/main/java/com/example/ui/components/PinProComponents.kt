package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertCategory
import com.example.data.model.AlertStatus
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.PinProBorder
import com.example.ui.theme.PinProError
import com.example.ui.theme.PinProNeonGreen
import com.example.ui.theme.PinProSurface
import com.example.ui.theme.PinProTextMuted
import com.example.ui.theme.PinProTextPrimary

@Composable
fun PinProPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val bgColor = if (enabled) PinProNeonGreen else Color(0xFFD1D5DB)
    val textColor = if (enabled) PinProTextPrimary else Color(0xFF6B7280)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun PinProOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, PinProBorder, RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = if (enabled) PinProTextPrimary else PinProTextMuted
        )
    }
}

@Composable
fun Emergency911Button(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
            .clickable {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:911")
                }
                context.startActivity(intent)
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Emergency 911",
            tint = PinProError,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Emergency? Tap to call 911",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = PinProError
        )
    }
}

@Composable
fun PinProTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            color = PinProTextMuted,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (readOnly) PinProSurface else Color.White)
                .border(
                    width = 1.dp,
                    color = if (errorText != null) PinProError else PinProBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = PinProTextMuted
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        readOnly = readOnly,
                        keyboardOptions = keyboardOptions,
                        visualTransformation = visualTransformation,
                        cursorBrush = SolidColor(PinProTextPrimary),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = InterFontFamily,
                            fontSize = 16.sp,
                            color = PinProTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (trailingContent != null) {
                    trailingContent()
                }
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = PinProError,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: AlertStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        AlertStatus.RECEIVED -> Color(0xFFFEF08A) to Color(0xFF854D0E) // Yellow
        AlertStatus.ASSIGNED -> Color(0xFFBFDBFE) to Color(0xFF1E40AF) // Blue
        AlertStatus.ON_THE_WAY -> PinProNeonGreen to PinProTextPrimary // Neon green fill
        AlertStatus.ARRIVED -> PinProNeonGreen to PinProTextPrimary
        AlertStatus.RESOLVED -> Color(0xFFDCFCE7) to Color(0xFF166534) // Soft green
        AlertStatus.CANCELLED -> Color(0xFFF3F4F6) to Color(0xFF6B7280) // Gray
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = textColor
        )
    }
}

@Composable
fun CategoryChip(
    category: AlertCategory,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) PinProNeonGreen else Color.White
    val borderCol = if (isSelected) PinProNeonGreen else PinProBorder
    val textColor = if (isSelected) PinProTextPrimary else PinProTextPrimary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.displayName,
            fontFamily = InterFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 16.sp,
            color = textColor
        )
    }
}

@Composable
fun OutsideHomeBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEF3C7))
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "Sent from outside home barangay",
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF92400E)
        )
    }
}

@Composable
fun StatusStepper(
    currentStatus: AlertStatus,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        AlertStatus.RECEIVED to "Received",
        AlertStatus.ASSIGNED to "Assigned",
        AlertStatus.ON_THE_WAY to "On the way",
        AlertStatus.ARRIVED to "Arrived"
    )

    val currentIndex = when (currentStatus) {
        AlertStatus.RECEIVED -> 0
        AlertStatus.ASSIGNED -> 1
        AlertStatus.ON_THE_WAY -> 2
        AlertStatus.ARRIVED, AlertStatus.RESOLVED -> 3
        AlertStatus.CANCELLED -> -1
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, pair ->
            val isPassed = currentIndex >= index
            val isCurrent = currentIndex == index

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPassed -> PinProNeonGreen
                                else -> PinProSurface
                            }
                        )
                        .border(
                            1.dp,
                            if (isPassed) Color(0xFF0B0B0B) else PinProBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPassed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF0B0B0B),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PinProTextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pair.second,
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPassed) PinProTextPrimary else PinProTextMuted
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(0.6f)
                        .background(
                            if (currentIndex > index) PinProNeonGreen else PinProBorder
                        )
                )
            }
        }
    }
}

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = "Star $i",
                tint = if (i <= rating) Color(0xFFEAB308) else Color(0xFFD1D5DB),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChanged(i) }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun BlockedSuspendedDialog(
    barangayName: String,
    barangayContact: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Account Suspended",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PinProError
            )
        },
        text = {
            Column {
                Text(
                    text = "Your resident alert access is temporarily suspended by $barangayName.",
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    color = PinProTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "If you have a life-threatening emergency, call 911 immediately.",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    color = PinProTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Emergency911Button()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Barangay Hall Contact: $barangayContact",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = PinProTextPrimary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Close",
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    color = PinProTextPrimary
                )
            }
        }
    )
}
