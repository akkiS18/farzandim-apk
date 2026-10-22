package uz.farzandim.teacher.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uz.farzandim.teacher.MainActivity
import uz.farzandim.teacher.R
import uz.farzandim.teacher.data.local.dao.CommentDao
import uz.farzandim.teacher.data.local.dao.GradeDao
import uz.farzandim.teacher.util.AppLogger
import uz.farzandim.teacher.util.BadgeManager

class DailySyncReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val gradeDao: GradeDao by inject()
    private val commentDao: CommentDao by inject()

    override suspend fun doWork(): Result {
        return try {
            AppLogger.i("DailySyncReminder", "Running 06:00 AM UZT sync reminder check...")

            val pendingGrades = gradeDao.getPendingGrades().size
            val pendingComments = commentDao.getPendingComments().size
            val totalPending = pendingGrades + pendingComments

            // Update external launcher badge
            BadgeManager.updateBadge(applicationContext, totalPending)

            if (totalPending > 0) {
                AppLogger.w("DailySyncReminder", "Pending data found: $totalPending unsynced items. Firing notification.")
                showReminderNotification(totalPending)
            } else {
                AppLogger.i("DailySyncReminder", "No unsynced items found. Skipping notification.")
            }

            // Schedule next morning 06:00 AM check
            SyncReminderScheduler.scheduleDailyReminder(applicationContext)

            Result.success()
        } catch (e: Exception) {
            AppLogger.e("DailySyncReminder", "Error executing daily reminder: ${e.message}")
            Result.failure()
        }
    }

    private fun showReminderNotification(count: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = SyncReminderScheduler.CHANNEL_ID

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sinxronizatsiya eslatmasi",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sinxronlanmagan ma'lumotlar mavjud bo'lganda ertalabki eslatmalar"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ Sinxronlanmagan ma'lumotlar mavjud")
            .setContentText("Sizda $count ta yuborilmagan baho va yozuvlar qolgan. Iltimos, internetga ulanib ularni yuboring!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Sizda $count ta sinxronlanmagan baho va ma'lumotlar mavjud. Oflayn kiritilgan baholar serverga yetib borishi uchun iltimos internetga ulanib dasturni oching."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setNumber(count)
            .build()

        notificationManager.notify(SyncReminderScheduler.NOTIFICATION_ID, notification)
    }
}
