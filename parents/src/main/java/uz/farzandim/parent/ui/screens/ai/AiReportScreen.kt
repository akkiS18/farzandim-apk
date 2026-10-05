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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
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
import uz.farzandim.parent.ui.viewmodel.ParentViewModel

@Composable
fun AiReportScreen(
    viewModel: ParentViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aiReport by viewModel.aiReport.collectAsState()
    val selectedChild by viewModel.selectedChild.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAiReport()
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
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Hero Card
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

                    Text(
                        text = aiReport?.summary
                            ?: "Farzandingiz ${selectedChild?.firstName ?: ""}ning so'nggi haftalardagi o'zlashtirish ko'rsatkichlari, qiziqishlari va darsdagi faolligi asosida tuzilgan avtomatik hisobot.",
                        style = Typography.bodyMedium,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 22.sp
                    )
                }
            }

            // Strengths
            val strengths = aiReport?.strengths ?: listOf(
                "Matematika fanidan a'lo darajadagi mantiqiy fikrlash",
                "Darslarga o'z vaqtida qatnashish va faol javob berish",
                "Guruh bilan ishlashda do'stona xulq-atvor"
            )

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GradeMintText,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Kuchli tomonlari",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    strengths.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .background(GradeMintText, CircleShape)
                            )
                            Text(
                                text = item,
                                style = Typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Areas to improve
            val areas = aiReport?.areasToImprove ?: listOf(
                "Ingliz tili lug'at boyligini yanada oshirish tavsiya etiladi",
                "Uy vazifalarini kechiktirmasdan bajarishga e'tibor qaratish"
            )

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = GradeAmberText,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Rivojlantirish mumkin bo'lgan sohalar",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    areas.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .background(GradeAmberText, CircleShape)
                            )
                            Text(
                                text = item,
                                style = Typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Recommendations
            val recommendations = aiReport?.recommendations ?: listOf(
                "Har kuni 20 daqiqa mustaqil badiiy kitob o'qish",
                "Dam olish kunlarida maktab kutubxonasidagi qo'shimcha adabiyotlardan foydalanish"
            )

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = SkyBluePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Ota-onaga tavsiyalar",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    recommendations.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .background(SkyBluePrimary, CircleShape)
                            )
                            Text(
                                text = item,
                                style = Typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
