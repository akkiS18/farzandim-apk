package uz.farzandim.parent.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "children")
data class ChildEntity(
    @PrimaryKey val id: Int, // real student_id
    val userId: Int?,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val classId: Int?,
    val className: String?,
    val classLevel: Int?,
    val address: String?,
    val birthdate: String?,
    val ina: String?,
    val balance: Double
)

@Entity(tableName = "parent_grades")
data class ParentGradeEntity(
    @PrimaryKey val id: Int, // server grade id
    val studentId: Int, // real student_id
    val subjectId: Int?,
    val subjectName: String,
    val teacherName: String?,
    val value: String, // e.g. "5", "+", "-", "k", "2"
    val numericValue: Double?,
    val gradeDate: String, // YYYY-MM-DD
    val gradeType: String = "MASTERY", // MASTERY, BEHAVIOR, ATTENDANCE
    val gradeCategory: String = "DAILY",
    val lessonNumber: Int?,
    val status: String?, // marked, approved
    val approvedByParent: Boolean = false,
    val createdAt: String?
)

@Entity(tableName = "parent_schedules")
data class ParentScheduleEntity(
    @PrimaryKey val id: Int,
    val classId: Int,
    val dayOfWeek: Int, // 1=Mon, 6=Sat
    val lessonNumber: Int,
    val subjectId: Int,
    val subjectName: String,
    val teacherName: String?,
    val room: String?,
    val startTime: String?,
    val endTime: String?,
    val topicName: String? = null,
    val homework: String? = null
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val holidayDate: String, // YYYY-MM-DD
    val targetClassesJson: String?,
    val targetLevelsJson: String?
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val authorName: String?,
    val createdAt: String?,
    val isPoll: Boolean = false,
    val pollOptionsJson: String?,
    val userVotedOptionId: Int?,
    val imageUrlsJson: String? = null
)

@Entity(tableName = "menu_days")
data class MenuEntity(
    @PrimaryKey val menuDate: String, // YYYY-MM-DD
    val mealsJson: String,
    val notes: String?
)

@Entity(tableName = "clubs")
data class ClubEntity(
    @PrimaryKey val id: Int,
    val studentId: Int,
    val name: String,
    val description: String?,
    val teacherName: String?,
    val status: String?, // APPROVED, PENDING, NONE
    val scheduleText: String?
)

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val author: String,
    val description: String?,
    val coverUrl: String?,
    val fileUrl: String?,
    val level: Int?,
    val categoryName: String?
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // PARENT_APPROVE_GRADE, CREATE_GRADE_COMMENT, CREATE_MENU_COMMENT, VOTE_POLL, CLUB_REQUEST, CLUB_CANCEL_REQUEST
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
