package uz.farzandim.teacher.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.farzandim.teacher.data.local.dao.CommentDao
import uz.farzandim.teacher.data.local.dao.GradeDao
import java.util.concurrent.TimeUnit

sealed class SyncState {
    data class Synced(val message: String = "Barcha ma'lumotlar sinxronlangan", val syncedAt: Long = System.currentTimeMillis()) : SyncState()
    data class Pending(val count: Int, val message: String) : SyncState()
    object Syncing : SyncState()
    object Offline : SyncState()
}

class SyncManager(
    private val context: Context,
    private val gradeDao: GradeDao,
    private val commentDao: CommentDao,
    private val sessionManager: uz.farzandim.teacher.data.local.SessionManager
) {
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Synced())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // Timestamp (epoch ms) of the last successful sync with the server
    private val _lastSyncedAt = MutableStateFlow<Long?>(null)
    val lastSyncedAt: StateFlow<Long?> = _lastSyncedAt.asStateFlow()

    init {
        scope.launch {
            val stored = sessionManager.getLastSyncedAt()
            if (stored != null && stored > 0L) {
                _lastSyncedAt.value = stored
                if (_syncState.value is SyncState.Synced) {
                    _syncState.value = SyncState.Synced(syncedAt = stored)
                }
            }
        }
        registerNetworkCallback()
        setupPeriodicSync()
        updateSyncStatus()
    }

    private fun registerNetworkCallback() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        _isOnline.value = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                triggerImmediateSync()
                updateSyncStatus()
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
                updateSyncStatus()
            }
        })
    }

    fun updateSyncStatus() {
        scope.launch {
            val pendingGrades = gradeDao.getPendingGrades().size
            val pendingComments = commentDao.getPendingComments().size
            val totalPending = pendingGrades + pendingComments

            // Update external launcher badge (ShortcutBadger)
            uz.farzandim.teacher.util.BadgeManager.updateBadge(context, totalPending)

            if (!_isOnline.value) {
                if (totalPending > 0) {
                    _syncState.value = SyncState.Pending(totalPending, "Oflayn rejim — $totalPending ta o'zgarish kutilmoqda")
                } else {
                    _syncState.value = SyncState.Offline
                }
            } else {
                if (totalPending > 0) {
                    _syncState.value = SyncState.Pending(totalPending, "$totalPending ta o'zgarish sinxronlanmoqda...")
                } else {
                    val now = System.currentTimeMillis()
                    _lastSyncedAt.value = now
                    sessionManager.saveLastSyncedAt(now)
                    _syncState.value = SyncState.Synced(syncedAt = now)
                }
            }
        }
    }

    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<GradeSyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "GRADE_SYNC_WORK_IMMEDIATE",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<GradeSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "GRADE_SYNC_WORK_PERIODIC",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }
}
