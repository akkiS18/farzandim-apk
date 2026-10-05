package uz.farzandim.parent.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.parent.R
import uz.farzandim.parent.data.local.entity.HolidayEntity
import uz.farzandim.parent.data.local.entity.ParentGradeEntity
import uz.farzandim.parent.data.local.entity.ParentScheduleEntity
import uz.farzandim.parent.ui.theme.*

data class DiaryRowData(
    val lessonNumber: Int,
    val subjectName: String,
    val teacherName: String?,
    val homework: String? = null,
    val topicName: String? = null,
    val masteryGrade: ParentGradeEntity? = null,
    val behaviorGrade: ParentGradeEntity? = null,
    val attendanceGrade: ParentGradeEntity? = null
)

@Composable
fun DiaryDayCard(
    dayLabel: String,
    rows: List<DiaryRowData>,
    holiday: HolidayEntity? = null,
    onApproveGrade: (Int) -> Unit,
    onGradeClick: (ParentGradeEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Day Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackgroundSecondary)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SkyBluePrimary)
                    )
                    Text(
                        text = dayLabel.uppercase(),
                        style = Typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                if (holiday != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SunsetOrangeContainer
                    ) {
                        Text(
                            text = holiday.name,
                            style = Typography.labelSmall,
                            color = SunsetOrangeDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardBorder.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${rows.size} dars",
                            style = Typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = CardBorder, thickness = 1.dp)

            // Content: Holiday Banner or Lesson Rows
            if (holiday != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SunsetOrangeContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = SunsetOrangeAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = stringResource(id = R.string.holiday_banner),
                            style = Typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = holiday.name,
                            style = Typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_lessons),
                        style = Typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                // Lesson Rows
                rows.forEachIndexed { index, row ->
                    DiaryLessonRow(
                        row = row,
                        isLast = index == rows.size - 1,
                        onApproveGrade = onApproveGrade,
                        onGradeClick = onGradeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DiaryLessonRow(
    row: DiaryRowData,
    isLast: Boolean,
    onApproveGrade: (Int) -> Unit,
    onGradeClick: (ParentGradeEntity) -> Unit
) {
    val presentGrades = listOfNotNull(row.masteryGrade, row.behaviorGrade, row.attendanceGrade)
    val unapprovedGrade = presentGrades.find { !it.approvedByParent }
    val isFullyApproved = presentGrades.isNotEmpty() && presentGrades.all { it.approvedByParent }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Lesson Number & Subject Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = SkyBlueContainer,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = row.lessonNumber.toString(),
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SkyBluePrimary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = row.subjectName,
                        style = Typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!row.homework.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Vazifa:",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SkyBluePrimary
                            )
                            Text(
                                text = row.homework.trim(),
                                style = Typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else if (!row.topicName.isNullOrBlank() && row.topicName != "Dars mashg'uloti") {
                        Text(
                            text = row.topicName.trim(),
                            style = Typography.labelSmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    } else {
                        row.teacherName?.let { teacher ->
                            Text(
                                text = teacher,
                                style = Typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Right: Badges & Action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Mastery Grade Badge
                row.masteryGrade?.let { mastery ->
                    GradeBadge(
                        value = mastery.value,
                        gradeType = "MASTERY",
                        onClick = { onGradeClick(mastery) }
                    )
                }

                // 2. Behavior Grade Badge
                row.behaviorGrade?.let { behavior ->
                    GradeBadge(
                        value = "★ ${behavior.value}",
                        gradeType = "BEHAVIOR",
                        onClick = { onGradeClick(behavior) }
                    )
                }

                // 3. Attendance Badge
                row.attendanceGrade?.let { att ->
                    GradeBadge(
                        value = att.value,
                        gradeType = "ATTENDANCE",
                        onClick = { onGradeClick(att) }
                    )
                }

                // Approval Status or Button
                if (unapprovedGrade != null) {
                    Button(
                        onClick = { onApproveGrade(unapprovedGrade.id) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SunsetOrangeAccent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.approve_grade_btn),
                            style = Typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isFullyApproved) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GradeMintBg,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GradeMintText,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.approved_badge),
                                style = Typography.labelSmall,
                                color = GradeMintText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        if (!isLast) {
            HorizontalDivider(
                color = BorderLight.copy(alpha = 0.6f),
                thickness = 0.8.dp,
                modifier = Modifier.padding(start = 50.dp, end = 14.dp)
            )
        }
    }
}

@Composable
fun GradeBadge(
    value: String,
    gradeType: String,
    onClick: () -> Unit
) {
    val (bg, text) = when (gradeType) {
        "ATTENDANCE" -> when (value) {
            "+" -> Pair(GradeMintBg, GradeMintText)
            "-" -> Pair(GradeCoralBg, GradeCoralText)
            "k" -> Pair(GradeAmberBg, GradeAmberText)
            else -> Pair(SkyBlueContainer, SkyBluePrimary)
        }
        "BEHAVIOR" -> Pair(BehaviorVioletBg, BehaviorVioletText)
        else -> when (value) {
            "5" -> Pair(GradeMintBg, GradeMintText)
            "4" -> Pair(SkyBlueContainer, SkyBluePrimary)
            "3" -> Pair(GradeAmberBg, GradeAmberText)
            "2" -> Pair(GradeCoralBg, GradeCoralText)
            else -> Pair(CardBackgroundSecondary, TextPrimary)
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(1.dp, text.copy(alpha = 0.2f)),
        modifier = Modifier
            .defaultMinSize(minWidth = 28.dp)
            .height(28.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = text,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }
    }
}
