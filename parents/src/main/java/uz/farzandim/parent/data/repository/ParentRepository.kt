package uz.farzandim.parent.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import uz.farzandim.parent.data.local.ParentDatabase
import uz.farzandim.parent.data.local.entity.*
import uz.farzandim.parent.data.model.*
import uz.farzandim.parent.data.remote.ApiService

class ParentRepository(
    private val apiService: ApiService,
    private val database: ParentDatabase,
    private val gson: Gson
) {
    private val childDao = database.childDao()
    private val gradeDao = database.gradeDao()
    private val scheduleDao = database.scheduleDao()
    private val holidayDao = database.holidayDao()
    private val announcementDao = database.announcementDao()
    private val menuDao = database.menuDao()
    private val clubDao = database.clubDao()
    private val bookDao = database.bookDao()
    private val syncQueueDao = database.syncQueueDao()

    // 1. Children
    val childrenFlow: Flow<List<ChildEntity>> = childDao.getAllChildren()

    suspend fun fetchChildren(): Result<List<ChildEntity>> {
        return try {
            val response = apiService.getStudents("STUDENT")
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                android.util.Log.d("ParentRepository", "fetchChildren: received ${dtoList.size} children from server")
                val entities = dtoList.map { dto ->
                    val extractedLevel = dto.classLevel ?: dto.level ?: dto.className?.takeWhile { it.isDigit() }?.toIntOrNull() ?: 0
                    ChildEntity(
                        id = dto.studentId ?: dto.id,
                        userId = dto.userId ?: dto.id,
                        firstName = dto.firstName ?: "O'quvchi",
                        lastName = dto.lastName ?: "",
                        middleName = dto.middleName,
                        classId = dto.classId,
                        className = dto.className ?: "Sinf",
                        classLevel = extractedLevel,
                        address = dto.address,
                        birthdate = dto.birthdate,
                        ina = dto.ina,
                        balance = dto.balance ?: 0.0
                    )
                }
                try {
                    childDao.insertChildren(entities)
                } catch (dbEx: Exception) {
                    android.util.Log.w("ParentRepository", "Failed to cache children into Room: ${dbEx.message}")
                }
                Result.success(entities)
            } else {
                val errorStr = response.errorBody()?.string()
                android.util.Log.e("ParentRepository", "fetchChildren failed: HTTP ${response.code()} - $errorStr")
                Result.failure(Exception("Farzandlarni yuklab bo'lmadi: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ParentRepository", "fetchChildren exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // 2. Grades
    fun getGradesFlow(studentId: Int): Flow<List<ParentGradeEntity>> =
        gradeDao.getGradesForStudent(studentId)

    suspend fun fetchGrades(studentId: Int): Result<List<ParentGradeEntity>> {
        return try {
            val response = apiService.getGrades(studentId)
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val entities = dtoList.map { dto ->
                    val cleanDate = dto.gradeDate?.split("T")?.firstOrNull()?.take(10) ?: ""
                    ParentGradeEntity(
                        id = dto.id,
                        studentId = dto.studentId,
                        subjectId = dto.subjectId,
                        subjectName = dto.subjectName ?: "Fan",
                        teacherName = dto.teacherName,
                        value = dto.value ?: "",
                        numericValue = dto.numericValue,
                        gradeDate = cleanDate,
                        gradeType = dto.gradeType ?: "MASTERY",
                        gradeCategory = dto.gradeCategory ?: "DAILY",
                        lessonNumber = dto.lessonNumber,
                        status = dto.status,
                        approvedByParent = dto.approvedByParent,
                        createdAt = dto.createdAt
                    )
                }
                try {
                    gradeDao.clearGradesForStudent(studentId)
                    gradeDao.insertGrades(entities)
                } catch (dbEx: Exception) {
                    android.util.Log.w("ParentRepository", "Failed to cache grades into Room: ${dbEx.message}")
                }
                Result.success(entities)
            } else {
                Result.failure(Exception("Baholarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Parent Approve Single Grade ("Ko'rdim ✓")
    suspend fun approveGrade(gradeId: Int): Result<Unit> {
        // Optimistic local update
        try {
            gradeDao.markApprovedLocally(gradeId)
        } catch (_: Exception) {}

        val payload = JSONObject().put("grade_id", gradeId).toString()
        val queueId = syncQueueDao.enqueue(
            SyncQueueEntity(
                actionType = "PARENT_APPROVE_GRADE",
                payloadJson = payload
            )
        )

        return try {
            val response = apiService.parentApproveGrade(gradeId)
            if (response.isSuccessful) {
                syncQueueDao.dequeue(queueId)
                Result.success(Unit)
            } else {
                // Kept in queue for offline sync
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Kept in queue for offline sync
            Result.success(Unit)
        }
    }

    // 4. Parent Approve All Week Grades
    suspend fun approveAllWeek(gradeIds: List<Int>): Result<Unit> {
        try {
            gradeDao.markAllApprovedLocally(gradeIds)
        } catch (_: Exception) {}
        for (id in gradeIds) {
            val payload = JSONObject().put("grade_id", id).toString()
            val queueId = syncQueueDao.enqueue(
                SyncQueueEntity(
                    actionType = "PARENT_APPROVE_GRADE",
                    payloadJson = payload
                )
            )
            try {
                val response = apiService.parentApproveGrade(id)
                if (response.isSuccessful) {
                    syncQueueDao.dequeue(queueId)
                }
            } catch (_: Exception) {
                // Kept in queue
            }
        }
        return Result.success(Unit)
    }

    // 5. Schedule
    fun getScheduleFlow(classId: Int): Flow<List<ParentScheduleEntity>> =
        scheduleDao.getScheduleForClass(classId)

    suspend fun fetchSchedule(classId: Int, dateStr: String? = null): Result<List<ParentScheduleEntity>> {
        return try {
            val response = apiService.getClassSchedule(classId, dateStr)
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val entities = dtoList.map { dto ->
                    ParentScheduleEntity(
                        id = dto.id,
                        classId = dto.classId,
                        dayOfWeek = dto.dayOfWeek,
                        lessonNumber = dto.lessonNumber,
                        subjectId = dto.subjectId,
                        subjectName = dto.subjectName,
                        teacherName = dto.teacherName,
                        room = dto.room,
                        startTime = dto.startTime,
                        endTime = dto.endTime,
                        topicName = dto.topicName,
                        homework = dto.homework
                    )
                }
                try {
                    scheduleDao.insertSchedules(entities)
                } catch (dbEx: Exception) {
                    android.util.Log.w("ParentRepository", "Failed to cache schedules into Room: ${dbEx.message}")
                }
                Result.success(entities)
            } else {
                Result.failure(Exception("Dars jadvalini yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. Holidays
    val holidaysFlow: Flow<List<HolidayEntity>> = holidayDao.getHolidays()

    suspend fun fetchHolidays(): Result<List<HolidayEntity>> {
        return try {
            val response = apiService.getHolidays()
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val entities = dtoList.map { dto ->
                    val cleanDate = dto.holidayDate?.split("T")?.firstOrNull()?.take(10) ?: ""
                    HolidayEntity(
                        id = dto.id,
                        name = dto.name ?: "Dam olish kuni",
                        holidayDate = cleanDate,
                        targetClassesJson = gson.toJson(dto.targetClasses ?: emptyList<Int>()),
                        targetLevelsJson = gson.toJson(dto.targetLevels ?: emptyList<Int>())
                    )
                }
                try {
                    holidayDao.insertHolidays(entities)
                } catch (dbEx: Exception) {
                    android.util.Log.w("ParentRepository", "Failed to cache holidays into Room: ${dbEx.message}")
                }
                Result.success(entities)
            } else {
                Result.failure(Exception("Bayram kunlarini yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 7. Announcements
    val announcementsFlow: Flow<List<AnnouncementEntity>> = announcementDao.getAnnouncements()

    suspend fun fetchAnnouncements(): Result<List<AnnouncementEntity>> {
        return try {
            val response = apiService.getAnnouncements()
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val entities = dtoList.map { dto ->
                    AnnouncementEntity(
                        id = dto.id,
                        title = dto.title ?: "E'lon",
                        content = dto.content ?: "",
                        authorName = dto.authorName,
                        createdAt = dto.createdAt,
                        isPoll = dto.isPoll,
                        pollOptionsJson = if (dto.pollOptions != null) gson.toJson(dto.pollOptions) else null,
                        userVotedOptionId = dto.userVotedOptionId,
                        imageUrlsJson = if (!dto.imageUrls.isNullOrEmpty()) gson.toJson(dto.imageUrls) else null
                    )
                }
                try {
                    announcementDao.insertAnnouncements(entities)
                } catch (dbEx: Exception) {
                    android.util.Log.w("ParentRepository", "Failed to cache announcements into Room: ${dbEx.message}")
                }
                Result.success(entities)
            } else {
                Result.failure(Exception("E'lonlarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun votePoll(announcementId: Int, optionId: Int): Result<Unit> {
        try {
            announcementDao.markVotedLocally(announcementId, optionId)
        } catch (_: Exception) {}
        val payload = JSONObject().put("announcement_id", announcementId).put("option_id", optionId).toString()
        val queueId = syncQueueDao.enqueue(
            SyncQueueEntity(actionType = "VOTE_POLL", payloadJson = payload)
        )

        return try {
            val response = apiService.votePoll(announcementId, VotePollRequest(optionId))
            if (response.isSuccessful) {
                syncQueueDao.dequeue(queueId)
                fetchAnnouncements()
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    // 8. Dining Menu
    fun getMenuFlow(dateStr: String): Flow<MenuEntity?> = menuDao.getMenuForDate(dateStr)

    suspend fun fetchMenu(dateStr: String): Result<DailyMenuResponse> {
        return try {
            val response = apiService.getMenu(dateStr)
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                try {
                    menuDao.insertMenu(
                        MenuEntity(
                            menuDate = dateStr,
                            mealsJson = gson.toJson(data.meals),
                            notes = data.notes
                        )
                    )
                } catch (dbEx: Exception) {
                    android.util.Log.w("ParentRepository", "Failed to cache menu into Room: ${dbEx.message}")
                }
                Result.success(data)
            } else {
                Result.failure(Exception("Taomnomani yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 9. Comments
    suspend fun getCommentsFeed(): Result<List<CommentItemDto>> {
        return try {
            val response = apiService.getCommentsFeed()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Izohlarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGradeComments(gradeId: Int): Result<List<CommentItemDto>> {
        return try {
            val response = apiService.getGradeComments(gradeId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Bahoga oid izohlarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGradeComment(gradeId: Int, content: String): Result<CommentItemDto> {
        return try {
            val response = apiService.createGradeComment(gradeId, CreateGradeCommentRequest(content))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Izoh yuborishda xatolik"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMenuComment(menuDate: String, content: String): Result<CommentItemDto> {
        return try {
            val response = apiService.createMenuComment(CreateMenuCommentRequest(menuDate, content))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Oshxona fikrini yuborishda xatolik"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 10. Clubs
    fun getClubsFlow(studentId: Int): Flow<List<ClubEntity>> = clubDao.getClubsForStudent(studentId)

    suspend fun fetchClubs(studentId: Int): Result<List<ClubEntity>> {
        return try {
            val response = apiService.getClubs(studentId)
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val entities = dtoList.map { dto ->
                    ClubEntity(
                        id = dto.id,
                        studentId = studentId,
                        name = dto.name,
                        description = dto.description,
                        teacherName = dto.teacherName,
                        status = dto.status ?: "NONE",
                        scheduleText = dto.scheduleText
                    )
                }
                try {
                    clubDao.insertClubs(entities)
                } catch (dbEx: Exception) {
                    android.util.Log.w("ParentRepository", "Failed to cache clubs into Room: ${dbEx.message}")
                }
                Result.success(entities)
            } else {
                Result.failure(Exception("To'garaklarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestJoinClub(clubId: Int, studentId: Int): Result<Unit> {
        try {
            clubDao.updateClubStatusLocally(clubId, studentId, "PENDING")
        } catch (_: Exception) {}
        return try {
            val response = apiService.requestJoinClub(clubId, mapOf("student_id" to studentId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("To'garakka so'rov yuborilmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelClubRequest(clubId: Int, studentId: Int): Result<Unit> {
        try {
            clubDao.updateClubStatusLocally(clubId, studentId, "NONE")
        } catch (_: Exception) {}
        return try {
            val response = apiService.cancelClubRequest(clubId, mapOf("student_id" to studentId))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("So'rovni bekor qilib bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 11. Books
    val booksFlow: Flow<List<BookEntity>> = bookDao.getBooks()

    suspend fun fetchBooks(level: Int? = null): Result<List<BookEntity>> {
        return try {
            val response = apiService.getBooks(level)
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val entities = dtoList.map { dto ->
                    BookEntity(
                        id = dto.id,
                        title = dto.title,
                        author = dto.author,
                        description = dto.description,
                        coverUrl = dto.coverUrl,
                        fileUrl = dto.fileUrl,
                        level = dto.level,
                        categoryName = dto.categoryName
                    )
                }
                try {
                    bookDao.insertBooks(entities)
                } catch (dbEx: Exception) {
                    android.util.Log.w("ParentRepository", "Failed to cache books into Room: ${dbEx.message}")
                }
                Result.success(entities)
            } else {
                Result.failure(Exception("Kitoblarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 12. Balance
    suspend fun getNextCharge(studentId: Int): Result<NextChargeDto> {
        return try {
            val response = apiService.getNextCharge(studentId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("To'lov ma'lumotlarini olib bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBalanceHistory(studentId: Int): Result<List<TransactionDto>> {
        return try {
            val response = apiService.getBalanceHistory(studentId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Operatsiyalar tarixini olib bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 13. AI Report
    suspend fun getLatestAiReport(): Result<AiReportDto> {
        return try {
            val response = apiService.getLatestAiReport()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("AI hisobot topilmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
