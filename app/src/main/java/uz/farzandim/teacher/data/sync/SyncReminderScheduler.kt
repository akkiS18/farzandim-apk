package uz.farzandim.teacher.data.sync

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import uz.farzandim.teacher.util.AppLogger
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object SyncReminderScheduler {

    const val CHANNEL_ID = "sync_reminder_channel"
    const val NOTIFICATION_ID = 6001
    private const val WORK_NAME = "DAILY_SYNC_REMINDER_6AM"

    /**
     * Schedules a one-time work request targeting precisely 06:00 AM Uzbekistan Time (Asia/Tashkent, UTC+5).
     * After firing, the worker reschedules itself for the next morning.
     */
    fun scheduleDailyReminder(context: Context) {
        val delayMs = calculateDelayUntilNext6AM()
        AppLogger.i("SyncReminderScheduler", "Next 06:00 AM check scheduled in ${delayMs / (1000 * 60)} minutes")

        val workRequest = OneTimeWorkRequestBuilder<DailySyncReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Calculates milliseconds from current instant until the next 06:00:00 AM in Uzbekistan Timezone (Asia/Tashkent).
     */
    fun calculateDelayUntilNext6AM(): Long {
        val uzbZone = TimeZone.getTimeZone("Asia/Tashkent")
        val now = Calendar.getInstance(uzbZone)

        val target = Calendar.getInstance(uzbZone).apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If it's already past 06:00 AM in Tashkent today, schedule for 06:00 AM tomorrow
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return target.timeInMillis - now.timeInMillis
    }
}
