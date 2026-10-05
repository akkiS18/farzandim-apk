package uz.farzandim.parent.data.sync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import uz.farzandim.parent.data.local.dao.SyncQueueDao
import uz.farzandim.parent.data.model.CreateGradeCommentRequest
import uz.farzandim.parent.data.model.CreateMenuCommentRequest
import uz.farzandim.parent.data.model.VotePollRequest
import uz.farzandim.parent.data.remote.ApiService
import java.util.concurrent.TimeUnit

class SyncManager(
    private val context: Context,
    private val apiService: ApiService,
    private val syncQueueDao: SyncQueueDao
) {
    suspend fun flushSyncQueue(): Int = withContext(Dispatchers.IO) {
        try {
            val pending = syncQueueDao.getPendingItems()
            var syncedCount = 0

            for (item in pending) {
                try {
                    val json = JSONObject(item.payloadJson)
                    val success = when (item.actionType) {
                        "PARENT_APPROVE_GRADE" -> {
                            val gradeId = json.getInt("grade_id")
                            apiService.parentApproveGrade(gradeId).isSuccessful
                        }
                        "VOTE_POLL" -> {
                            val annId = json.getInt("announcement_id")
                            val optionId = json.getInt("option_id")
                            apiService.votePoll(annId, VotePollRequest(optionId)).isSuccessful
                        }
                        "CREATE_GRADE_COMMENT" -> {
                            val gradeId = json.getInt("grade_id")
                            val content = json.getString("content")
                            apiService.createGradeComment(gradeId, CreateGradeCommentRequest(content)).isSuccessful
                        }
                        "CREATE_MENU_COMMENT" -> {
                            val menuDate = json.getString("menu_date")
                            val content = json.getString("content")
                            apiService.createMenuComment(CreateMenuCommentRequest(menuDate, content)).isSuccessful
                        }
                        else -> false
                    }

                    if (success) {
                        syncQueueDao.dequeue(item.id)
                        syncedCount++
                    } else {
                        syncQueueDao.incrementRetry(item.id)
                    }
                } catch (e: Exception) {
                    syncQueueDao.incrementRetry(item.id)
                }
            }
            syncedCount
        } catch (t: Throwable) {
            android.util.Log.e("SyncManager", "Error flushing sync queue", t)
            0
        }
    }

    fun schedulePeriodicSync() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "FarzandimParentSyncWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } catch (t: Throwable) {
            android.util.Log.e("SyncManager", "WorkManager schedule failed safely", t)
        }
    }
}
