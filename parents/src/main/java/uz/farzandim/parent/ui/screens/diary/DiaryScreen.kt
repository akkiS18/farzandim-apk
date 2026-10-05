package uz.farzandim.parent.ui.screens.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DoneAll
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
import uz.farzandim.parent.data.local.entity.ParentGradeEntity
import uz.farzandim.parent.ui.components.DiaryDayCard
import uz.farzandim.parent.ui.components.DiaryRowData
import uz.farzandim.parent.ui.components.GradeCommentBottomSheet
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel
import uz.farzandim.parent.util.DiaryDateHelper
import java.util.*

@Composable
fun DiaryScreen(
    viewModel: ParentViewModel,
    modifier: Modifier = Modifier
) {
    val children by viewModel.children.collectAsState()
    val selectedChildId by viewModel.selectedChildId.collectAsState()
    val selectedChild by viewModel.selectedChild.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    val holidays by viewModel.holidays.collectAsState()

    var currentMondayCal by remember {
        mutableStateOf(DiaryDateHelper.getMondayOfDate(Calendar.getInstance()))
    }
    var selectedGradeForComment by remember { mutableStateOf<ParentGradeEntity?>(null) }

    val weekDays = remember(currentMondayCal) {
        DiaryDateHelper.getWeekDays(currentMondayCal)
    }

    val weekIsoDates = remember(weekDays) {
        weekDays.map { DiaryDateHelper.toIsoDateStr(it) }
    }

    val weekGrades = remember(grades, weekIsoDates) {
        grades.filter { it.gradeDate in weekIsoDates }
    }

    val weekUnapprovedGrades = remember(weekGrades) {
        weekGrades.filter { !it.approvedByParent }
    }

    val isCurrentWeek = remember(currentMondayCal) {
        val todayMonday = DiaryDateHelper.getMondayOfDate(Calendar.getInstance())
        DiaryDateHelper.toIsoDateStr(currentMondayCal) == DiaryDateHelper.toIsoDateStr(todayMonday)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title and Child Selector
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.diary_title),
                        style = Typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Multiple children switcher chips
                    if (children.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(children) { child ->
                                val isSelected = child.id == selectedChildId
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) SkyBluePrimary else CardBackgroundSecondary,
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.clickable { viewModel.selectChild(child.id) }
                                ) {
                                    Text(
                                        text = child.firstName,
                                        style = Typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Week Switcher Controls
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardBackgroundSecondary,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                val cal = currentMondayCal.clone() as Calendar
                                cal.add(Calendar.DAY_OF_MONTH, -7)
                                currentMondayCal = cal
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Oldingi hafta",
                                tint = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DiaryDateHelper.formatWeekRange(currentMondayCal),
                                style = Typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (!isCurrentWeek) {
                                Text(
                                    text = "Bugunga qaytish",
                                    style = Typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBluePrimary,
                                    modifier = Modifier
                                        .clickable {
                                            currentMondayCal = DiaryDateHelper.getMondayOfDate(Calendar.getInstance())
                                        }
                                        .padding(top = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val cal = currentMondayCal.clone() as Calendar
                                cal.add(Calendar.DAY_OF_MONTH, 7)
                                currentMondayCal = cal
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Keyingi hafta",
                                tint = TextPrimary
                            )
                        }
                    }
                }
            }

            // Bulk Approve All Week Button
            if (weekUnapprovedGrades.isNotEmpty()) {
                item {
                    Button(
                        onClick = {
                            viewModel.approveAllWeek(weekUnapprovedGrades.map { it.id })
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SunsetOrangeAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Haftadagi ${weekUnapprovedGrades.size} ta bahoni tasdiqlash",
                                style = Typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Days 0..5 (Dushanba to Shanba)
            items(weekDays) { dayCal ->
                val isoDate = DiaryDateHelper.toIsoDateStr(dayCal)
                val dayOfWeek = ((dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1 // 1..6
                val dayLabel = DiaryDateHelper.formatDayLabel(dayCal)

                val dayHoliday = holidays.find { it.holidayDate == isoDate }

                val daySchedule = schedule.filter { it.dayOfWeek == dayOfWeek }
                    .sortedBy { it.lessonNumber }
                val dayGrades = grades.filter { it.gradeDate == isoDate }

                // Build DiaryRowData list
                val rows = mutableListOf<DiaryRowData>()
                val matchedGradeIds = mutableSetOf<Int>()

                daySchedule.forEach { sched ->
                    val isMasteryType = { g: ParentGradeEntity ->
                        g.gradeType == "MASTERY" || g.gradeType == "DAILY" || (g.gradeType != "BEHAVIOR" && g.gradeType != "ATTENDANCE")
                    }
                    val isSubjectMatch = { g: ParentGradeEntity ->
                        (g.subjectId != null && g.subjectId == sched.subjectId) ||
                                g.subjectName.trim().equals(sched.subjectName.trim(), ignoreCase = true)
                    }
                    val isLessonMatch = { g: ParentGradeEntity ->
                        if (g.lessonNumber != null && g.lessonNumber > 0) g.lessonNumber == sched.lessonNumber else true
                    }

                    // Priority 1: Match by lessonNumber + subject
                    var mastery = dayGrades.find { g ->
                        !matchedGradeIds.contains(g.id) && isLessonMatch(g) && isSubjectMatch(g) && isMasteryType(g)
                    }
                    if (mastery == null) {
                        mastery = dayGrades.find { g ->
                            !matchedGradeIds.contains(g.id) && isSubjectMatch(g) && isMasteryType(g)
                        }
                    }
                    mastery?.let { matchedGradeIds.add(it.id) }

                    var behavior = dayGrades.find { g ->
                        !matchedGradeIds.contains(g.id) && isLessonMatch(g) && isSubjectMatch(g) && g.gradeType == "BEHAVIOR"
                    }
                    if (behavior == null) {
                        behavior = dayGrades.find { g ->
                            !matchedGradeIds.contains(g.id) && isSubjectMatch(g) && g.gradeType == "BEHAVIOR"
                        }
                    }
                    behavior?.let { matchedGradeIds.add(it.id) }

                    var attendance = dayGrades.find { g ->
                        !matchedGradeIds.contains(g.id) && isLessonMatch(g) && isSubjectMatch(g) && g.gradeType == "ATTENDANCE"
                    }
                    if (attendance == null) {
                        attendance = dayGrades.find { g ->
                            !matchedGradeIds.contains(g.id) && isSubjectMatch(g) && g.gradeType == "ATTENDANCE"
                        }
                    }
                    attendance?.let { matchedGradeIds.add(it.id) }

                    rows.add(
                        DiaryRowData(
                            lessonNumber = sched.lessonNumber,
                            subjectName = sched.subjectName,
                            teacherName = sched.teacherName,
                            homework = sched.homework,
                            topicName = sched.topicName,
                            masteryGrade = mastery,
                            behaviorGrade = behavior,
                            attendanceGrade = attendance
                        )
                    )
                }

                // Add any unmatched grades for this day
                val unmatched = dayGrades.filter { !matchedGradeIds.contains(it.id) }
                unmatched.forEachIndexed { idx, g ->
                    val isMastery = g.gradeType == "MASTERY" || g.gradeType == "DAILY" || (g.gradeType != "BEHAVIOR" && g.gradeType != "ATTENDANCE")
                    rows.add(
                        DiaryRowData(
                            lessonNumber = daySchedule.size + idx + 1,
                            subjectName = g.subjectName,
                            teacherName = g.teacherName,
                            masteryGrade = if (isMastery) g else null,
                            behaviorGrade = if (g.gradeType == "BEHAVIOR") g else null,
                            attendanceGrade = if (g.gradeType == "ATTENDANCE") g else null
                        )
                    )
                }

                DiaryDayCard(
                    dayLabel = dayLabel,
                    rows = rows,
                    holiday = dayHoliday,
                    onApproveGrade = { gradeId -> viewModel.approveGrade(gradeId) },
                    onGradeClick = { grade -> selectedGradeForComment = grade }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Grade Comment Bottom Sheet
        if (selectedGradeForComment != null) {
            GradeCommentBottomSheet(
                grade = selectedGradeForComment,
                onDismiss = { selectedGradeForComment = null },
                onSendComment = { gradeId, content ->
                    viewModel.createGradeComment(gradeId, content)
                }
            )
        }
    }
}
