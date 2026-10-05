package uz.farzandim.parent.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val syncManager: SyncManager by inject()

    override suspend fun doWork(): Result {
        return try {
            syncManager.flushSyncQueue()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
