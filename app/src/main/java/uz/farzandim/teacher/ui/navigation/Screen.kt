package uz.farzandim.teacher.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object PinSetup : Screen("pin_setup")
    object PinLock : Screen("pin_lock")
    object Comment : Screen("comment/{gradeId}/{studentName}/{className}/{subjectName}/{date}") {
        fun createRoute(
            gradeId: String,
            studentName: String,
            className: String,
            subjectName: String,
            date: String
        ): String {
            val encStudent = Uri.encode(studentName)
            val encClass = Uri.encode(className)
            val encSubject = Uri.encode(subjectName)
            return "comment/$gradeId/$encStudent/$encClass/$encSubject/$date"
        }
    }
}
