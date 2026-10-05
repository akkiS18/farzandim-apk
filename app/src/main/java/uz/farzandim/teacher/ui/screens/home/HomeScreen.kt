package uz.farzandim.teacher.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.farzandim.teacher.data.local.entity.TodayLessonEntity
import uz.farzandim.teacher.ui.theme.*
import uz.farzandim.teacher.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

fun formatUzbekDate(dateStr: String): String {
    return try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr) ?: return dateStr
        val cal = Calendar.getInstance().apply { time = d }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthNames = listOf("Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun", "Iyul", "Avgust", "Sentabr", "Oktabr", "Noyabr", "Dekabr")
        val weekDays = listOf("Yakshanba", "Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba")
        val month = monthNames[cal.get(Calendar.MONTH)]
        val weekDay = weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
        "$day-$month, $weekDay"
    } catch (_: Exception) {
        dateStr
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLessonClick: (classId: Int, subjectId: Int, lessonNumber: Int, date: String) -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayLessons by viewModel.todayLessons.collectAsState()
    val managedClasses by viewModel.managedClasses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scheduleMeta by viewModel.scheduleMeta.collectAsState()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val formattedDate = remember(selectedDate) { formatUzbekDate(selectedDate) }

    val pendingCount = remember(todayLessons) { todayLessons.count { !it.isFullyMarked } }
    val completedCount = remember(todayLessons) { todayLessons.count { it.isFullyMarked } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // 1. Date Navigation Ribbon
        item {
            Surface(
                shape = RoundedCornerShape(0.dp),
                color = CardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            try {
                                val cal = Calendar.getInstance().apply {
                                    time = dateFormat.parse(selectedDate) ?: Date()
                                    add(Calendar.DAY_OF_YEAR, -1)
                                }
                                viewModel.selectDate(dateFormat.format(cal.time))
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Oldingi kun", tint = OxfordSlate)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedDate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OxfordSlate
                        )
                        if (selectedDate != todayStr) {
                            Text(
                                text = "Bugungi kunga qaytish ↩",
                                color = HarvardCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clickable { viewModel.selectDate(todayStr) }
                            )
                        } else {
                            Text(
                                text = "Bugungi dars jadvali",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            try {
                                val cal = Calendar.getInstance().apply {
                                    time = dateFormat.parse(selectedDate) ?: Date()
                                    add(Calendar.DAY_OF_YEAR, 1)
                                }
                                viewModel.selectDate(dateFormat.format(cal.time))
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Keyingi kun", tint = OxfordSlate)
                    }
                }
            }
        }

        // 2. Summary Counters (Emerald & Amber)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = EmeraldBadgeBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBadgeBorder),
                    modifier = Modifier.weight(1f),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(EmeraldBadge.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldBadge, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(text = "$completedCount ta", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EmeraldBadge)
                            Text(text = "Bajarildi", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = AmberBadgeBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberBadgeBorder),
                    modifier = Modifier.weight(1f),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AmberBadge.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = AmberBadge, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(text = "$pendingCount ta", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AmberBadge)
                            Text(text = "Kutilmoqda", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // 3. Section Title: Bugungi Darslar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BUGUNGI DARSLAR (${todayLessons.size} TA)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OxfordSlate,
                    letterSpacing = 0.5.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { viewModel.refresh() }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(14.dp), color = HarvardCrimson)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Yangilash", tint = TextMuted, modifier = Modifier.size(14.dp))
                        Text(text = "Yangilash", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        // 4. Lessons List
        if (todayLessons.isEmpty()) {
            item {
                val isWeekend = scheduleMeta?.isWeekend == true
                val isHoliday = scheduleMeta?.isHoliday == true
                val holidayName = scheduleMeta?.holidayName ?: "Maktab Ta'tili"

                val weekDayName = try {
                    val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)
                    val cal = Calendar.getInstance().apply { time = d ?: Date() }
                    val weekDays = listOf("Yakshanba", "Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba")
                    weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
                } catch (_: Exception) { "" }

                Surface(
                    shape = RoundedCornerShape(0.dp),
                    color = CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Weekend,
                            contentDescription = null,
                            tint = HarvardCrimson,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = when {
                                isHoliday -> "$holidayName — Dam Olish Kuni"
                                weekDayName.isNotBlank() -> "$weekDayName — Dam Olish Kuni"
                                else -> "Dam Olish Kuni"
                            },
                            style = Typography.titleMedium,
                            color = OxfordSlate
                        )
                        Text(
                            text = when {
                                isHoliday -> "Admin tomonidan ushbu sana dam olish kuni deb belgilangan."
                                isWeekend -> "Bugun dam olish kuni. Darslar va baholash o'tkazilmaydi."
                                else -> "Dars jadvalida dars belgilanmagan (Dam olish kuni). Darslar va baholash o'tkazilmaydi."
                            },
                            style = Typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (selectedDate != todayStr) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { viewModel.selectDate(todayStr) },
                                shape = RoundedCornerShape(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OxfordSlate)
                            ) {
                                Text("Bugungi kunga qaytish ↩", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            items(todayLessons, key = { it.id }) { lesson ->
                LessonCard(lesson = lesson, onClick = {
                    onLessonClick(lesson.classId, lesson.subjectId, lesson.lessonNumber, lesson.date)
                })
            }
        }

        // 5. Managed Classes Section
        if (managedClasses.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "BOSHQARAYOTGAN SINFLARIM (${managedClasses.size} TA)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OxfordSlate,
                    letterSpacing = 0.5.sp
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(managedClasses, key = { it.id }) { cls ->
                        Surface(
                            shape = RoundedCornerShape(0.dp),
                            color = CardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier
                                .width(150.dp)
                                .clickable {
                                    cls.defaultSubjectId?.let { subjId ->
                                        onLessonClick(cls.id, subjId, 1, selectedDate)
                                    }
                                },
                            shadowElevation = 0.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "SINFI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    if (cls.isMainTeacher) {
                                        Surface(
                                            color = AmberBadgeBg,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberBadgeBorder),
                                            shape = RoundedCornerShape(0.dp)
                                        ) {
                                            Text(
                                                text = "★ Rahbar",
                                                color = AmberBadge,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = cls.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = HarvardCrimson
                                )
                                Text(
                                    text = cls.defaultSubjectName ?: "Fan tanlanmagan",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCard(
    lesson: TodayLessonEntity,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(0.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Lesson # and Class
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${lesson.lessonNumber}-SOAT DARSI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    color = OxfordSlateLight,
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        text = "${lesson.className} SINFI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OxfordSlate,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Subject Headline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = lesson.subjectName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = HarvardCrimson
                    )
                    if (!lesson.time.isNullOrEmpty()) {
                        Text(
                            text = lesson.time,
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }

            HorizontalDivider(color = BorderLight.copy(alpha = 0.6f))

            // Footer: Marking Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (lesson.isFullyMarked) {
                    Surface(
                        color = EmeraldBadgeBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldBadgeBorder),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldBadge, modifier = Modifier.size(12.dp))
                            Text(
                                text = "Bajarildi (${lesson.markedStudentsCount}/${lesson.totalStudentsCount})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldBadge
                            )
                        }
                    }
                } else if (lesson.isMarked) {
                    Surface(
                        color = AmberBadgeBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberBadgeBorder),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = AmberBadge, modifier = Modifier.size(12.dp))
                            Text(
                                text = "Qisman (${lesson.markedStudentsCount}/${lesson.totalStudentsCount})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberBadge
                            )
                        }
                    }
                } else {
                    Surface(
                        color = RoseBadgeBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseBadgeBorder),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(RoseBadge))
                            Text(
                                text = "Kiritilmagan (${lesson.markedStudentsCount}/${lesson.totalStudentsCount})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoseBadge
                            )
                        }
                    }
                }

                Text(
                    text = "Jurnalga o'tish →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OxfordSlate
                )
            }
        }
    }
}
