package uz.farzandim.teacher

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import uz.farzandim.teacher.di.appModules

class FarzandimApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@FarzandimApp)
            modules(appModules)
        }

        createNotificationChannel()
        uz.farzandim.teacher.data.sync.SyncReminderScheduler.scheduleDailyReminder(this)
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                uz.farzandim.teacher.data.sync.SyncReminderScheduler.CHANNEL_ID,
                "Sinxronizatsiya eslatmasi",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sinxronlanmagan ma'lumotlar mavjud bo'lganda ertalabki eslatmalar"
                setShowBadge(true)
            }
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
