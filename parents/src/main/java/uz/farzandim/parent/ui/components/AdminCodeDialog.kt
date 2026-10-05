package uz.farzandim.parent.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.ui.theme.*

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
                    tint = SkyBluePrimary
                )
                Text(
                    text = stringResource(id = R.string.school_settings),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.school_subdomain),
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = subdomainInput,
                    onValueChange = { subdomainInput = it.lowercase().trim() },
                    placeholder = { Text(stringResource(id = R.string.school_subdomain_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "https://${subdomainInput.ifEmpty { "maktab" }}.akademx.uz",
                    fontSize = 11.sp,
                    color = SkyBlueDark,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subdomainInput.isNotBlank()) {
                        onSaveSubdomain(subdomainInput)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(id = R.string.save_btn), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel_btn), color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = CardBackground
    )
}
