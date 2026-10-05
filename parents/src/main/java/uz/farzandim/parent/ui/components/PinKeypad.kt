package uz.farzandim.parent.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import uz.farzandim.parent.ui.theme.*

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
                    isError -> GradeCoral
                    isFilled -> SkyBluePrimary
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
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
fun PinKeypad(
    onDigitClick: (Int) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rows 1-3: Digits 1 to 9
        for (row in 0 until 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 1..3) {
                    val digit = row * 3 + col
                    PinKey(
                        text = digit.toString(),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
            if (onBiometricClick != null) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
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
                        tint = SkyBluePrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(68.dp))
            }

            PinKey(
                text = "0",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDigitClick(0)
                }
            )

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackspaceClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "O'chirish",
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun PinKey(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = CardBackgroundSecondary,
        modifier = Modifier.size(68.dp),
        shadowElevation = 0.5.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
