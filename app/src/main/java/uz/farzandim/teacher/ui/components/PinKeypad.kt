package uz.farzandim.teacher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.teacher.ui.theme.*

@Composable
fun PinDots(
    pinLength: Int,
    maxDigits: Int = 4,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxDigits) {
            val isFilled = i < pinLength
            val dotColor by animateColorAsState(
                targetValue = when {
                    isError -> RoseBadge
                    isFilled -> HarvardCrimson
                    else -> BorderMedium
                },
                animationSpec = tween(durationMillis = 200),
                label = "dotColor"
            )
            val dotSize by animateDpAsState(
                targetValue = if (isFilled) 18.dp else 14.dp,
                animationSpec = tween(durationMillis = 200),
                label = "dotSize"
            )

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(if (isFilled || isError) dotColor else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = dotColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    showBiometric: Boolean = false,
    onBiometricClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    PinKeyButton(
                        text = digit,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDigitClick(digit)
                        }
                    )
                }
            }
        }

        // Bottom Row: Biometric / Empty, 0, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left action: Biometric or empty box
            if (showBiometric) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBiometricClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometriya",
                        tint = HarvardCrimson,
                        modifier = Modifier.size(34.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }

            // Digit 0
            PinKeyButton(
                text = "0",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDigitClick("0")
                }
            )

            // Right action: Backspace
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBackspaceClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "O'chirish",
                    tint = OxfordSlate,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OxfordSlate
            )
        }
    }
}
