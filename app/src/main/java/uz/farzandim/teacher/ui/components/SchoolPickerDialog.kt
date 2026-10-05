package uz.farzandim.teacher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import uz.farzandim.teacher.data.model.SchoolDto
import uz.farzandim.teacher.ui.theme.*

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
            shape = RoundedCornerShape(16.dp),
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
                            color = HarvardCrimson.copy(alpha = 0.1f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = HarvardCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Maktabni tanlang",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = OxfordSlate
                            )
                            Text(
                                text = "Tizimga kirish uchun o'z maktabingizni belgilang",
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
                    placeholder = { Text("Maktab nomini qidiring...", fontSize = 13.sp) },
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
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarvardCrimson,
                        unfocusedBorderColor = BorderLight,
                        focusedContainerColor = GrayBadgeBg.copy(alpha = 0.3f),
                        unfocusedContainerColor = GrayBadgeBg.copy(alpha = 0.3f)
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
                                        color = HarvardCrimson,
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
                                        color = OxfordSlate
                                    )
                                    TextButton(onClick = onRefresh) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Qayta yuklash", color = HarvardCrimson)
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
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) HarvardCrimson.copy(alpha = 0.06f) else Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) HarvardCrimson else BorderLight
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
                                                    color = if (isSelected) HarvardCrimson else Color(0xFFE2E8F0),
                                                    modifier = Modifier.size(32.dp)
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
                                                        color = if (isSelected) HarvardCrimson else OxfordSlate
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
                                                    tint = HarvardCrimson,
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
                Divider(color = BorderLight)
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
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                val clean = manualSubdomainInput.trim().lowercase()
                                if (clean.isNotEmpty()) {
                                    onManualSubdomain(clean)
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HarvardCrimson),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Saqlash", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
