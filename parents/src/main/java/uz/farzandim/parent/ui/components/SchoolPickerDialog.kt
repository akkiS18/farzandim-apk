package uz.farzandim.parent.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import uz.farzandim.parent.data.model.SchoolDto
import uz.farzandim.parent.ui.theme.*

@Composable
fun SchoolPickerDialog(
    schools: List<SchoolDto>,
    isLoading: Boolean,
    currentSubdomain: String,
    onDismiss: () -> Unit,
    onSelectSchool: (SchoolDto) -> Unit,
    onRefresh: () -> Unit,
    onManualSubdomain: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }
    var manualSubdomainInput by remember { mutableStateOf(currentSubdomain) }

    val filteredSchools = remember(schools, searchQuery) {
        if (searchQuery.isBlank()) schools
        else schools.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            (it.subdomain?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SkyBlueContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Maktabni tanlang",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Farzandingiz o'qiydigan maktabni belgilang",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Yopish",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Maktab nomini qidiring...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Qidiruv",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Tozalash",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBluePrimary,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = CardBackgroundSecondary,
                        unfocusedContainerColor = CardBackgroundSecondary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List of schools
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        isLoading && schools.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = SkyBluePrimary,
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Text(
                                        text = "Maktablar yuklanmoqda...",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        filteredSchools.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = if (searchQuery.isBlank()) "Maktablar topilmadi" else "'$searchQuery' bo'yicha maktab topilmadi",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    TextButton(onClick = onRefresh) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Qayta yuklash", color = SkyBluePrimary)
                                    }
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredSchools, key = { it.id }) { school ->
                                    val isSelected = school.subdomain.equals(currentSubdomain, ignoreCase = true) ||
                                            school.id.equals(currentSubdomain, ignoreCase = true)

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) SkyBlueContainer.copy(alpha = 0.45f) else CardBackgroundSecondary,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) SkyBluePrimary else BorderLight
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onSelectSchool(school)
                                                onDismiss()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isSelected) SkyBluePrimary else Color(0xFFE2E8F0),
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.AccountBalance,
                                                            contentDescription = null,
                                                            tint = if (isSelected) Color.White else TextSecondary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                Column {
                                                    Text(
                                                        text = school.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                        color = if (isSelected) SkyBlueDark else TextPrimary
                                                    )
                                                    if (!school.subdomain.isNullOrBlank()) {
                                                        Text(
                                                            text = "${school.subdomain}.akademx.uz",
                                                            fontSize = 11.sp,
                                                            color = TextMuted
                                                        )
                                                    }
                                                }
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Tanlangan",
                                                    tint = SkyBluePrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom: Refresh and Manual Subdomain
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(8.dp))

                if (!showManualInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yangilash", fontSize = 12.sp, color = TextSecondary)
                        }

                        TextButton(onClick = { showManualInput = true }) {
                            Text("Qo'lda kiritish", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualSubdomainInput,
                            onValueChange = { manualSubdomainInput = it.lowercase().replace(" ", "") },
                            label = { Text("Subdomen", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                val clean = manualSubdomainInput.trim().lowercase()
                                if (clean.isNotEmpty()) {
                                    onManualSubdomain(clean)
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Saqlash", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
