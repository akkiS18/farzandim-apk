package uz.farzandim.parent.data.sync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import uz.farzandim.parent.data.local.dao.ParentGradeDao
import uz.farzandim.parent.data.local.dao.SyncQueueDao
import uz.farzandim.parent.data.model.CreateGradeCommentRequest
import uz.farzandim.parent.data.model.CreateMenuCommentRequest
import uz.farzandim.parent.data.model.VotePollRequest
import uz.farzandim.parent.data.remote.ApiService
import java.util.concurrent.TimeUnit

class SyncManager(
    private val context: Context,
    private val apiService: ApiService,
    private val syncQueueDao: SyncQueueDao,
    private val gradeDao: ParentGradeDao? = null
) {
    suspend fun flushSyncQueue(): Int = flushInternal().first

    suspend fun flushSyncQueueWithStatus(): Boolean = flushInternal().second

    private suspend fun flushInternal(): Pair<Int, Boolean> = withContext(Dispatchers.IO) {
        try {
            val pending = syncQueueDao.getPendingItems()
            var syncedCount = 0
            var hasTransientFailure = false

            for (item in pending) {
                try {
                    val json = JSONObject(item.payloadJson)
                    val response = when (item.actionType) {
                        "PARENT_APPROVE_GRADE" -> {
                            val gradeId = json.getInt("grade_id")
                            apiService.parentApproveGrade(gradeId)
                        }
                        "VOTE_POLL" -> {
                            val annId = json.getInt("announcement_id")
                            val optionId = json.getInt("option_id")
                            apiService.votePoll(annId, VotePollRequest(optionId))
                        }
                        "CREATE_GRADE_COMMENT" -> {
                            val gradeId = json.getInt("grade_id")
                            val content = json.getString("content")
                            apiService.createGradeComment(gradeId, CreateGradeCommentRequest(content))
                        }
                        "CREATE_MENU_COMMENT" -> {
                            val menuDate = json.getString("menu_date")
                            val content = json.getString("content")
                            apiService.createMenuComment(CreateMenuCommentRequest(menuDate, content))
                        }
                        else -> null
                    }

                    if (response == null) {
                        syncQueueDao.dequeue(item.id)
                    } else if (response.isSuccessful) {
                        syncQueueDao.dequeue(item.id)
                        syncedCount++
                    } else if (response.code() in listOf(400, 403, 404, 409, 422)) {
                        // Permanent client/authorization rejection: revert optimistic UI and remove from queue
                        if (item.actionType == "PARENT_APPROVE_GRADE") {
                            val gradeId = json.optInt("grade_id", -1)
                            if (gradeId > 0) {
                                gradeDao?.markUnapprovedLocally(gradeId)
                            }
                        }
                        syncQueueDao.dequeue(item.id)
                    } else {
                        // Transient 5xx / rate limit error
                        syncQueueDao.incrementRetry(item.id)
                        hasTransientFailure = true
                    }
                } catch (e: Exception) {
                    syncQueueDao.incrementRetry(item.id)
                    hasTransientFailure = true
                }
            }
            Pair(syncedCount, !hasTransientFailure)
        } catch (t: Throwable) {
            android.util.Log.e("SyncManager", "Error flushing sync queue", t)
            Pair(0, false)
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
