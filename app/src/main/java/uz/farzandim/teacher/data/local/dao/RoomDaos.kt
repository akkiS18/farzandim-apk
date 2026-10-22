package uz.farzandim.teacher.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.farzandim.teacher.data.local.entity.*

@Dao
interface SchoolDao {
    @Query("SELECT * FROM schools ORDER BY name ASC")
    fun getAllSchoolsFlow(): Flow<List<SchoolEntity>>

    @Query("SELECT * FROM schools ORDER BY name ASC")
    suspend fun getAllSchools(): List<SchoolEntity>

    @Query("SELECT * FROM schools WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedSchool(): SchoolEntity?

    @Query("SELECT * FROM schools WHERE isSelected = 1 LIMIT 1")
    fun getSelectedSchoolFlow(): Flow<SchoolEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<SchoolEntity>)

    @Query("UPDATE schools SET isSelected = 0")
    suspend fun clearSelectedSchool()

    @Query("UPDATE schools SET isSelected = 1 WHERE id = :schoolId")
    suspend fun setSelectedSchool(schoolId: String)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM user_session LIMIT 1")
    suspend fun getSession(): UserSessionEntity?

    @Query("SELECT * FROM user_session LIMIT 1")
    fun getSessionFlow(): Flow<UserSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getClassesFlow(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes ORDER BY name ASC")
    suspend fun getClasses(): List<ClassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassEntity>)

    @Query("DELETE FROM classes")
    suspend fun clearClasses()
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getSubjectsFlow(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    suspend fun getSubjects(): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY lastName ASC, firstName ASC")
    fun getStudentsForClassFlow(classId: Int): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY lastName ASC, firstName ASC")
    suspend fun getStudentsForClass(classId: Int): List<StudentEntity>

    @Query("""
        SELECT * FROM students 
        WHERE classId = :classId 
          AND (enrollmentDate IS NULL OR enrollmentDate <= :date)
        ORDER BY lastName ASC, firstName ASC
    """)
    fun getStudentsForClassAndDateFlow(classId: Int, date: String): Flow<List<StudentEntity>>

    @Query("""
        SELECT * FROM students 
        WHERE classId = :classId 
          AND (enrollmentDate IS NULL OR enrollmentDate <= :date)
        ORDER BY lastName ASC, firstName ASC
    """)
    suspend fun getStudentsForClassAndDate(classId: Int, date: String): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("DELETE FROM students WHERE classId = :classId")
    suspend fun clearStudentsForClass(classId: Int)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM today_lessons WHERE date = :date ORDER BY lessonNumber ASC")
    fun getTodayLessonsFlow(date: String): Flow<List<TodayLessonEntity>>

    @Query("SELECT * FROM today_lessons WHERE date = :date ORDER BY lessonNumber ASC")
    suspend fun getTodayLessons(date: String): List<TodayLessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodayLessons(lessons: List<TodayLessonEntity>)

    @Query("DELETE FROM today_lessons WHERE date = :date")
    suspend fun clearTodayLessons(date: String)
}

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades WHERE subjectId = :subjectId AND lessonNumber = :lessonNumber AND gradeDate = :gradeDate")
    fun getGradesForLessonFlow(subjectId: Int, lessonNumber: Int, gradeDate: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE subjectId = :subjectId AND gradeDate = :gradeDate")
    fun getGradesForSubjectAndDateFlow(subjectId: Int, gradeDate: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE studentId = :studentId AND subjectId = :subjectId AND lessonNumber = :lessonNumber AND gradeDate = :gradeDate AND (gradeType = :gradeType OR (:gradeType = 'MASTERY' AND gradeType = 'DAILY') OR (:gradeType = 'DAILY' AND gradeType = 'MASTERY')) LIMIT 1")
    suspend fun getGradeForStudent(studentId: Int, subjectId: Int, lessonNumber: Int, gradeDate: String, gradeType: String): GradeEntity?

    @Query("SELECT * FROM grades WHERE studentId = :studentId AND subjectId = :subjectId AND lessonNumber = :lessonNumber AND gradeDate = :gradeDate")
    suspend fun getGradesForStudentLesson(studentId: Int, subjectId: Int, lessonNumber: Int, gradeDate: String): List<GradeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGrade(grade: GradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGrades(grades: List<GradeEntity>)

    @Query("SELECT * FROM grades WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingGrades(): List<GradeEntity>

    @Query("SELECT * FROM grades WHERE syncStatus = 'CONFLICT'")
    fun getConflictGradesFlow(): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE syncStatus = 'CONFLICT'")
    suspend fun getConflictGrades(): List<GradeEntity>

    @Query("SELECT * FROM grades WHERE syncStatus = 'CONFLICT' AND subjectId = :subjectId AND lessonNumber = :lessonNumber AND gradeDate = :gradeDate")
    fun getConflictGradesForLessonFlow(subjectId: Int, lessonNumber: Int, gradeDate: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE localId IN (:localIds)")
    suspend fun getGradesByLocalIds(localIds: List<String>): List<GradeEntity>

    @Query("SELECT * FROM grades WHERE subjectId = :subjectId AND lessonNumber = :lessonNumber AND gradeDate = :gradeDate")
    suspend fun getGradesForLesson(subjectId: Int, lessonNumber: Int, gradeDate: String): List<GradeEntity>

    @Query("DELETE FROM grades WHERE localId = :localId")
    suspend fun deleteGradeByLocalId(localId: String)

    @Query("UPDATE grades SET status = 'approved', updatedAt = :timestamp WHERE localId IN (:localIds)")
    suspend fun markGradesAsApproved(localIds: List<String>, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE grades SET serverId = :serverId, syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun updateServerGradeId(localId: String, serverId: Int)

    @Query("UPDATE grades SET syncStatus = 'SYNCED' WHERE localId IN (:localIds)")
    suspend fun markGradesAsSynced(localIds: List<String>)

    @Query("DELETE FROM grades WHERE studentId NOT IN (SELECT id FROM students)")
    suspend fun cleanOrphanGrades()

    @Query("DELETE FROM grades WHERE studentId = 69")
    suspend fun purgeZombieGrades()

    @Query("""
        SELECT * FROM grades 
        WHERE studentId IN (:studentIds) 
          AND subjectId = :subjectId
    """)
    suspend fun getGradesForStudentsAndSubject(studentIds: List<Int>, subjectId: Int): List<GradeEntity>

    @Query("""
        DELETE FROM grades 
        WHERE studentId IN (:studentIds) 
          AND subjectId = :subjectId 
          AND serverId IS NOT NULL 
          AND serverId NOT IN (:activeServerIds)
          AND syncStatus = 'SYNCED'
    """)
    suspend fun deleteStaleServerGrades(studentIds: List<Int>, subjectId: Int, activeServerIds: List<Int>)

    @Query("""
        DELETE FROM grades 
        WHERE studentId IN (:studentIds) 
          AND subjectId = :subjectId 
          AND serverId IS NOT NULL 
          AND syncStatus = 'SYNCED'
    """)
    suspend fun deleteAllSyncedGradesForStudentsAndSubject(studentIds: List<Int>, subjectId: Int)

    @Query("""
        DELETE FROM grades
        WHERE localId IN (:localIds)
    """)
    suspend fun deleteGradesByLocalIds(localIds: List<String>)

    @Query("""
        SELECT studentId, MAX(gradeDate) as maxDate
        FROM grades
        WHERE subjectId = :subjectId
          AND gradeDate < :beforeDate
          AND (gradeType = 'MASTERY' OR gradeType = 'DAILY')
          AND value != ''
        GROUP BY studentId
    """)
    fun getLastGradeDatesBeforeFlow(subjectId: Int, beforeDate: String): Flow<List<uz.farzandim.teacher.data.local.entity.StudentLastGradeDate>>

    @Query("""
        SELECT gradeDate FROM grades
        WHERE studentId = :studentId
          AND subjectId = :subjectId
          AND gradeDate < :beforeDate
          AND (gradeType = 'MASTERY' OR gradeType = 'DAILY')
          AND value != ''
        ORDER BY gradeDate DESC
        LIMIT 1
    """)
    suspend fun getLastGradeDateBeforeForStudent(studentId: Int, subjectId: Int, beforeDate: String): String?
}


@Dao
interface CommentDao {
    @Query("SELECT * FROM grade_comments WHERE localGradeId = :localGradeId ORDER BY createdAt ASC")
    fun getCommentsForGradeFlow(localGradeId: String): Flow<List<GradeCommentEntity>>

    @Query("SELECT * FROM grade_comments WHERE localGradeId = :localGradeId ORDER BY createdAt ASC")
    suspend fun getCommentsForGrade(localGradeId: String): List<GradeCommentEntity>

    @Query("SELECT * FROM grade_comments WHERE syncStatus = 'PENDING'")
    suspend fun getPendingComments(): List<GradeCommentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: GradeCommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<GradeCommentEntity>)

    @Query("UPDATE grade_comments SET serverId = :serverId, serverGradeId = :serverGradeId, syncStatus = 'SYNCED' WHERE localId = :localId")
    suspend fun updateServerCommentId(localId: String, serverId: Int, serverGradeId: Int)

    @Query("UPDATE grade_comments SET serverGradeId = :serverGradeId WHERE localGradeId = :localGradeId")
    suspend fun updateServerGradeIdForComments(localGradeId: String, serverGradeId: Int)

    @Query("UPDATE grade_comments SET syncStatus = 'SYNCED' WHERE localId IN (:localIds)")
    suspend fun markCommentsAsSynced(localIds: List<String>)
}

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingActions(): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET status = :status, retryCount = :retryCount WHERE id = :id")
    suspend fun updateActionStatus(id: Long, status: String, retryCount: Int)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteAction(id: Long)
}
