package uz.farzandim.teacher.data.repository

import kotlinx.coroutines.flow.Flow
import uz.farzandim.teacher.data.local.dao.*
import uz.farzandim.teacher.data.local.entity.*
import uz.farzandim.teacher.data.model.*
import uz.farzandim.teacher.data.remote.ApiService
import uz.farzandim.teacher.util.AppLogger
import java.text.SimpleDateFormat
import java.util.*

class JournalRepository(
    private val apiService: ApiService,
    private val classDao: ClassDao,
    private val subjectDao: SubjectDao,
    private val studentDao: StudentDao,
    private val gradeDao: GradeDao,
    private val commentDao: CommentDao
) {

    fun getClassesFlow(): Flow<List<ClassEntity>> = classDao.getClassesFlow()
    fun getSubjectsFlow(): Flow<List<SubjectEntity>> = subjectDao.getSubjectsFlow()
    fun getStudentsForClassFlow(classId: Int): Flow<List<StudentEntity>> = studentDao.getStudentsForClassFlow(classId)
    fun getStudentsForClassAndDateFlow(classId: Int, date: String): Flow<List<StudentEntity>> =
        studentDao.getStudentsForClassAndDateFlow(classId, date)

    fun getGradesForLessonFlow(subjectId: Int, lessonNumber: Int, gradeDate: String): Flow<List<GradeEntity>> {
        return gradeDao.getGradesForLessonFlow(subjectId, lessonNumber, gradeDate)
    }

    fun getCommentsForGradeFlow(localGradeId: String): Flow<List<GradeCommentEntity>> {
        return commentDao.getCommentsForGradeFlow(localGradeId)
    }

    /**
     * Returns the most recent gradeDate (as "yyyy-MM-dd") where this student received a
     * MASTERY grade for the given subject, strictly before [beforeDate].
     * Used by the journal UI to show "N kun oldin" badge next to the student's name.
     */
    fun getLastGradeDatesBeforeFlow(subjectId: Int, beforeDate: String): Flow<List<uz.farzandim.teacher.data.local.entity.StudentLastGradeDate>> {
        return gradeDao.getLastGradeDatesBeforeFlow(subjectId, beforeDate)
    }

    suspend fun getLastGradeDateBefore(studentId: Int, subjectId: Int, beforeDate: String): String? {
        return gradeDao.getLastGradeDateBeforeForStudent(studentId, subjectId, beforeDate)
    }


    suspend fun refreshClasses(): Result<List<ClassEntity>> {
        return try {
            val res = apiService.getClasses()
            if (res.isSuccessful && res.body() != null) {
                val entities = res.body()!!.map {
                    ClassEntity(
                        id = it.id,
                        name = it.name,
                        isMainTeacher = it.isMainTeacher,
                        defaultSubjectId = it.subjectId,
                        defaultSubjectName = it.subjectName
                    )
                }
                classDao.clearClasses()
                classDao.insertClasses(entities)
                Result.success(entities)
            } else {
                Result.success(classDao.getClasses())
            }
        } catch (e: Exception) {
            Result.success(classDao.getClasses())
        }
    }

    suspend fun refreshSubjects(): Result<List<SubjectEntity>> {
        return try {
            val res = apiService.getSubjects()
            if (res.isSuccessful && res.body() != null) {
                val entities = res.body()!!.map {
                    SubjectEntity(id = it.id, name = it.name)
                }
                subjectDao.clearSubjects()
                subjectDao.insertSubjects(entities)
                Result.success(entities)
            } else {
                Result.success(subjectDao.getSubjects())
            }
        } catch (e: Exception) {
            Result.success(subjectDao.getSubjects())
        }
    }

    private fun getDayOfWeek(dateStr: String): Int {
        return try {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val d = sdf.parse(dateStr) ?: Date()
            cal.time = d
            when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> 7
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> 1
            }
        } catch (_: Exception) {
            1
        }
    }

    suspend fun fetchClassScheduleToday(classId: Int, date: String): List<JournalLessonItem> {
        val dow = getDayOfWeek(date)
        return try {
            val res = apiService.getClassSchedule(classId, date)
            if (res.isSuccessful && res.body() != null) {
                res.body()!!
                    .filter { it.dayOfWeek == dow && it.subjectId > 0 && !it.subjectName.isNullOrBlank() }
                    .sortedBy { it.lessonNumber }
                    .map {
                        JournalLessonItem(
                            subjectId = it.subjectId,
                            subjectName = it.subjectName ?: "Fan",
                            lessonNumber = it.lessonNumber
                        )
                    }
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    data class LessonTopicAndHomework(
        val planId: Int? = null,
        val topic: String? = null,
        val homework: String? = null
    )

    suspend fun fetchLessonTopicAndHomework(classId: Int, subjectId: Int, lessonNumber: Int, date: String): LessonTopicAndHomework {
        return try {
            val res = apiService.getLessonPlans(classId, subjectId, date, date)
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                val plans = res.body()!!
                val matched = plans.find { it.lessonNumber == lessonNumber } ?: plans.first()
                LessonTopicAndHomework(
                    planId = matched.id,
                    topic = matched.topicName.ifBlank { null },
                    homework = matched.homework?.ifBlank { null }
                )
            } else {
                LessonTopicAndHomework()
            }
        } catch (_: Exception) {
            LessonTopicAndHomework()
        }
    }

    suspend fun fetchLessonTopic(classId: Int, subjectId: Int, lessonNumber: Int, date: String): String? {
        return fetchLessonTopicAndHomework(classId, subjectId, lessonNumber, date).topic
    }

    suspend fun saveLessonTopicAndHomework(
        classId: Int,
        subjectId: Int,
        lessonNumber: Int,
        date: String,
        topic: String,
        homework: String,
        existingPlanId: Int?
    ): Result<Int?> {
        val dow = getDayOfWeek(date)
        val req = SaveLessonPlanRequest(
            classId = classId,
            subjectId = subjectId,
            dayOfWeek = dow,
            lessonNumber = lessonNumber,
            startDate = date,
            topicName = topic.ifBlank { "Dars mashg'uloti" },
            homework = homework.trim(),
            notes = ""
        )
        return try {
            if (existingPlanId != null && existingPlanId > 0) {
                val res = apiService.updateLessonPlan(existingPlanId, req)
                if (res.isSuccessful) {
                    Result.success(existingPlanId)
                } else {
                    Result.failure(Exception("HTTP ${res.code()}: ${res.errorBody()?.string()}"))
                }
            } else {
                val res = apiService.createLessonPlan(req)
                if (res.isSuccessful) {
                    Result.success(res.body()?.id)
                } else {
                    Result.failure(Exception("HTTP ${res.code()}: ${res.errorBody()?.string()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAdjacentLesson(
        classId: Int,
        subjectId: Int,
        lessonNumber: Int,
        date: String,
        direction: String
    ): Result<AdjacentLessonDto?> {
        return try {
            val res = apiService.getJournalAdjacentLesson(classId, subjectId, lessonNumber, date, direction)
            if (res.isSuccessful) {
                Result.success(res.body()?.lesson)
            } else {
                Result.failure(Exception("HTTP ${res.code()}: ${res.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshStudents(classId: Int): Result<List<StudentEntity>> {
        return try {
            val res = apiService.getStudents(role = "STUDENT", classId = classId)
            if (res.isSuccessful && res.body() != null) {
                val entities = res.body()!!.map {
                    StudentEntity(
                        id = it.realStudentId, // Must be student_id from students table!
                        classId = classId,
                        userId = it.id, // User ID
                        firstName = it.firstName,
                        lastName = it.lastName,
                        middleName = it.middleName,
                        enrollmentDate = it.enrollmentDate?.take(10)
                    )
                }
                studentDao.clearStudentsForClass(classId)
                studentDao.insertStudents(entities)
                // Clean any outdated grades that had invalid/old user_id keys
                gradeDao.cleanOrphanGrades()
                Result.success(entities)
            } else {
                Result.success(studentDao.getStudentsForClass(classId))
            }
        } catch (e: Exception) {
            Result.success(studentDao.getStudentsForClass(classId))
        }
    }

    suspend fun cleanupStaleGrades() {
        try {
            gradeDao.cleanOrphanGrades()
        } catch (_: Exception) {}
    }

    suspend fun refreshGrades(classId: Int, subjectId: Int, date: String): Result<Unit> {
        return try {
            val res = apiService.getGrades(classId = classId, subjectId = subjectId)
            if (res.isSuccessful && res.body() != null) {
                val remoteGrades = res.body()!!

                // Fetch class students to scope grade reconciliation
                val classStudents = studentDao.getStudentsForClass(classId)
                val allStudentIds = classStudents.map { it.id }.distinct()

                val existingLocalGrades = if (allStudentIds.isNotEmpty()) {
                    gradeDao.getGradesForStudentsAndSubject(allStudentIds, subjectId)
                } else {
                    emptyList()
                }

                val existingMap = existingLocalGrades.associateBy {
                    "${it.studentId}_${it.lessonNumber}_${it.gradeDate}_${it.gradeType}"
                }

                // 1. Reconcile: Purge any local SYNCED grades that were deleted on the server
                val remoteKeySet = remoteGrades.map { g ->
                    val cleanDate = if (g.gradeDate.contains('T')) {
                        g.gradeDate.substringBefore('T').take(10)
                    } else {
                        g.gradeDate.take(10)
                    }
                    val resolvedType = when (g.gradeType?.uppercase()) {
                        "DAILY" -> "MASTERY"
                        null, "" -> "MASTERY"
                        else -> g.gradeType
                    }
                    val lessonNum = if (g.lessonNumber != null && g.lessonNumber > 0) g.lessonNumber else 1
                    "${g.studentId}_${lessonNum}_${cleanDate}_${resolvedType}"
                }.toSet()

                val staleLocalIds = existingLocalGrades.filter { local ->
                    local.syncStatus == "SYNCED" && !remoteKeySet.contains("${local.studentId}_${local.lessonNumber}_${local.gradeDate}_${local.gradeType}")
                }.map { it.localId }

                if (staleLocalIds.isNotEmpty()) {
                    gradeDao.deleteGradesByLocalIds(staleLocalIds)
                    AppLogger.i("JournalRepo", "Purged ${staleLocalIds.size} server-deleted grades from local Room DB")
                }

                // 2. Map and Upsert active server grades, checking for conflicts with pending local drafts
                val entities = remoteGrades.map { g ->
                    val cleanDate = if (g.gradeDate.contains('T')) {
                        g.gradeDate.substringBefore('T').take(10)
                    } else {
                        g.gradeDate.take(10)
                    }
                    val resolvedGradeType = when (g.gradeType?.uppercase()) {
                        "DAILY" -> "MASTERY"
                        null, "" -> "MASTERY"
                        else -> g.gradeType
                    }
                    val lessonNum = if (g.lessonNumber != null && g.lessonNumber > 0) g.lessonNumber else 1
                    val key = "${g.studentId}_${lessonNum}_${cleanDate}_${resolvedGradeType}"
                    val existing = existingMap[key]

                    if (existing != null && (existing.syncStatus == "PENDING_DELETE" || existing.syncStatus == "REJECTED")) {
                        // Preserve pending offline tombstone or rejected draft until resolved
                        existing
                    } else if (existing != null && (existing.syncStatus == "PENDING_UPSERT" || existing.syncStatus == "CONFLICT")) {
                        if (existing.value != g.value) {
                            // CONFLICT: Teacher set a grade offline, but server (class leader) set a different grade!
                            AppLogger.w("JournalRepo", "Conflict detected for student ${g.studentId}: local='${existing.value}' vs server='${g.value}'")
                            existing.copy(
                                syncStatus = "CONFLICT",
                                conflictRemoteValue = g.value,
                                conflictRemoteId = g.id,
                                updatedAt = System.currentTimeMillis()
                            )
                        } else {
                            // Both have the same value -> safely mark synced if status also matches
                            val needsApproveSync = existing.status == "approved" && g.status != "approved"
                            existing.copy(
                                serverId = g.id,
                                syncStatus = if (needsApproveSync) "PENDING_UPSERT" else "SYNCED",
                                conflictRemoteValue = null,
                                conflictRemoteId = null,
                                updatedAt = System.currentTimeMillis()
                            )
                        }
                    } else {
                        GradeEntity(
                            localId = existing?.localId ?: "srv_${g.id}",
                            serverId = g.id,
                            studentId = g.studentId,
                            subjectId = g.subjectId,
                            lessonNumber = lessonNum,
                            gradeDate = cleanDate,
                            gradeType = resolvedGradeType,
                            gradeCategory = g.gradeCategory ?: "DAILY",
                            value = g.value,
                            numericValue = g.numericValue,
                            status = g.status ?: "marked",
                            approvedByParent = g.approvedByParent,
                            gradingSystemId = g.gradingSystemId,
                            syncStatus = "SYNCED",
                            conflictRemoteValue = null,
                            conflictRemoteId = null,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                }

                if (entities.isNotEmpty()) {
                    gradeDao.upsertGrades(entities)
                }
                Result.success(Unit)
            } else {
                AppLogger.w("JournalRepo", "Failed to get grades: ${res.code()} ${res.message()}")
                Result.failure(Exception("HTTP ${res.code()}: ${res.message()}"))
            }
        } catch (e: Exception) {
            AppLogger.e("JournalRepo", "Exception refreshing grades: ${e.message}")
            Result.failure(e)
        }
    }

    // 1. Two-tier save step 1: Immediate local save + instant background backend draft sync
    suspend fun saveGradeDraft(
        studentId: Int,
        subjectId: Int,
        lessonNumber: Int,
        gradeDate: String,
        gradeType: String = "MASTERY",
        value: String,
        gradeCategory: String = "DAILY"
    ): GradeEntity? {
        val resolvedGradeType = if (gradeType.equals("DAILY", ignoreCase = true)) "MASTERY" else gradeType
        val existing = gradeDao.getGradeForStudent(studentId, subjectId, lessonNumber, gradeDate, resolvedGradeType)

        // If already locked / approved, it cannot be edited!
        if (existing != null && existing.status == "approved" && existing.syncStatus != "PENDING_DELETE") {
            return null
        }

        // If user cleared the cell (empty value)
        if (value.isBlank()) {
            if (existing != null) {
                val serverId = existing.serverId
                if (serverId != null) {
                    // Mark as PENDING_DELETE tombstone so offline deletions are not lost
                    gradeDao.markGradeAsPendingDelete(existing.localId)
                    try {
                        val delRes = apiService.deleteGrade(serverId)
                        if (delRes.isSuccessful || delRes.code() == 404) {
                            gradeDao.deleteGradeByLocalId(existing.localId)
                            AppLogger.i("GradeSync", "Deleted grade on backend: $serverId")
                        } else {
                            AppLogger.w("GradeSync", "Delete grade returned ${delRes.code()}, keeping PENDING_DELETE for worker")
                        }
                    } catch (e: Exception) {
                        AppLogger.e("GradeSync", "Offline delete queued for grade $serverId: ${e.message}")
                    }
                } else {
                    // Never synced to server, safe to delete locally immediately
                    gradeDao.deleteGradeByLocalId(existing.localId)
                }
            }
            return null
        }

        val numVal = value.toDoubleOrNull()
        val localId = existing?.localId ?: UUID.randomUUID().toString()
        val initialServerId = existing?.serverId

        val entity = GradeEntity(
            localId = localId,
            serverId = initialServerId,
            studentId = studentId,
            subjectId = subjectId,
            lessonNumber = lessonNumber,
            gradeDate = gradeDate,
            gradeType = resolvedGradeType,
            gradeCategory = gradeCategory,
            value = value,
            numericValue = numVal,
            status = "draft",
            syncStatus = "PENDING_UPSERT",
            updatedAt = System.currentTimeMillis()
        )

        // 1. Immediately save locally in Room (instant UI update via Flow)
        gradeDao.upsertGrade(entity)

        // 2. Immediately send to backend asynchronously in draft state
        try {
            val req = CreateGradeRequest(
                studentId = studentId,
                subjectId = subjectId,
                value = value,
                gradeDate = gradeDate,
                gradingSystemId = entity.gradingSystemId,
                gradeType = resolvedGradeType,
                gradeCategory = entity.gradeCategory,
                lessonNumber = lessonNumber
            )

            if (initialServerId != null) {
                val res = apiService.updateGrade(initialServerId, req)
                if (res.isSuccessful && res.body() != null) {
                    gradeDao.upsertGrade(entity.copy(syncStatus = "SYNCED"))
                    AppLogger.i("GradeSync", "Updated grade draft on backend: ID $initialServerId")
                } else if (res.code() == 403 || res.code() == 400) {
                    AppLogger.w("GradeSync", "Backend update rejected ${res.code()}. Marking local draft as REJECTED.")
                    gradeDao.markGradeAsRejected(localId)
                    return null
                } else {
                    AppLogger.w("GradeSync", "Backend update returned ${res.code()}")
                }
            } else {
                val res = apiService.createGrade(req)
                if (res.isSuccessful && res.body() != null) {
                    val created = res.body()!!
                    gradeDao.updateServerGradeId(localId, created.id)
                    commentDao.updateServerGradeIdForComments(localId, created.id)
                    AppLogger.i("GradeSync", "Created grade draft on backend: ID ${created.id}")
                } else if (res.code() == 403 || res.code() == 400) {
                    AppLogger.w("GradeSync", "Backend create rejected ${res.code()}. Marking local draft as REJECTED.")
                    gradeDao.markGradeAsRejected(localId)
                    return null
                } else {
                    AppLogger.w("GradeSync", "Backend create returned ${res.code()}")
                }
            }
        } catch (e: Exception) {
            AppLogger.e("GradeSync", "Immediate draft sync error: ${e.message}. Saved locally, will sync later.")
        }

        return entity
    }

    // 2. Two-tier save step 2: Finalize & Approve ("Saqlash" button)
    suspend fun finalizeAndApproveGrades(
        subjectId: Int,
        lessonNumber: Int,
        gradeDate: String,
        classId: Int? = null
    ): Result<Int> {
        val allLessonGrades = gradeDao.getGradesForLesson(subjectId, lessonNumber, gradeDate, classId)
        val unapprovedGrades = allLessonGrades.filter {
            it.status != "approved" && it.syncStatus != "PENDING_DELETE" && it.syncStatus != "CONFLICT" && it.syncStatus != "REJECTED"
        }

        if (unapprovedGrades.isEmpty()) {
            return Result.success(0)
        }

        val hadPendingValueEdits = unapprovedGrades.filter { it.serverId != null && it.syncStatus == "PENDING_UPSERT" }
            .map { it.localId }.toSet()

        // Lock locally immediately as status='approved' and syncStatus='PENDING_UPSERT' so UI locks and worker tracks them
        val localIds = unapprovedGrades.map { it.localId }
        gradeDao.markGradesAsApproved(localIds)

        try {
            // Map of localId -> confirmed serverId ready to be approved on server
            val confirmedServerIdsByLocalId = mutableMapOf<String, Int>()

            // 1. Any grades that haven't been created on the server yet (e.g. offline when entered)
            val notOnServer = unapprovedGrades.filter { it.serverId == null }
            if (notOnServer.isNotEmpty()) {
                val requests = notOnServer.map { g ->
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
                val batchRes = apiService.batchCreateGrades(BatchCreateGradesRequest(requests))
                if (batchRes.isSuccessful && batchRes.body() != null) {
                    val createdList = batchRes.body()!!
                    val matchedLocalIds = mutableSetOf<String>()

                    createdList.forEach { cg ->
                        val cgDate = cg.gradeDate.substringBefore('T').take(10)
                        val cgType = if (cg.gradeType.equals("DAILY", ignoreCase = true) || cg.gradeType.isNullOrBlank()) "MASTERY" else cg.gradeType
                        val cgCat = cg.gradeCategory ?: "DAILY"
                        val cgLesson = if (cg.lessonNumber != null && cg.lessonNumber > 0) cg.lessonNumber else 1

                        val match = notOnServer.find {
                            it.localId !in matchedLocalIds &&
                                    it.studentId == cg.studentId &&
                                    it.subjectId == cg.subjectId &&
                                    it.lessonNumber == cgLesson &&
                                    it.gradeDate.take(10) == cgDate &&
                                    (it.gradeCategory.ifBlank { "DAILY" }) == cgCat &&
                                    (it.gradeType == cgType || (it.gradeType == "MASTERY" && cgType == "DAILY") || (it.gradeType == "DAILY" && cgType == "MASTERY"))
                        }
                        if (match != null) {
                            matchedLocalIds.add(match.localId)
                            confirmedServerIdsByLocalId[match.localId] = cg.id
                            gradeDao.updateServerIdOnly(match.localId, cg.id)
                            commentDao.updateServerGradeIdForComments(match.localId, cg.id)
                        }
                    }
                } else if (batchRes.code() == 403 || batchRes.code() == 400) {
                    // Fallback to individual creates to isolate invalid/unauthorized grades
                    for (g in notOnServer) {
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
                                confirmedServerIdsByLocalId[g.localId] = sId
                                gradeDao.updateServerIdOnly(g.localId, sId)
                                commentDao.updateServerGradeIdForComments(g.localId, sId)
                            } else if (singleRes.code() == 403 || singleRes.code() == 400) {
                                AppLogger.w("GradeSync", "Rejected grade: localId=${g.localId}, studentId=${g.studentId}")
                                gradeDao.markGradeAsRejected(g.localId)
                            }
                        } catch (_: Exception) {}
                    }
                } else {
                    AppLogger.w("GradeSync", "Batch create returned ${batchRes.code()}, leaving grades as PENDING_UPSERT")
                }
            }

            // 2. Process grades that already had a serverId
            for (g in unapprovedGrades.filter { it.serverId != null }) {
                val sId = g.serverId!!
                if (g.localId in hadPendingValueEdits) {
                    // Value was edited offline, push value update before approving
                    val updateReq = CreateGradeRequest(
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
                        val upRes = apiService.updateGrade(sId, updateReq)
                        if (upRes.isSuccessful) {
                            confirmedServerIdsByLocalId[g.localId] = sId
                        } else if (upRes.code() == 403 || upRes.code() == 400) {
                            gradeDao.markGradeAsRejected(g.localId)
                        }
                    } catch (_: Exception) {}
                } else {
                    confirmedServerIdsByLocalId[g.localId] = sId
                }
            }

            // 3. Approve and lock confirmed grades on the server
            var approvedSuccess = false
            if (confirmedServerIdsByLocalId.isNotEmpty()) {
                val serverIdsToApprove = confirmedServerIdsByLocalId.values.distinct()
                val statusRes = apiService.changeGradeStatus(
                    ChangeGradeStatusRequest(serverIdsToApprove, "approved")
                )
                if (statusRes.isSuccessful) {
                    approvedSuccess = true
                    // ONLY mark as SYNCED the records that were confirmed AND approved on the server!
                    gradeDao.markGradesAsSynced(confirmedServerIdsByLocalId.keys.toList())
                    AppLogger.i("GradeSync", "Successfully approved ${serverIdsToApprove.size} grades on server")
                } else {
                    AppLogger.w("GradeSync", "Approve status change failed: ${statusRes.code()}")
                }
            }

            syncPendingComments()

            return if (approvedSuccess && confirmedServerIdsByLocalId.size == unapprovedGrades.size) {
                Result.success(unapprovedGrades.size)
            } else {
                Result.failure(Exception("Server bilan aloqa xatosi: baholar qurilmada tasdiqlandi va tarmoq tiklanganda avtomatik sinxronlanadi"))
            }
        } catch (e: Exception) {
            AppLogger.e("GradeSync", "finalizeAndApproveGrades error: ${e.message}")
            // Keep grades as status='approved' and syncStatus='PENDING_UPSERT' so worker will sync them
            return Result.failure(Exception("Oflayn saqlandi: internet ulanganda serverga yuboriladi"))
        }
    }

    // Add comment to grade (supports offline local UUID linking)
    suspend fun addComment(
        localGradeId: String,
        authorId: Int,
        authorName: String,
        content: String
    ): Result<GradeCommentEntity> {
        val grade = gradeDao.getGradesByLocalIds(listOf(localGradeId)).firstOrNull()
        val serverGradeId = grade?.serverId

        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val comment = GradeCommentEntity(
            localId = UUID.randomUUID().toString(),
            serverId = null,
            localGradeId = localGradeId,
            serverGradeId = serverGradeId,
            authorId = authorId,
            authorName = authorName,
            content = content,
            createdAt = nowIso,
            syncStatus = "PENDING"
        )

        commentDao.insertComment(comment)

        // If serverGradeId is available, try sending immediately
        if (serverGradeId != null) {
            try {
                val res = apiService.createGradeComment(serverGradeId, CreateCommentRequest(content))
                if (res.isSuccessful && res.body() != null) {
                    val serverComment = res.body()!!
                    commentDao.updateServerCommentId(comment.localId, serverComment.id, serverGradeId)
                }
            } catch (e: Exception) {
                // Offline, kept as PENDING
            }
        }

        return Result.success(comment)
    }

    suspend fun syncPendingComments() {
        val pending = commentDao.getPendingComments()
        for (c in pending) {
            val serverGradeId = c.serverGradeId ?: run {
                // Check if grade got serverId in the meantime
                val g = gradeDao.getGradesByLocalIds(listOf(c.localGradeId)).firstOrNull()
                g?.serverId
            }

            if (serverGradeId != null) {
                try {
                    val res = apiService.createGradeComment(serverGradeId, CreateCommentRequest(c.content))
                    if (res.isSuccessful && res.body() != null) {
                        commentDao.updateServerCommentId(c.localId, res.body()!!.id, serverGradeId)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun getConflictGradesFlow(): Flow<List<GradeEntity>> = gradeDao.getConflictGradesFlow()

    fun getConflictGradesForLessonFlow(subjectId: Int, lessonNumber: Int, gradeDate: String): Flow<List<GradeEntity>> =
        gradeDao.getConflictGradesForLessonFlow(subjectId, lessonNumber, gradeDate)

    suspend fun resolveConflictKeepLocal(localId: String): Result<Unit> {
        return try {
            val grade = gradeDao.getGradesByLocalIds(listOf(localId)).firstOrNull()
                ?: return Result.failure(Exception("Baho topilmadi"))

            val req = CreateGradeRequest(
                studentId = grade.studentId,
                subjectId = grade.subjectId,
                value = grade.value,
                gradeDate = grade.gradeDate,
                gradingSystemId = grade.gradingSystemId,
                gradeType = grade.gradeType,
                gradeCategory = grade.gradeCategory,
                lessonNumber = grade.lessonNumber
            )

            val targetServerId = grade.conflictRemoteId ?: grade.serverId
            val res = if (targetServerId != null) {
                apiService.updateGrade(targetServerId, req)
            } else {
                apiService.createGrade(req)
            }

            if (res.isSuccessful && res.body() != null) {
                val finalServerId = res.body()!!.id
                gradeDao.upsertGrade(
                    grade.copy(
                        serverId = finalServerId,
                        syncStatus = "SYNCED",
                        conflictRemoteValue = null,
                        conflictRemoteId = null,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Serverga saqlashda xatolik: HTTP ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveConflictAcceptRemote(localId: String): Result<Unit> {
        return try {
            val grade = gradeDao.getGradesByLocalIds(listOf(localId)).firstOrNull()
                ?: return Result.failure(Exception("Baho topilmadi"))

            val remoteValue = grade.conflictRemoteValue ?: grade.value
            val remoteId = grade.conflictRemoteId ?: grade.serverId

            gradeDao.upsertGrade(
                grade.copy(
                    value = remoteValue,
                    numericValue = remoteValue.toDoubleOrNull(),
                    serverId = remoteId,
                    syncStatus = "SYNCED",
                    conflictRemoteValue = null,
                    conflictRemoteId = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
