package uz.farzandim.teacher.data.sync

import android.content.Context
import androidx.work.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uz.farzandim.teacher.data.local.dao.CommentDao
import uz.farzandim.teacher.data.local.dao.GradeDao
import uz.farzandim.teacher.data.local.entity.GradeEntity
import uz.farzandim.teacher.data.model.BatchCreateGradesRequest
import uz.farzandim.teacher.data.model.ChangeGradeStatusRequest
import uz.farzandim.teacher.data.model.CreateCommentRequest
import uz.farzandim.teacher.data.model.CreateGradeRequest
import uz.farzandim.teacher.data.model.GradeResponse
import uz.farzandim.teacher.data.remote.ApiService

object GradeMatcher {
    fun normalizeGradeType(type: String?): String {
        return when (type?.uppercase()?.trim()) {
            "DAILY", null, "" -> "MASTERY"
            else -> type.uppercase().trim()
        }
    }

    fun normalizeDate(date: String?): String {
        if (date.isNullOrBlank()) return ""
        return date.substringBefore('T').take(10)
    }

    fun normalizeCategory(category: String?): String {
        return if (category.isNullOrBlank()) "DAILY" else category.uppercase().trim()
    }

    fun findMatch(
        remote: GradeResponse,
        candidates: List<GradeEntity>,
        alreadyMatchedLocalIds: MutableSet<String>
    ): GradeEntity? {
        val remoteDate = normalizeDate(remote.gradeDate)
        val remoteType = normalizeGradeType(remote.gradeType)
        val remoteCat = normalizeCategory(remote.gradeCategory)
        val remoteLesson = if (remote.lessonNumber != null && remote.lessonNumber > 0) remote.lessonNumber else 1

        val match = candidates.find { local ->
            local.localId !in alreadyMatchedLocalIds &&
                    local.studentId == remote.studentId &&
                    local.subjectId == remote.subjectId &&
                    local.lessonNumber == remoteLesson &&
                    normalizeDate(local.gradeDate) == remoteDate &&
                    normalizeCategory(local.gradeCategory) == remoteCat &&
                    normalizeGradeType(local.gradeType) == remoteType
        }
        if (match != null) {
            alreadyMatchedLocalIds.add(match.localId)
        }
        return match
    }
}

class GradeSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val apiService: ApiService by inject()
    private val gradeDao: GradeDao by inject()
    private val commentDao: CommentDao by inject()
    private val sessionManager: uz.farzandim.teacher.data.local.SessionManager by inject()

    override suspend fun doWork(): Result {
        return try {
            var hasTransientFailure = false

            // 1. Process offline tombstone deletes (PENDING_DELETE)
            val pendingDeletes = gradeDao.getPendingDeleteGrades()
            for (delGrade in pendingDeletes) {
                val sId = delGrade.serverId
                if (sId == null) {
                    gradeDao.deleteGradeByLocalId(delGrade.localId)
                } else {
                    try {
                        val delRes = apiService.deleteGrade(sId)
                        if (delRes.isSuccessful || delRes.code() == 404 || delRes.code() == 400 || delRes.code() == 403) {
                            gradeDao.deleteGradeByLocalId(delGrade.localId)
                        } else {
                            hasTransientFailure = true
                        }
                    } catch (_: Exception) {
                        hasTransientFailure = true
                    }
                }
            }

            // 2. Process pending creates and updates (PENDING_UPSERT)
            val pendingUpserts = gradeDao.getPendingUpsertGrades()
            if (pendingUpserts.isNotEmpty()) {
                val toApproveMap = mutableMapOf<String, Int>() // localId -> serverId
                val toCreate = pendingUpserts.filter { it.serverId == null }
                val toUpdate = pendingUpserts.filter { it.serverId != null }

                if (toCreate.isNotEmpty()) {
                    val requests = toCreate.map { g ->
                        CreateGradeRequest(
                            studentId = g.studentId,
                            subjectId = g.subjectId,
                            value = g.value,
                            gradeDate = g.gradeDate,
                            gradingSystemId = g.gradingSystemId,
                            gradeType = g.gradeType,
                            gradeCategory = g.gradeCategory,
                            lessonNumber = g.lessonNumber
                        )
                    }

                    val res = apiService.batchCreateGrades(BatchCreateGradesRequest(requests))
                    if (res.isSuccessful && res.body() != null) {
                        val createdGrades = res.body()!!
                        val matchedLocalIds = mutableSetOf<String>()

                        createdGrades.forEach { cg ->
                            val match = GradeMatcher.findMatch(cg, toCreate, matchedLocalIds)
                            if (match != null) {
                                commentDao.updateServerGradeIdForComments(match.localId, cg.id)
                                if (match.status == "approved") {
                                    gradeDao.updateServerIdOnly(match.localId, cg.id)
                                    toApproveMap[match.localId] = cg.id
                                } else {
                                    gradeDao.updateServerGradeId(match.localId, cg.id)
                                }
                            }
                        }

                        if (matchedLocalIds.size < toCreate.size) {
                            hasTransientFailure = true
                        }
                    } else if (res.code() == 403 || res.code() == 400) {
                        // Process grades individually to isolate invalid grades while saving valid ones
                        for (g in toCreate) {
                            val singleReq = CreateGradeRequest(
                                studentId = g.studentId,
                                subjectId = g.subjectId,
                                value = g.value,
                                gradeDate = g.gradeDate,
                                gradingSystemId = g.gradingSystemId,
                                gradeType = g.gradeType,
                                gradeCategory = g.gradeCategory,
                                lessonNumber = g.lessonNumber
                            )
                            try {
                                val singleRes = apiService.createGrade(singleReq)
                                if (singleRes.isSuccessful && singleRes.body() != null) {
                                    val sId = singleRes.body()!!.id
                                    commentDao.updateServerGradeIdForComments(g.localId, sId)
                                    if (g.status == "approved") {
                                        gradeDao.updateServerIdOnly(g.localId, sId)
                                        toApproveMap[g.localId] = sId
                                    } else {
                                        gradeDao.updateServerGradeId(g.localId, sId)
                                    }
                                } else if (singleRes.code() == 403 || singleRes.code() == 400) {
                                    uz.farzandim.teacher.util.AppLogger.w("GradeSync", "Marking grade REJECTED: localId=${g.localId}")
                                    gradeDao.markGradeAsRejected(g.localId)
                                } else {
                                    hasTransientFailure = true
                                }
                            } catch (_: Exception) {
                                hasTransientFailure = true
                            }
                        }
                    } else {
                        hasTransientFailure = true
                    }
                }

                // Process updates for grades that already exist on server
                for (g in toUpdate) {
                    val sId = g.serverId!!
                    val singleReq = CreateGradeRequest(
                        studentId = g.studentId,
                        subjectId = g.subjectId,
                        value = g.value,
                        gradeDate = g.gradeDate,
                        gradingSystemId = g.gradingSystemId,
                        gradeType = g.gradeType,
                        gradeCategory = g.gradeCategory,
                        lessonNumber = g.lessonNumber
                    )
                    try {
                        val upRes = apiService.updateGrade(sId, singleReq)
                        if (upRes.isSuccessful) {
                            if (g.status == "approved") {
                                toApproveMap[g.localId] = sId
                            } else {
                                gradeDao.markGradesAsSynced(listOf(g.localId))
                            }
                        } else if (upRes.code() == 403 || upRes.code() == 400) {
                            // If already approved on server or forbidden edit, check if it just needs approval
                            if (g.status == "approved") {
                                toApproveMap[g.localId] = sId
                            } else {
                                gradeDao.markGradeAsRejected(g.localId)
                            }
                        } else {
                            hasTransientFailure = true
                        }
                    } catch (_: Exception) {
                        hasTransientFailure = true
                    }
                }

                // Approve any grades marked approved locally
                if (toApproveMap.isNotEmpty()) {
                    val approveRes = apiService.changeGradeStatus(
                        ChangeGradeStatusRequest(toApproveMap.values.distinct(), "approved")
                    )
                    if (approveRes.isSuccessful) {
                        gradeDao.markGradesAsSynced(toApproveMap.keys.toList())
                    } else {
                        hasTransientFailure = true
                    }
                }
            }

            // 3. Sync chained comments
            val pendingComments = commentDao.getPendingComments()
            for (c in pendingComments) {
                val sId = c.serverGradeId ?: run {
                    val g = gradeDao.getGradesByLocalIds(listOf(c.localGradeId)).firstOrNull()
                    g?.serverId
                }

                if (sId != null) {
                    val cRes = apiService.createGradeComment(sId, CreateCommentRequest(c.content))
                    if (cRes.isSuccessful && cRes.body() != null) {
                        commentDao.updateServerCommentId(c.localId, cRes.body()!!.id, sId)
                    } else if (cRes.code() >= 500) {
                        hasTransientFailure = true
                    }
                }
            }

            if (hasTransientFailure) {
                Result.retry()
            } else {
                sessionManager.saveLastSyncedAt(System.currentTimeMillis())
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
