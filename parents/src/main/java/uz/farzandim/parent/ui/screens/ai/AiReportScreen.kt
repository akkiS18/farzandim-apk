package uz.farzandim.parent.ui.screens.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel

@Composable
fun AiReportScreen(
    viewModel: ParentViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aiReport by viewModel.aiReport.collectAsState()
    val isAiReportLoading by viewModel.isAiReportLoading.collectAsState()
    val selectedChild by viewModel.selectedChild.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()

    LaunchedEffect(selectedChildId) {
        if (selectedChildId != null) {
            viewModel.fetchAiReport(selectedChildId)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Text(
                text = stringResource(id = R.string.service_ai_report),
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isAiReportLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = SkyBluePrimary
                )
            } else {
                IconButton(onClick = { viewModel.fetchAiReport() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = TextSecondary
                    )
                }
            }
        }

        val report = aiReport
        val hasRealReport = report != null && !report.reportText.isNullOrBlank()

        if (isAiReportLoading && report == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = SkyBluePrimary)
                    Text(
                        text = "Hisobot yuklanmoqda...",
                        style = Typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else if (!hasRealReport) {
            // Honest empty state when no report has been generated on backend
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SkyBlueContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Text(
                            text = "Haftalik AI hisoboti mavjud emas",
                            style = Typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        val childName = selectedChild?.firstName ?: "Farzandingiz"
                        Text(
                            text = "$childName uchun ushbu davrda sun'iy intellekt haftalik tahlili hali shakllantirilmagan. Maktab ma'muriyati yoki tizim hisobot generatsiya qilgach, natijalar shu yerda avtomatik ko'rinadi.",
                            style = Typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = { viewModel.fetchAiReport() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SkyBluePrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = SkyBluePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Qayta tekshirish",
                                color = SkyBluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        } else {
            // Genuine report from backend
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Card with week metadata
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = SkyBlueLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Sun'iy Intellekt Tahlili",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        val childName = selectedChild?.firstName ?: ""
                        val weekInfo = if (!report?.startDate.isNullOrBlank() && !report?.endDate.isNullOrBlank()) {
                            "${report?.startDate} — ${report?.endDate}"
                        } else if (report?.weekNumber != null && report?.year != null) {
                            "${report.year}-yil, ${report.weekNumber}-hafta"
                        } else {
                            "Haftalik tahlil"
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$childName • $weekInfo",
                                style = Typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Summary Stats from backend summary_json (if available)
                val summary = report?.summaryJson
                if (summary != null && summary.isNotEmpty()) {
                    val avgGrade = (summary["average_grade"] as? Number)?.toDouble()
                    val trend = summary["grade_trend"] as? String
                    val totalGrades = (summary["total_grades"] as? Number)?.toInt()
                    val booksRead = (summary["books_read_count"] as? Number)?.toInt()

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (avgGrade != null && avgGrade > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format(java.util.Locale.US, "%.1f", avgGrade),
                                        style = Typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBluePrimary
                                    )
                                    Text(
                                        text = "O'rtacha baho",
                                        style = Typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (!trend.isNullOrBlank()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val icon = when (trend.uppercase()) {
                                            "UP" -> Icons.AutoMirrored.Filled.TrendingUp
                                            "DOWN" -> Icons.AutoMirrored.Filled.TrendingDown
                                            else -> Icons.AutoMirrored.Filled.TrendingFlat
                                        }
                                        val color = when (trend.uppercase()) {
                                            "UP" -> GradeMintText
                                            "DOWN" -> GradeCoralText
                                            else -> SkyBluePrimary
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (trend.uppercase()) {
                                                "UP" -> "O'sish"
                                                "DOWN" -> "Pasayish"
                                                else -> "Barqaror"
                                            },
                                            style = Typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                    Text(
                                        text = "Dinamika",
                                        style = Typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (totalGrades != null && totalGrades > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$totalGrades ta",
                                        style = Typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Baholar soni",
                                        style = Typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (booksRead != null && booksRead > 0) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$booksRead ta",
                                        style = Typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Kitob mutolaasi",
                                        style = Typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Report Text from AI
                val fullText = report?.reportText.orEmpty()
                if (fullText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = SkyBluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Haftalik Xulosa va Tahlil",
                                    style = Typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = fullText,
                                style = Typography.bodyMedium,
                                color = TextPrimary,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
