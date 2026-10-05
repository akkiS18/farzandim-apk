package uz.farzandim.teacher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.teacher.ui.theme.*

@Composable
fun AdminCodeDialog(
    currentSubdomain: String,
    onDismiss: () -> Unit,
    onSaveSubdomain: (String) -> Unit
) {
    var subdomainInput by remember { mutableStateOf(currentSubdomain) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = HarvardCrimson
                )
                Text(
                    text = "Maktabni Tanlash",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OxfordSlate
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Maktabingizning tizimdagi subdomen nomini kiriting:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = subdomainInput,
                        onValueChange = { subdomainInput = it.lowercase().replace(" ", "") },
                        label = { Text("Subdomen") },
                        placeholder = { Text("test_school") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(
                        text = ".akademx.uz",
                        fontWeight = FontWeight.Bold,
                        color = OxfordSlate,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    color = GrayBadgeBg,
                    shape = RoundedCornerShape(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "API Server manzili:",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = uz.farzandim.teacher.BuildConfig.BASE_URL,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OxfordSlate,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Maktab Origin domeni:",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "https://${subdomainInput.ifEmpty { "..." }}.akademx.uz",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OxfordSlate,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = subdomainInput.trim().lowercase()
                    if (clean.isNotEmpty()) {
                        onSaveSubdomain(clean)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HarvardCrimson),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(
                    text = "Saqlash",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Bekor qilish", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(0.dp),
        containerColor = CardBackground
    )
}
