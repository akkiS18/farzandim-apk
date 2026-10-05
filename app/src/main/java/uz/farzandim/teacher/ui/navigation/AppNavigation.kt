package uz.farzandim.teacher.ui.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel
import uz.farzandim.teacher.ui.screens.auth.LoginScreen
import uz.farzandim.teacher.ui.screens.comments.CommentScreen
import uz.farzandim.teacher.ui.screens.main.MainScreen
import uz.farzandim.teacher.ui.screens.pin.PinLockScreen
import uz.farzandim.teacher.ui.screens.pin.PinSetupScreen
import uz.farzandim.teacher.ui.viewmodel.AuthUiState
import uz.farzandim.teacher.ui.viewmodel.AuthViewModel
import uz.farzandim.teacher.ui.viewmodel.CommentViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = koinViewModel()
) {
    val sessionManager = authViewModel.sessionManager
    val isSessionChecked by authViewModel.isSessionChecked.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    val isPinSet by sessionManager.isPinSetFlow.collectAsState(initial = null)
    var isUnlocked by rememberSaveable { mutableStateOf(false) }

    if (!isSessionChecked || isPinSet == null) {
        // Checking offline session and PIN state
        return
    }

    if (uiState !is AuthUiState.Authenticated) {
        LoginScreen(
            viewModel = authViewModel,
            onLoginSuccess = {
                isUnlocked = true
            }
        )
    } else if (isPinSet == false) {
        PinSetupScreen(
            sessionManager = sessionManager,
            onSetupComplete = {
                isUnlocked = true
            }
        )
    } else if (!isUnlocked) {
        PinLockScreen(
            sessionManager = sessionManager,
            authViewModel = authViewModel,
            onUnlockSuccess = {
                isUnlocked = true
            },
            onLogout = {
                isUnlocked = false
            }
        )
    } else {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route
        ) {
            composable(Screen.Main.route) {
                MainScreen(
                    authViewModel = authViewModel,
                    onOpenComment = { gradeId, studentName, className, subjectName, date ->
                        val route = Screen.Comment.createRoute(gradeId, studentName, className, subjectName, date)
                        navController.navigate(route)
                    },
                    onLogout = {
                        isUnlocked = false
                    }
                )
            }

            composable(
                route = Screen.Comment.route,
                arguments = listOf(
                    navArgument("gradeId") { type = NavType.StringType },
                    navArgument("studentName") { type = NavType.StringType },
                    navArgument("className") { type = NavType.StringType },
                    navArgument("subjectName") { type = NavType.StringType },
                    navArgument("date") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val gradeId = backStackEntry.arguments?.getString("gradeId") ?: ""
                val studentName = Uri.decode(backStackEntry.arguments?.getString("studentName") ?: "")
                val className = Uri.decode(backStackEntry.arguments?.getString("className") ?: "")
                val subjectName = Uri.decode(backStackEntry.arguments?.getString("subjectName") ?: "")
                val date = backStackEntry.arguments?.getString("date") ?: ""

                val commentViewModel: CommentViewModel = koinViewModel()

                CommentScreen(
                    gradeId = gradeId,
                    studentName = studentName,
                    className = className,
                    subjectName = subjectName,
                    date = date,
                    viewModel = commentViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
