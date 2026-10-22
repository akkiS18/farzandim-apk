package uz.farzandim.teacher.util

import android.content.Context
import me.leolin.shortcutbadger.ShortcutBadger

object BadgeManager {

    /**
     * Updates the external app launcher icon badge count.
     * Uses ShortcutBadger to support Samsung, Xiaomi, Huawei, Vivo, Oppo, Nova, etc.
     */
    fun updateBadge(context: Context, pendingCount: Int) {
        try {
            if (pendingCount > 0) {
                ShortcutBadger.applyCount(context.applicationContext, pendingCount)
                AppLogger.i("BadgeManager", "Launcher badge updated: $pendingCount")
            } else {
                clearBadge(context)
            }
        } catch (e: Exception) {
            AppLogger.w("BadgeManager", "Failed to update launcher badge: ${e.message}")
        }
    }

    /**
     * Clears the external launcher icon badge.
     */
    fun clearBadge(context: Context) {
        try {
            ShortcutBadger.removeCount(context.applicationContext)
            AppLogger.i("BadgeManager", "Launcher badge cleared")
        } catch (e: Exception) {
            AppLogger.w("BadgeManager", "Failed to clear launcher badge: ${e.message}")
        }
    }
}
