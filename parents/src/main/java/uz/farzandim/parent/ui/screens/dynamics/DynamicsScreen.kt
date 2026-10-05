package uz.farzandim.parent.ui.screens.dynamics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.data.local.entity.ParentGradeEntity
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel
import java.util.*

enum class QuarterFilter(val labelRes: Int, val startMonth: Int, val endMonth: Int) {
    Q1(R.string.quarter_1, 8, 10),  // Sep - Oct
    Q2(R.string.quarter_2, 10, 11), // Nov - Dec
    Q3(R.string.quarter_3, 0, 2),   // Jan - Mar
    Q4(R.string.quarter_4, 3, 4)    // Apr - May
}

data class SubjectStat(
    val subjectName: String,
    val averageScore: Double,
    val totalCount: Int
)

@Composable
fun DynamicsScreen(
    viewModel: ParentViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grades by viewModel.grades.collectAsState()
    val selectedChild by viewModel.selectedChild.collectAsState()

    var selectedQuarter by remember { mutableStateOf(QuarterFilter.Q1) }

    // Filter only numeric mastery grades
    val masteryGrades = remember(grades) {
        grades.filter {
            (it.gradeType == "MASTERY" || it.gradeType == "DAILY" || (it.gradeType != "BEHAVIOR" && it.gradeType != "ATTENDANCE")) &&
                    (it.numericValue != null || it.value.toDoubleOrNull() != null)
        }.sortedBy { it.gradeDate }
    }

    val overallAverage = remember(masteryGrades) {
        if (masteryGrades.isEmpty()) 0.0
        else {
            val sum = masteryGrades.sumOf { it.numericValue ?: it.value.toDoubleOrNull() ?: 0.0 }
            sum / masteryGrades.size
        }
    }

    val subjectStats = remember(masteryGrades) {
        masteryGrades.groupBy { it.subjectName }
            .map { (subject, list) ->
                val avg = list.sumOf { it.numericValue ?: it.value.toDoubleOrNull() ?: 0.0 } / list.size
                SubjectStat(subject, avg, list.size)
            }
            .sortedByDescending { it.averageScore }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top App Bar
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
                text = stringResource(id = R.string.dynamics_title),
                style = Typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quarter Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuarterFilter.values().forEach { q ->
                        val isSelected = q == selectedQuarter
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) SkyBluePrimary else CardBackgroundSecondary,
                            border = if (isSelected) null else BorderStroke(1.dp, CardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedQuarter = q }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = q.labelRes),
                                    style = Typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Overview Summary Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.average_score),
                                style = Typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (overallAverage > 0) String.format(Locale.US, "%.1f / 5.0", overallAverage) else "—",
                                style = Typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = SkyBluePrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (overallAverage >= 4.0) GradeMintBg else GradeAmberBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = if (overallAverage >= 4.0) GradeMintText else GradeAmberText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (overallAverage >= 4.5) "A'lo natija" else if (overallAverage >= 3.5) "Yaxshi" else "O'rtacha",
                                    style = Typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (overallAverage >= 4.0) GradeMintText else GradeAmberText
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Canvas Line Chart
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Baholar Grafigi",
                            style = Typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (masteryGrades.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Grafik uchun ma'lumotlar yetarli emas",
                                    style = Typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        } else {
                            val values = masteryGrades.mapNotNull {
                                it.numericValue ?: it.value.toDoubleOrNull()
                            }
                            CanvasLineChart(
                                values = values,
                                average = overallAverage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            )
                        }
                    }
                }
            }

            // Subject Performance Breakdown Header
            item {
                Text(
                    text = "Fanlar bo'yicha Tahlil",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Subject Performance Cards
            items(subjectStats) { stat ->
                SubjectPerformanceCard(stat = stat)
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CanvasLineChart(
    values: List<Double>,
    average: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val paddingLeft = 16f
        val paddingRight = 16f
        val paddingTop = 20f
        val paddingBottom = 20f

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        // Min 1.0, Max 5.0 scale
        val minY = 1.0
        val maxY = 5.0

        fun getY(v: Double): Float {
            val normalized = ((v - minY) / (maxY - minY)).coerceIn(0.0, 1.0)
            return (paddingTop + graphHeight * (1.0 - normalized)).toFloat()
        }

        // Draw average reference line
        if (average in 1.0..5.0) {
            val avgY = getY(average)
            drawLine(
                color = SkyBlueLight.copy(alpha = 0.5f),
                start = Offset(paddingLeft, avgY),
                end = Offset(width - paddingRight, avgY),
                strokeWidth = 2f
            )
        }

        if (values.size > 1) {
            val stepX = graphWidth / (values.size - 1)

            val linePath = Path()
            val fillPath = Path()

            values.forEachIndexed { i, v ->
                val x = paddingLeft + i * stepX
                val y = getY(v)

                if (i == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, height - paddingBottom)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(paddingLeft + (values.size - 1) * stepX, height - paddingBottom)
            fillPath.close()

            // Draw gradient area under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SkyBluePrimary.copy(alpha = 0.25f),
                        SkyBluePrimary.copy(alpha = 0.02f)
                    ),
                    startY = paddingTop,
                    endY = height - paddingBottom
                )
            )

            // Draw line
            drawPath(
                path = linePath,
                color = SkyBluePrimary,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw points
            values.forEachIndexed { i, v ->
                val x = paddingLeft + i * stepX
                val y = getY(v)

                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = SkyBluePrimary,
                    radius = 3.5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        } else if (values.size == 1) {
            val x = width / 2f
            val y = getY(values[0])
            drawCircle(
                color = SkyBluePrimary,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun SubjectPerformanceCard(stat: SubjectStat) {
    val (badgeBg, badgeText, badgeLabel) = when {
        stat.averageScore >= 4.5 -> Triple(GradeMintBg, GradeMintText, "A'lo (5)")
        stat.averageScore >= 3.5 -> Triple(SkyBlueContainer, SkyBluePrimary, "Yaxshi (4)")
        stat.averageScore >= 2.5 -> Triple(GradeAmberBg, GradeAmberText, "Qoniqarli (3)")
        else -> Triple(GradeCoralBg, GradeCoralText, "Past (2)")
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.subjectName,
                    style = Typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${stat.totalCount} ta baho",
                    style = Typography.bodySmall,
                    color = TextMuted
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.1f", stat.averageScore),
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeLabel,
                        style = Typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
