package uz.farzandim.teacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "schools")
data class SchoolEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isSelected: Boolean = false
)

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val role: String,
    val phone: String?,
    val documentNo: String?,
    val schoolId: String,
    val schoolName: String,
    val token: String,
    val refreshToken: String?
)

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val isMainTeacher: Boolean = false,
    val defaultSubjectId: Int? = null,
    val defaultSubjectName: String? = null
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: Int,
    val name: String
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: Int,
    val classId: Int,
    val userId: Int?,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val enrollmentDate: String? = null
)

@Entity(tableName = "today_lessons")
data class TodayLessonEntity(
    @PrimaryKey val id: String, // composite key e.g. "date_classId_subjectId_lessonNumber"
    val date: String,
    val scheduleId: Int?,
    val classId: Int,
    val className: String,
    val subjectId: Int,
    val subjectName: String,
    val lessonNumber: Int,
    val time: String?,
    val isFullyMarked: Boolean,
    val isMarked: Boolean,
    val markedStudentsCount: Int,
    val totalStudentsCount: Int
)

@Entity(tableName = "grades")
data class GradeEntity(
    @PrimaryKey val localId: String = UUID.randomUUID().toString(),
    val serverId: Int? = null,
    val studentId: Int,
    val subjectId: Int,
    val lessonNumber: Int,
    val gradeDate: String, // YYYY-MM-DD
    val gradeType: String = "MASTERY", // MASTERY, ATTENDANCE, BEHAVIOR, etc.
    val gradeCategory: String = "REGULAR",
    val value: String, // e.g. "5", "+", "-", "k"
    val numericValue: Double? = null,
    val status: String = "draft", // "draft" (tahrirlanadigan) or "approved" (tasdiqlangan)
    val approvedByParent: Boolean = false,
    val gradingSystemId: Int? = null,
    val syncStatus: String = "SYNCED", // "SYNCED", "PENDING_UPSERT", "PENDING_DELETE"
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "grade_comments")
data class GradeCommentEntity(
    @PrimaryKey val localId: String = UUID.randomUUID().toString(),
    val serverId: Int? = null,
    val localGradeId: String, // links to GradeEntity.localId
    val serverGradeId: Int? = null, // PostgreSQL grades.id
    val authorId: Int,
    val authorName: String? = null,
    val content: String,
    val createdAt: String,
    val syncStatus: String = "SYNCED" // "SYNCED", "PENDING"
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // "GRADE_BATCH", "GRADE_STATUS", "COMMENT"
    val payloadJson: String,
    val referenceId: String? = null,
    val retryCount: Int = 0,
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "FAILED"
    val createdAt: Long = System.currentTimeMillis()
)

data class StudentLastGradeDate(
    val studentId: Int,
    val maxDate: String
)
