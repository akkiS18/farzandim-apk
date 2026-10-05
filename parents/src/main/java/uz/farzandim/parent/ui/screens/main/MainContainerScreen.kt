package uz.farzandim.parent.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel
import uz.farzandim.parent.R
import uz.farzandim.parent.ui.components.ParentBottomNavigationBar
import uz.farzandim.parent.ui.components.ParentBottomTab
import uz.farzandim.parent.ui.screens.ai.AiReportScreen
import uz.farzandim.parent.ui.screens.announcements.AnnouncementsScreen
import uz.farzandim.parent.ui.screens.balance.BalanceScreen
import uz.farzandim.parent.ui.screens.books.BooksScreen
import uz.farzandim.parent.ui.screens.clubs.ClubsScreen
import uz.farzandim.parent.ui.screens.comments.CommentsScreen
import uz.farzandim.parent.ui.screens.diary.DiaryScreen
import uz.farzandim.parent.ui.screens.dynamics.DynamicsScreen
import uz.farzandim.parent.ui.screens.home.DashboardScreen
import uz.farzandim.parent.ui.screens.menu.MenuScreen
import uz.farzandim.parent.ui.screens.settings.SettingsScreen
import uz.farzandim.parent.ui.theme.*
import uz.farzandim.parent.ui.viewmodel.ParentViewModel

enum class SubScreen(val titleRes: Int) {
    NONE(0),
    DYNAMICS(R.string.service_dynamics),
    BALANCE(R.string.service_balance),
    MENU(R.string.service_menu),
    ANNOUNCEMENTS(R.string.service_announcements),
    CLUBS(R.string.service_clubs),
    BOOKS(R.string.service_books),
    AI_REPORT(R.string.service_ai_report)
}

@Composable
fun MainContainerScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ParentViewModel = koinViewModel()
) {
    var currentTab by remember { mutableStateOf(ParentBottomTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }

    val unapprovedGradesCount by viewModel.unapprovedGradesCount.collectAsState()

    // Handle system back button when inside a sub-screen
    BackHandler(enabled = currentSubScreen != SubScreen.NONE) {
        currentSubScreen = SubScreen.NONE
    }

    // Handle system back button when on another tab (return to Home instead of exiting)
    BackHandler(enabled = currentSubScreen == SubScreen.NONE && currentTab != ParentBottomTab.HOME) {
        currentTab = ParentBottomTab.HOME
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        bottomBar = {
            if (currentSubScreen == SubScreen.NONE) {
                ParentBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                    },
                    unapprovedGradesCount = unapprovedGradesCount,
                    unreadCommentsCount = 0
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentSubScreen != SubScreen.NONE) {
                when (currentSubScreen) {
                    SubScreen.DYNAMICS -> {
                        DynamicsScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    SubScreen.BALANCE -> {
                        BalanceScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    SubScreen.MENU -> {
                        MenuScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    SubScreen.ANNOUNCEMENTS -> {
                        AnnouncementsScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    SubScreen.CLUBS -> {
                        ClubsScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    SubScreen.BOOKS -> {
                        BooksScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    SubScreen.AI_REPORT -> {
                        AiReportScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    SubScreen.NONE -> {}
                }
            } else {
                when (currentTab) {
                    ParentBottomTab.HOME -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onOpenDiary = { currentTab = ParentBottomTab.DIARY },
                            onOpenDynamics = { currentSubScreen = SubScreen.DYNAMICS },
                            onOpenBalance = { currentSubScreen = SubScreen.BALANCE },
                            onOpenMenu = { currentSubScreen = SubScreen.MENU },
                            onOpenAnnouncements = { currentSubScreen = SubScreen.ANNOUNCEMENTS },
                            onOpenClubs = { currentSubScreen = SubScreen.CLUBS },
                            onOpenBooks = { currentSubScreen = SubScreen.BOOKS },
                            onOpenAiReport = { currentSubScreen = SubScreen.AI_REPORT }
                        )
                    }
                    ParentBottomTab.DIARY -> {
                        DiaryScreen(viewModel = viewModel)
                    }
                    ParentBottomTab.COMMENTS -> {
                        CommentsScreen(viewModel = viewModel)
                    }
                    ParentBottomTab.SETTINGS -> {
                        SettingsScreen(
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}
