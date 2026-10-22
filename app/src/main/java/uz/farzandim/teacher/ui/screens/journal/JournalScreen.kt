package uz.farzandim.teacher.ui.screens.journal

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import uz.farzandim.teacher.data.local.entity.GradeEntity
import uz.farzandim.teacher.data.local.entity.StudentEntity
import uz.farzandim.teacher.ui.theme.*
import uz.farzandim.teacher.ui.viewmodel.JournalViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import java.text.SimpleDateFormat
import java.util.*

fun formatShortUzbekDate(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = sdf.parse(dateStr) ?: Date()
        val cal = Calendar.getInstance().apply { time = d }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthShort = arrayOf("Yan", "Fev", "Mar", "Apr", "May", "Iyun", "Iyul", "Avg", "Sen", "Okt", "Noy", "Dek")
        "$day-${monthShort[cal.get(Calendar.MONTH)]}"
    } catch (_: Exception) {
        dateStr
    }
}

data class WeekdayTheme(
    val day: Int,
    val name: String,
    val shortName: String,
    val badgeBg: Color,
    val badgeText: Color,
    val border: Color,
    val bg: Color,
    val textColor: Color
)

fun getWeekdayTheme(dateStr: String): WeekdayTheme {
    val dayOfWeek = try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val d = sdf.parse(dateStr) ?: Date()
        val cal = Calendar.getInstance().apply { time = d }
        cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Yakshanba, 1 = Dushanba...
    } catch (_: Exception) {
        1
    }
    return when (dayOfWeek) {
        0 -> WeekdayTheme(0, "Yakshanba", "Yak", Color(0xFFE11D48), Color.White, Color(0xFFFDA4AF), Color(0xFFFFF1F2), Color(0xFF881337))
        1 -> WeekdayTheme(1, "Dushanba", "Dush", Color(0xFF2563EB), Color.White, Color(0xFF93C5FD), Color(0xFFEFF6FF), Color(0xFF1E3A8A))
        2 -> WeekdayTheme(2, "Seshanba", "Sesh", Color(0xFF059669), Color.White, Color(0xFF6EE7B7), Color(0xFFECFDF5), Color(0xFF064E3B))
        3 -> WeekdayTheme(3, "Chorshanba", "Chor", Color(0xFF9333EA), Color.White, Color(0xFFD8B4FE), Color(0xFFFAF5FF), Color(0xFF581C87))
        4 -> WeekdayTheme(4, "Payshanba", "Pay", Color(0xFFD97706), Color.White, Color(0xFFFCD34D), Color(0xFFFFFBEB), Color(0xFF78350F))
        5 -> WeekdayTheme(5, "Juma", "Jum", Color(0xFF0D9488), Color.White, Color(0xFF5EEAD4), Color(0xFFF0FDFA), Color(0xFF134E4A))
        6 -> WeekdayTheme(6, "Shanba", "Shan", Color(0xFFEA580C), Color.White, Color(0xFFFDBA74), Color(0xFFFFF7ED), Color(0xFF7C2D12))
        else -> WeekdayTheme(1, "Dushanba", "Dush", Color(0xFF2563EB), Color.White, Color(0xFF93C5FD), Color(0xFFEFF6FF), Color(0xFF1E3A8A))
    }
}

@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onOpenComment: (localGradeId: String, studentName: String, className: String, subjectName: String, date: String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val classes by viewModel.classes.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val selectedLessonNumber by viewModel.selectedLessonNumber.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val journalLessonsToday by viewModel.journalLessonsToday.collectAsState()
    val currentTopic by viewModel.currentTopic.collectAsState()
    val currentHomework by viewModel.currentHomework.collectAsState()
    val isTopicLoading by viewModel.isTopicLoading.collectAsState()
    val isNavigatingLesson by viewModel.isNavigatingLesson.collectAsState()
    val isSavingTopic by viewModel.isSavingTopic.collectAsState()
    val students by viewModel.students.collectAsState()
    val grades by viewModel.grades.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedGradeCategory by viewModel.selectedGradeCategory.collectAsState()
    val lastGradeDaysAgoMap by viewModel.lastGradeDaysAgoMap.collectAsState()
    val conflicts by viewModel.conflicts.collectAsState()
    val isResolvingConflict by viewModel.isResolvingConflict.collectAsState()

    val currentDayTheme = remember(selectedDate) { getWeekdayTheme(selectedDate) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshCurrentJournal()
    }

    var showClassDropdown by remember { mutableStateOf(false) }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTopicEditDialog by remember { mutableStateOf(false) }
    var editTopicInput by remember { mutableStateOf("") }
    var editHomeworkInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val curClassName = classes.find { it.id == selectedClassId }?.name
    val curSubjectName = subjects.find { it.id == selectedSubjectId }?.name
        ?: journalLessonsToday.find { it.subjectId == selectedSubjectId }?.subjectName

    val datePickerDialog = remember(selectedDate) {
        val cal = Calendar.getInstance().apply {
            try {
                time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate) ?: Date()
            } catch (_: Exception) {}
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(newCal.time)
                viewModel.selectDate(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    val neutralBorder = Color(0xFFE2E8F0)
    val slateBg = Color(0xFFF8FAFC)
    val crimsonColor = Color(0xFFA51C30)
    val darkSlateColor = Color(0xFF1E2B42)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── TOP CONTROL STRIP (Web 1-to-1) ──
        Surface(
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Class Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                        color = slateBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable { showClassDropdown = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = curClassName ?: "Sinf",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedClassId != null) darkSlateColor else Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showClassDropdown,
                        onDismissRequest = { showClassDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        classes.forEach { cls ->
                            val isSel = selectedClassId == cls.id
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cls.name,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) crimsonColor else darkSlateColor
                                        )
                                        if (cls.isMainTeacher) {
                                            Text("★", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectClass(cls.id)
                                    showClassDropdown = false
                                }
                            )
                        }
                    }
                }

                // 2. Subject Dropdown (enabled when class selected and lessons exist today)
                Box(modifier = Modifier.weight(1.3f)) {
                    val hasLessonsToday = journalLessonsToday.isNotEmpty()
                    val subjectDisplayText = when {
                        selectedSubjectId != null && curSubjectName != null -> "${selectedLessonNumber}-soat: $curSubjectName"
                        selectedClassId != null && !hasLessonsToday -> "Darslar yo'q"
                        else -> "Fan"
                    }

                    Surface(
                        border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                        color = if (selectedClassId != null && hasLessonsToday) slateBg else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable(enabled = selectedClassId != null && hasLessonsToday) { showSubjectDropdown = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subjectDisplayText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedSubjectId != null) darkSlateColor else Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = if (selectedClassId != null && hasLessonsToday) Color(0xFF64748B) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSubjectDropdown,
                        onDismissRequest = { showSubjectDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (journalLessonsToday.isNotEmpty()) {
                            Text(
                                text = "BUGUNGI DARSLAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            journalLessonsToday.forEach { lesson ->
                                val isSel = selectedSubjectId == lesson.subjectId && selectedLessonNumber == lesson.lessonNumber
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${lesson.lessonNumber}-soat: ${lesson.subjectName}",
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) crimsonColor else darkSlateColor
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectSubject(lesson.subjectId, lesson.lessonNumber)
                                        showSubjectDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Unified Date & Lesson Navigator (< Oldingi | DUSH 4-Sen | Keyingi >)
                Surface(
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentDayTheme.border),
                    color = currentDayTheme.bg,
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Lesson Button (<)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable(
                                    enabled = selectedClassId != null && selectedSubjectId != null && !isNavigatingLesson
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.navigateAdjacentLesson("previous")
                                }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isNavigatingLesson) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = currentDayTheme.textColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Oldingi dars",
                                    tint = if (selectedClassId != null && selectedSubjectId != null) currentDayTheme.textColor else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Date Trigger with Weekday Badge
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = currentDayTheme.badgeBg,
                                shape = RoundedCornerShape(3.dp)
                            ) {
                                Text(
                                    text = currentDayTheme.shortName.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentDayTheme.badgeText,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = formatShortUzbekDate(selectedDate),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentDayTheme.textColor
                            )
                        }

                        // Next Lesson Button (>)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .clickable(
                                    enabled = selectedClassId != null && selectedSubjectId != null && !isNavigatingLesson
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.navigateAdjacentLesson("next")
                                }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isNavigatingLesson) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = currentDayTheme.textColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Keyingi dars",
                                    tint = if (selectedClassId != null && selectedSubjectId != null) currentDayTheme.textColor else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Grade Type Selector Dropdown (Kunlik, Choraklik, Imtihon)
                if (selectedClassId != null && selectedSubjectId != null) {
                    val (catIcon, catName) = when (selectedGradeCategory) {
                        "QUARTERLY_EXAM" -> Pair(Icons.Default.Assessment, "Choraklik")
                        "SEMESTER_EXAM" -> Pair(Icons.Default.WorkspacePremium, "Imtihon")
                        else -> Pair(Icons.Default.EditCalendar, "Kunlik")
                    }

                    Box {
                        Surface(
                            border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                            color = slateBg,
                            modifier = Modifier
                                .height(40.dp)
                                .clickable { showCategoryDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = catName,
                                    tint = crimsonColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.EditCalendar,
                                        contentDescription = null,
                                        tint = if (selectedGradeCategory == "DAILY") crimsonColor else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Kunlik",
                                        fontWeight = if (selectedGradeCategory == "DAILY") FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedGradeCategory == "DAILY") crimsonColor else darkSlateColor
                                    )
                                },
                                onClick = {
                                    viewModel.selectGradeCategory("DAILY")
                                    showCategoryDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = if (selectedGradeCategory == "QUARTERLY_EXAM") crimsonColor else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Choraklik",
                                        fontWeight = if (selectedGradeCategory == "QUARTERLY_EXAM") FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedGradeCategory == "QUARTERLY_EXAM") crimsonColor else darkSlateColor
                                    )
                                },
                                onClick = {
                                    viewModel.selectGradeCategory("QUARTERLY_EXAM")
                                    showCategoryDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = if (selectedGradeCategory == "SEMESTER_EXAM") crimsonColor else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Imtihon",
                                        fontWeight = if (selectedGradeCategory == "SEMESTER_EXAM") FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedGradeCategory == "SEMESTER_EXAM") crimsonColor else darkSlateColor
                                    )
                                },
                                onClick = {
                                    viewModel.selectGradeCategory("SEMESTER_EXAM")
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── TOPIC & HEADER INFO (When both Class and Subject selected) ──
        if (selectedClassId != null && selectedSubjectId != null && curClassName != null && curSubjectName != null) {
            Surface(
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$curClassName • ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = darkSlateColor
                            )
                            Text(
                                text = curSubjectName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = crimsonColor
                            )
                            Text(
                                text = " (${selectedLessonNumber}-soat)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = darkSlateColor
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${students.size} TA O'QUVCHI • KUNDALIK BAHOLASH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.refreshCurrentJournal()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = crimsonColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Yangilash",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // Topic row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "📘 MAVZU: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                if (isTopicLoading) {
                                    Text(
                                        text = "Yuklanmoqda...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                } else if (!currentTopic.isNullOrBlank()) {
                                    Text(
                                        text = currentTopic!!,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = darkSlateColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "Mavzu belgilanmagan",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Homework row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "📝 VAZIFA: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F46E5)
                                )
                                if (isTopicLoading) {
                                    Text(
                                        text = "Yuklanmoqda...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                } else if (!currentHomework.isNullOrBlank()) {
                                    Text(
                                        text = currentHomework!!,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF3730A3),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "Vazifa kiritilmagan",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }

                        // Edit Topic & Homework button
                        Surface(
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.clickable {
                                editTopicInput = currentTopic ?: ""
                                editHomeworkInput = currentHomework ?: ""
                                showTopicEditDialog = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Tahrirlash",
                                    tint = crimsonColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Tahrirlash",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = crimsonColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── TOPIC & HOMEWORK EDIT DIALOG ──
        if (showTopicEditDialog) {
            AlertDialog(
                onDismissRequest = { if (!isSavingTopic) showTopicEditDialog = false },
                title = {
                    Text(
                        text = "Dars Mavzusi va Uyga Vazifa",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = darkSlateColor
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$curClassName • $curSubjectName (${selectedLessonNumber}-soat)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = crimsonColor
                        )
                        OutlinedTextField(
                            value = editTopicInput,
                            onValueChange = { editTopicInput = it },
                            label = { Text("Dars mavzusi") },
                            placeholder = { Text("Masalan: Kvadrat tenglamalar") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editHomeworkInput,
                            onValueChange = { editHomeworkInput = it },
                            label = { Text("Uyga vazifa") },
                            placeholder = { Text("Masalan: 12-mashq, 45-bet") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveTopicAndHomework(editTopicInput, editHomeworkInput)
                            showTopicEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = crimsonColor),
                        enabled = !isSavingTopic
                    ) {
                        if (isSavingTopic) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Saqlash", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTopicEditDialog = false },
                        enabled = !isSavingTopic
                    ) {
                        Text("Bekor qilish", color = Color(0xFF64748B))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // ── MULTI-STAGE CONTENT FLOW ──
        if (selectedClassId == null) {
            // ── 1-BOSQICH: SINF TANLASH ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "1-BOSQICH: JURNALNI OCHISH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Sinfni tanlang",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = darkSlateColor
                    )
                    Text(
                        text = "Baholash jurnalini ko'rish yoki baho qo'yish uchun quyidagi sinflardan birini tanlang:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(classes) { _, cls ->
                        Surface(
                            border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                            color = Color.White,
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectClass(cls.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "MAKTAB SINFI",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B),
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "${cls.name} SINFI",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Serif,
                                        color = crimsonColor
                                    )
                                    if (cls.isMainTeacher) {
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = "★ SINF RAHBARI",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF92400E),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (!cls.defaultSubjectName.isNullOrBlank()) {
                                        Surface(
                                            color = Color(0xFFEFF6FF),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text(
                                                text = "Fan: ${cls.defaultSubjectName}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D4ED8),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedSubjectId == null) {
            // ── 2-BOSQICH: DARSNI TANLASH ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "2-BOSQICH: DARSNI TANLASH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Fanni tanlang",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = darkSlateColor
                    )
                    Text(
                        text = "Bugungi dars jadvalidan kerakli darsni tanlang:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                val curSelectedClass = classes.find { it.id == selectedClassId }
                val prioritizedLessons = remember(journalLessonsToday, curSelectedClass) {
                    journalLessonsToday.sortedWith(
                        compareByDescending<uz.farzandim.teacher.data.model.JournalLessonItem> {
                            curSelectedClass?.defaultSubjectId != null && it.subjectId == curSelectedClass.defaultSubjectId
                        }.thenBy { it.lessonNumber }
                    )
                }

                if (prioritizedLessons.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(prioritizedLessons) { _, lesson ->
                            val isPrimarySubject = curSelectedClass?.defaultSubjectId == lesson.subjectId
                            Surface(
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isPrimarySubject) Color(0xFF93C5FD) else neutralBorder
                                ),
                                color = if (isPrimarySubject) Color(0xFFF8FAFC) else Color.White,
                                shape = RoundedCornerShape(0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectSubject(lesson.subjectId, lesson.lessonNumber) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${lesson.lessonNumber}-SOAT DARSI",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B),
                                                letterSpacing = 0.5.sp
                                            )
                                            if (isPrimarySubject) {
                                                Surface(
                                                    color = Color(0xFFEFF6FF),
                                                    shape = RoundedCornerShape(2.dp)
                                                ) {
                                                    Text(
                                                        text = "★ ASOSIY FANINGIZ",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1D4ED8),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = lesson.subjectName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = crimsonColor
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = if (isPrimarySubject) Color(0xFF2563EB) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val journalDayName = remember(selectedDate) {
                        try {
                            val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)
                            val cal = Calendar.getInstance().apply { time = d ?: Date() }
                            val weekDays = listOf("Yakshanba", "Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba")
                            weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
                        } catch (_: Exception) { "" }
                    }

                    Surface(
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Weekend,
                                contentDescription = null,
                                tint = crimsonColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (journalDayName.isNotBlank()) "$journalDayName — Dam Olish Kuni" else "Dam Olish Kuni",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = darkSlateColor,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tanlangan sanada ushbu sinf uchun dars jadvali belgilanmagan (Dam olish kuni). Dars jadvalini ko'rish yoki baholash uchun yuqoridagi kalendardan boshqa sanani tanlang.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        } else {
            // ── 3-BOSQICH: GRADING MATRIX TABLE (Web 1-to-1) ──
            val horizontalScrollState = rememberScrollState()

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Table Header
                    Surface(
                        color = slateBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(horizontalScrollState)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "№",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkSlateColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                            Text(
                                text = "O'QUVCHI ISMI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkSlateColor,
                                modifier = Modifier
                                    .width(170.dp)
                                    .padding(horizontal = 6.dp)
                            )
                            Text(
                                text = "DAVOMAT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkSlateColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(72.dp)
                            )
                            Text(
                                text = "XULQI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkSlateColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(72.dp)
                            )
                            Text(
                                text = "O'ZLASHTIRISH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkSlateColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = "IZOH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = darkSlateColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(50.dp)
                            )
                        }
                    }

                    // Table Rows (Students List)
                    if (students.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Ushbu sinfda o'quvchilar mavjud emas", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                    } else {
                        val gradesByStudent = remember(grades) { grades.groupBy { it.studentId } }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            itemsIndexed(
                                items = students,
                                key = { _, student -> student.id }
                            ) { index, student ->
                                val studentGrades = remember(gradesByStudent, student.id) {
                                    gradesByStudent[student.id] ?: emptyList()
                                }
                                StudentRowWebStyle(
                                    index = index + 1,
                                    student = student,
                                    grades = studentGrades,
                                    daysSinceLastGrade = lastGradeDaysAgoMap[student.id],
                                    horizontalScrollState = horizontalScrollState,
                                    onCellChange = { gradeType, value ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.onCellChanged(student.id, gradeType, value)
                                    },
                                    onCommentClick = { localGradeId ->
                                        onOpenComment(
                                            localGradeId,
                                            "${student.lastName} ${student.firstName}",
                                            curClassName ?: "Sinf",
                                            curSubjectName ?: "Fan",
                                            selectedDate
                                        )
                                    }
                                )
                                HorizontalDivider(color = neutralBorder)
                            }
                        }
                    }

                    // Table Footer Bar
                    Surface(
                        color = slateBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val enteredGradesCount = grades.count { (it.gradeType == "MASTERY" || it.gradeType == "DAILY") && it.value.isNotBlank() }
                            Text(
                                text = "O'QUVCHILAR: ${students.size} TA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "KIRITILGAN BAHOLAR: $enteredGradesCount TA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Sticky Floating "Saqlash" button in bottom right corner (ergonomic thumb reach)
                ExtendedFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.finalizeAndApprove()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 48.dp),
                    containerColor = crimsonColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    icon = {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Saqlash",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    text = {
                        Text(
                            text = if (isSaving) "Saqlanmoqda..." else "Saqlash",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                )
            }
        }

        val activeConflict = conflicts.firstOrNull()
        if (activeConflict != null) {
            uz.farzandim.teacher.ui.components.GradeConflictDialog(
                conflict = activeConflict,
                isResolving = isResolvingConflict,
                onKeepLocal = { viewModel.resolveConflictKeepLocal(activeConflict) },
                onAcceptRemote = { viewModel.resolveConflictAcceptRemote(activeConflict) }
            )
        }
    }
}

@Composable
fun StudentRowWebStyle(
    index: Int,
    student: StudentEntity,
    grades: List<GradeEntity>,
    daysSinceLastGrade: Int?,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    onCellChange: (gradeType: String, value: String) -> Unit,
    onCommentClick: (localGradeId: String) -> Unit
) {
    val attendanceGrade = remember(grades) { grades.find { it.gradeType == "ATTENDANCE" } }
    val dailyGrade = remember(grades) { grades.find { it.gradeType == "MASTERY" || it.gradeType == "DAILY" } }
    val behaviorGrade = remember(grades) { grades.find { it.gradeType == "BEHAVIOR" } }

    val isLocked = dailyGrade?.status == "approved"
    val isParentApproved = dailyGrade?.approvedByParent == true
    val neutralBorder = Color(0xFFE2E8F0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
            .background(Color.White)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // № (01, 02...)
        Text(
            text = String.format("%02d", index),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A),
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
        )

        // Student Name + days-ago badge
        Column(
            modifier = Modifier
                .width(170.dp)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = "${student.lastName} ${student.firstName}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (daysSinceLastGrade != null) {
                val (badgeColor, textColor) = when {
                    daysSinceLastGrade <= 7  -> Color(0xFFFEF3C7) to Color(0xFF92400E)  // amber — yangi
                    daysSinceLastGrade <= 14 -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)  // ko'k — o'rta
                    else                     -> Color(0xFFF1F5F9) to Color(0xFF64748B)  // kulrang — eski
                }
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(3.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, textColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "$daysSinceLastGrade kun oldin",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            } else {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(3.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                ) {
                    Text(
                        text = "baho yo'q",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Davomat Cell (+ v, - v, k v)
        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.Center
        ) {
            val attVal = attendanceGrade?.value ?: "+"
            var showAttMenu by remember { mutableStateOf(false) }

            val (bg, textColor, border) = when (attVal) {
                "+" -> Triple(Color(0xFFECFDF5), Color(0xFF059669), Color(0xFFA7F3D0))
                "-" -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFFFECACA))
                else -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFFFDE68A))
            }

            Surface(
                shape = RoundedCornerShape(0.dp),
                color = bg,
                border = androidx.compose.foundation.BorderStroke(1.dp, border),
                modifier = Modifier
                    .size(44.dp, 32.dp)
                    .clickable(enabled = !isLocked) { showAttMenu = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = attVal, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            DropdownMenu(expanded = showAttMenu, onDismissRequest = { showAttMenu = false }) {
                DropdownMenuItem(text = { Text("+ (Kelgan)", color = Color(0xFF059669), fontWeight = FontWeight.Bold) }, onClick = { onCellChange("ATTENDANCE", "+"); showAttMenu = false })
                DropdownMenuItem(text = { Text("- (Kelmagan)", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold) }, onClick = { onCellChange("ATTENDANCE", "-"); showAttMenu = false })
                DropdownMenuItem(text = { Text("k (Sababli)", color = Color(0xFFD97706), fontWeight = FontWeight.Bold) }, onClick = { onCellChange("ATTENDANCE", "k"); showAttMenu = false })
            }
        }

        // Xulq Cell (0 v, +1, -1...)
        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.Center
        ) {
            val behVal = behaviorGrade?.value ?: "0"
            var showBehMenu by remember { mutableStateOf(false) }

            Surface(
                shape = RoundedCornerShape(0.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, neutralBorder),
                modifier = Modifier
                    .size(44.dp, 32.dp)
                    .clickable(enabled = !isLocked) { showBehMenu = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = behVal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF334155)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            DropdownMenu(expanded = showBehMenu, onDismissRequest = { showBehMenu = false }) {
                listOf("+5", "+4", "+3", "+2", "+1", "0", "-1", "-2", "-3", "-4", "-5").forEach { v ->
                    DropdownMenuItem(
                        text = { Text(v, fontWeight = FontWeight.Bold) },
                        onClick = { onCellChange("BEHAVIOR", v); showBehMenu = false }
                    )
                }
            }
        }

        // O'zlashtirish (Baho) Cell
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.Center
        ) {
            val gradeVal = dailyGrade?.value ?: ""
            var showGradeMenu by remember { mutableStateOf(false) }

            val (cellBg, cellText, cellBorder) = when (gradeVal) {
                "5" -> Triple(Color(0xFFECFDF5), Color(0xFF059669), Color(0xFFA7F3D0))
                "4" -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), Color(0xFFBFDBFE))
                "3" -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFFFDE68A))
                "2" -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFFFECACA))
                else -> Triple(Color.White, Color(0xFF94A3B8), neutralBorder)
            }

            Surface(
                shape = RoundedCornerShape(0.dp),
                color = cellBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, cellBorder),
                modifier = Modifier
                    .size(44.dp, 32.dp)
                    .clickable(enabled = !isLocked) { showGradeMenu = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = gradeVal.ifEmpty { "—" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = cellText
                    )

                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Tasdiqlangan",
                            tint = Color(0xFF475569),
                            modifier = Modifier
                                .size(9.dp)
                                .align(Alignment.TopEnd)
                                .padding(1.dp)
                        )
                    }
                    if (isParentApproved && !isLocked) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Ota-ona ko'rdi",
                            tint = Color(0xFF059669),
                            modifier = Modifier
                                .size(9.dp)
                                .align(Alignment.TopEnd)
                                .padding(1.dp)
                        )
                    }
                }
            }

            DropdownMenu(expanded = showGradeMenu, onDismissRequest = { showGradeMenu = false }) {
                DropdownMenuItem(text = { Text("5 — A'lo", color = Color(0xFF059669), fontWeight = FontWeight.Bold) }, onClick = { onCellChange("MASTERY", "5"); showGradeMenu = false })
                DropdownMenuItem(text = { Text("4 — Yaxshi", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold) }, onClick = { onCellChange("MASTERY", "4"); showGradeMenu = false })
                DropdownMenuItem(text = { Text("3 — Qoniqarli", color = Color(0xFFD97706), fontWeight = FontWeight.Bold) }, onClick = { onCellChange("MASTERY", "3"); showGradeMenu = false })
                DropdownMenuItem(text = { Text("2 — Qoniqarsiz", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold) }, onClick = { onCellChange("MASTERY", "2"); showGradeMenu = false })
                DropdownMenuItem(text = { Text("O'chirish (—)", color = Color(0xFF94A3B8)) }, onClick = { onCellChange("MASTERY", ""); showGradeMenu = false })
            }
        }

        // Izoh Button (💬)
        Box(
            modifier = Modifier.width(50.dp),
            contentAlignment = Alignment.Center
        ) {
            val targetGrade = dailyGrade ?: attendanceGrade
            val hasGrade = targetGrade != null && targetGrade.value.isNotBlank()

            IconButton(
                onClick = { targetGrade?.let { onCommentClick(it.localId) } },
                enabled = hasGrade,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Izoh",
                    tint = if (hasGrade) Color(0xFF1E2B42) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
