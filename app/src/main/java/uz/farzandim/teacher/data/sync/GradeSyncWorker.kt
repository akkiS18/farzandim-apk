package uz.farzandim.teacher.data.sync

import android.content.Context
import androidx.work.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uz.farzandim.teacher.data.local.dao.CommentDao
import uz.farzandim.teacher.data.local.dao.GradeDao
import uz.farzandim.teacher.data.model.BatchCreateGradesRequest
import uz.farzandim.teacher.data.model.ChangeGradeStatusRequest
import uz.farzandim.teacher.data.model.CreateCommentRequest
import uz.farzandim.teacher.data.model.CreateGradeRequest
import uz.farzandim.teacher.data.remote.ApiService

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
            val pendingGrades = gradeDao.getPendingGrades()
            if (pendingGrades.isNotEmpty()) {
                val requests = pendingGrades.map { g ->
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

                    createdGrades.forEach { cg ->
                        val match = pendingGrades.find {
                            it.studentId == cg.studentId &&
                                    it.subjectId == cg.subjectId &&
                                    it.lessonNumber == (cg.lessonNumber ?: 1) &&
                                    (it.gradeType == cg.gradeType || (it.gradeType == "MASTERY" && cg.gradeType == "DAILY") || (it.gradeType == "DAILY" && cg.gradeType == "MASTERY"))
                        }
                        if (match != null) {
                            gradeDao.updateServerGradeId(match.localId, cg.id)
                            commentDao.updateServerGradeIdForComments(match.localId, cg.id)
                        }
                    }

                    // If any grades were marked approved locally, approve them on server
                    val approvedServerIds = pendingGrades
                        .filter { it.status == "approved" }
                        .mapNotNull { g ->
                            createdGrades.find {
                                it.studentId == g.studentId &&
                                        it.subjectId == g.subjectId &&
                                        it.lessonNumber == g.lessonNumber
                            }?.id
                        }

                    if (approvedServerIds.isNotEmpty()) {
                        apiService.changeGradeStatus(ChangeGradeStatusRequest(approvedServerIds, "approved"))
                    }
                } else if (res.code() == 403 || res.code() == 400) {
                    // One or more grades in the batch are forbidden or invalid (e.g. stale student 69).
                    // Process grades individually to isolate and purge forbidden grades while saving valid ones!
                    for (g in pendingGrades) {
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
                            val singleRes = if (g.serverId != null) {
                                apiService.updateGrade(g.serverId, singleReq)
                            } else {
                                apiService.createGrade(singleReq)
                            }
                            if (singleRes.isSuccessful && singleRes.body() != null) {
                                val sId = singleRes.body()!!.id
                                gradeDao.updateServerGradeId(g.localId, sId)
                                commentDao.updateServerGradeIdForComments(g.localId, sId)
                                if (g.status == "approved") {
                                    apiService.changeGradeStatus(ChangeGradeStatusRequest(listOf(sId), "approved"))
                                }
                            } else if (singleRes.code() == 403 || singleRes.code() == 400) {
                                // Server permanently rejected this grade! Discard from local Room DB!
                                uz.farzandim.teacher.util.AppLogger.w("GradeSync", "Purging invalid grade: localId=${g.localId}, studentId=${g.studentId}")
                                gradeDao.deleteGradeByLocalId(g.localId)
                            }
                        } catch (_: Exception) {}
                    }
                } else {
                    return Result.retry()
                }
            }

            // Sync chained comments
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
                    }
                }
            }

            sessionManager.saveLastSyncedAt(System.currentTimeMillis())
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
