package uz.farzandim.teacher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import uz.farzandim.teacher.data.model.GradeConflictItem
import uz.farzandim.teacher.ui.theme.*

@Composable
fun GradeConflictDialog(
    conflict: GradeConflictItem,
    isResolving: Boolean = false,
    onKeepLocal: () -> Unit,
    onAcceptRemote: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Modal: require user selection to resolve conflict safely */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Warning Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7), // Light amber
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Ogohlantirish",
                            tint = Color(0xFFD97706), // Amber 600
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Baho Ziddiyati Aniqlandi!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OxfordSlate,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Oflayn kiritilgan baho va sinf rahbar webdan kiritgan baho o'rtasida farq mavjud.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Student & Context Info Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "O'quvchi:",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = conflict.studentName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OxfordSlate
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fan va Dars:",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "${conflict.subjectName} (${conflict.lessonNumber}-soat)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OxfordSlate
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sana:",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = conflict.gradeDate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OxfordSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comparison Cards (Local vs Remote)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Local Grade Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF), // Light blue
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Siz qo'ygan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1D4ED8)
                            )
                            Text(
                                text = "(Oflayn)",
                                fontSize = 10.sp,
                                color = Color(0xFF3B82F6)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = conflict.localValue,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }

                    // Remote Grade Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2), // Light red
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Serverdagi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFB91C1C)
                            )
                            Text(
                                text = "(Sinf rahbar)",
                                fontSize = 10.sp,
                                color = Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = conflict.remoteValue,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Qaysi bahoni qabul qilaylik?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OxfordSlate
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                if (isResolving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = HarvardCrimson,
                        strokeWidth = 3.dp
                    )
                } else {
                    Button(
                        onClick = onKeepLocal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB) // Blue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "O'zimning bahoyimni qoldirish",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onAcceptRemote,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Serverdagini (Sinf rahbar) qabul qilish",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }
    }
}
