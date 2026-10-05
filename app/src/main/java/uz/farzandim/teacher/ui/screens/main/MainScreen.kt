package uz.farzandim.teacher.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import uz.farzandim.teacher.ui.components.HeaderWithProfile
import uz.farzandim.teacher.ui.screens.home.HomeScreen
import uz.farzandim.teacher.ui.screens.journal.JournalScreen
import uz.farzandim.teacher.ui.theme.BorderLight
import uz.farzandim.teacher.ui.theme.HarvardCrimson
import uz.farzandim.teacher.ui.theme.OxfordSlate
import uz.farzandim.teacher.ui.theme.TextMuted
import uz.farzandim.teacher.ui.viewmodel.AuthViewModel
import uz.farzandim.teacher.ui.viewmodel.HomeViewModel
import uz.farzandim.teacher.ui.viewmodel.JournalViewModel

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
    journalViewModel: JournalViewModel = koinViewModel(),
    onOpenComment: (gradeId: String, studentName: String, className: String, subjectName: String, date: String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Journal

    val teacherName by authViewModel.userFullName.collectAsState()
    val schoolName by authViewModel.currentSchoolName.collectAsState()
    val currentBaseUrl by authViewModel.baseUrl.collectAsState()
    val syncState by journalViewModel.syncState.collectAsState()
    val lastSyncedAt by journalViewModel.lastSyncedAt.collectAsState()

    val selectedClassId by journalViewModel.selectedClassId.collectAsState()
    val selectedSubjectId by journalViewModel.selectedSubjectId.collectAsState()

    // Hierarchical back press handling:
    // If inside Journal: Subject Matrix -> Class Lessons/Schedule -> Class List -> Home Screen
    val canHandleBack = selectedTab == 1 || selectedClassId != null || selectedSubjectId != null
    BackHandler(enabled = canHandleBack) {
        when {
            selectedSubjectId != null -> {
                journalViewModel.clearSubject()
            }
            selectedClassId != null -> {
                journalViewModel.clearClass()
            }
            selectedTab == 1 -> {
                selectedTab = 0
            }
        }
    }

    Scaffold(
        topBar = {
            HeaderWithProfile(
                teacherName = teacherName,
                schoolName = schoolName,
                syncState = syncState,
                lastSyncedAt = lastSyncedAt,
                currentBaseUrl = currentBaseUrl,
                onLogoutClick = {
                    authViewModel.logout(onLogout)
                }
            )
        },
        bottomBar = {
            Surface(
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                color = Color.White
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(72.dp)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Bosh sahifa",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Bosh sahifa",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HarvardCrimson,
                            selectedTextColor = HarvardCrimson,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color.Transparent
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Jurnal",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Jurnal",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HarvardCrimson,
                            selectedTextColor = HarvardCrimson,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onLessonClick = { classId, subjectId, lessonNumber, date ->
                            journalViewModel.selectLesson(classId, subjectId, lessonNumber, date)
                            selectedTab = 1 // Switch directly to Journal tab!
                        }
                    )
                }
                1 -> {
                    JournalScreen(
                        viewModel = journalViewModel,
                        onOpenComment = onOpenComment
                    )
                }
            }
        }
    }
}
