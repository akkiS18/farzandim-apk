package uz.farzandim.parent.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.data.model.NextChargeDto
import uz.farzandim.parent.ui.theme.*

@Composable
fun PinnedActionCards(
    unapprovedGradesCount: Int,
    nextCharge: NextChargeDto?,
    onOpenDiary: () -> Unit,
    onOpenBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Unapproved Grades Alert Card
        if (unapprovedGradesCount > 0) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SunsetOrangeContainer,
                border = BorderStroke(1.dp, SunsetOrangeLight.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SunsetOrangeAccent,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = stringResource(id = R.string.unapproved_grades_alert, unapprovedGradesCount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SunsetOrangeDark
                            )
                            Text(
                                text = "Farzandingiz baholarini ko'zdan kechiring",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onOpenDiary,
                        colors = ButtonDefaults.buttonColors(containerColor = SunsetOrangeAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Ko'rish",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. Next Payment Due Card
        if (nextCharge?.amount != null && nextCharge.amount > 0) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GradeSkyBg,
                border = BorderStroke(1.dp, GradeSkyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SkyBluePrimary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            val formattedAmount = try {
                                java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
                                    maximumFractionDigits = 0
                                }.format(nextCharge.amount).replace(',', ' ')
                            } catch (e: Exception) {
                                "${nextCharge.amount.toLong()}"
                            }
                            Text(
                                text = "Oylik to'lov: $formattedAmount so'm",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBlueDark
                            )
                            Text(
                                text = "Muddati: ${nextCharge.chargeDate ?: "Yaqinda"}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onOpenBalance,
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Balans",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
