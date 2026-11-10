package uz.farzandim.parent.ui.screens.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val report = aiReport
    val hasRealReport = report != null && !report.reportText.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Header (the story slider has its own full-screen chrome)
        if (!hasRealReport) Row(
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
            // Genuine report: vertical "story" slider (same experience as the web)
            AiStoryPager(
                report = report!!,
                childName = selectedChild?.firstName.orEmpty(),
                onClose = onBack
            )
        }
    }
}
